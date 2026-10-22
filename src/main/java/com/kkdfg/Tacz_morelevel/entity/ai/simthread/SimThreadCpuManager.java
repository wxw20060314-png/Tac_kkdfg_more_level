package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 模拟线程调度：等待区 FIFO（无等待）→ Ready（上限 {@link #READY_CAP}，每票自记 waitTicks）→ 单进程 {@link SimThreadCpu}。
 * <p>{@code f = urgency + min(waitTicks, AGE_CAP)}，选 f 最大者上机。
 * <p>FIND_COVER 带距离闸的票在上机前再验；越界则摘票（UDP）。
 */
public final class SimThreadCpuManager {

    public static final int READY_CAP = 40;
    public static final int WAIT_CAP = 200;
    /** Ready 等待老化上限（票自身 waitTicks）。 */
    public static final int AGE_CAP = 30;
    private final SimThreadCpu cpu = new SimThreadCpu();
    private final Map<UUID, SimThreadTicket> byId = new HashMap<>();
    private final ArrayDeque<UUID> waitOrder = new ArrayDeque<>();
    private final List<UUID> readyOrder = new ArrayList<>();

    public SimThreadCpu cpu() {
        return this.cpu;
    }

    public int readySize() {
        return this.readyOrder.size();
    }

    public int waitSize() {
        return this.waitOrder.size();
    }

    /**
     * 希望排队抽签概率：{@code p = 1 - (0.5/WAIT_CAP) × waitSize}。
     * Wait 空 → 1；Wait 满 → 0.5（约 2 倍均值运载）。
     */
    public double queueSize() {
        if (WAIT_CAP <= 0) {
            return 1.0D;
        }
        int wait = Math.min(Math.max(0, this.waitSize()), WAIT_CAP);
        double p = 1.0D - (0.5D / (double) WAIT_CAP) * (double) wait;
        if (p < 0.0D) {
            return 0.0D;
        }
        if (p > 1.0D) {
            return 1.0D;
        }
        return p;
    }

    /**
     * @return 是否入队或合并成功；{@code type == null} 时清除该 uuid 并返回 false
     */
    public boolean submit(UUID uuid, @Nullable SimThreadEventType type, int urgency) {
        return this.submit(uuid, type, urgency, -1.0F, -1.0F);
    }

    public boolean submit(
            UUID uuid,
            @Nullable SimThreadEventType type,
            int urgency,
            float coverNear,
            float coverFar) {
        if (uuid == null) {
            return false;
        }
        if (!SimThreadEventType.isRunnable(type)) {
            this.remove(uuid);
            return false;
        }
        int u = Math.max(0, urgency);
        SimThreadTicket existing = this.byId.get(uuid);
        if (existing != null) {
            existing.merge(type, u, coverNear, coverFar);
            if (!existing.isRunnable()) {
                this.remove(uuid);
                return false;
            }
            return true;
        }
        if (this.waitOrder.size() >= WAIT_CAP) {
            return false;
        }
        SimThreadTicket ticket = new SimThreadTicket(uuid, type, u, coverNear, coverFar);
        this.byId.put(uuid, ticket);
        this.waitOrder.addLast(uuid);
        ticket.seat = SimThreadTicket.Seat.WAIT;
        return true;
    }

    public void cancel(UUID uuid) {
        this.remove(uuid);
    }

    public void tick(ServerLevel level) {
        if (level == null || level.isClientSide()) {
            return;
        }
        this.tickReceiversOnReadyAndRunning(level);
        this.scrubReady(level);
        this.scrubCoverRangeGate(level);
        this.ageReadyWaits();
        this.scrubRunning(level);
        this.admitFromWait(level);
        if (!this.cpu.isBusy()) {
            SimThreadTicket best = this.pickBestReady();
            if (best != null) {
                this.detachFromReady(best.uuid);
                if (this.failsCoverRangeGate(level, best)) {
                    this.byId.remove(best.uuid);
                } else if (!this.cpu.accept(best)) {
                    this.requeueWait(best);
                }
            }
        }
        UUID finished = this.cpu.runOnce(level);
        if (finished != null) {
            this.finishJob(finished);
        }
        this.admitFromWait(level);
    }

    private void finishJob(UUID finished) {
        SimThreadTicket ticket = this.byId.get(finished);
        if (ticket != null && ticket.requeueAfterRun && ticket.isRunnable()) {
            ticket.requeueAfterRun = false;
            ticket.seat = SimThreadTicket.Seat.WAIT;
            ticket.resetWait();
            this.waitOrder.addLast(finished);
            return;
        }
        this.byId.remove(finished);
    }

    private void tickReceiversOnReadyAndRunning(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (this.cpu.running() != null) {
            this.tickReceiver(level, this.cpu.running().uuid, gameTime);
        }
        for (UUID id : this.readyOrder) {
            this.tickReceiver(level, id, gameTime);
        }
    }

    private void tickReceiver(ServerLevel level, UUID id, long gameTime) {
        Entity entity = level.getEntity(id);
        if (entity instanceof SimThreadReceiver receiver) {
            receiver.tickSimThreadReceiver(gameTime);
        }
    }

    private void scrubReady(ServerLevel level) {
        Iterator<UUID> it = this.readyOrder.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            SimThreadTicket ticket = this.byId.get(id);
            if (ticket == null || !ticket.isRunnable() || this.isKnownDead(level, id)) {
                it.remove();
                this.byId.remove(id);
            }
        }
    }

    /** Ready 区 FIND_COVER 距离闸失败则摘票（UDP）。未加载实体保留。 */
    private void scrubCoverRangeGate(ServerLevel level) {
        Iterator<UUID> it = this.readyOrder.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            SimThreadTicket ticket = this.byId.get(id);
            if (ticket == null) {
                it.remove();
                continue;
            }
            if (!this.failsCoverRangeGate(level, ticket)) {
                continue;
            }
            it.remove();
            this.byId.remove(id);
        }
    }

    /**
     * @return true = 应丢弃（越界 / 无目标 / 非 Mob）；实体未加载返回 false（保留票）
     */
    private boolean failsCoverRangeGate(ServerLevel level, SimThreadTicket ticket) {
        if (ticket.type != SimThreadEventType.FIND_COVER || !ticket.hasCoverRangeGate()) {
            return false;
        }
        Entity entity = level.getEntity(ticket.uuid);
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof Mob mob) || !mob.isAlive()) {
            return true;
        }
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return true;
        }
        double distSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        if (ticket.coverNear >= 0.0F
                && distSqr < (double) ticket.coverNear * (double) ticket.coverNear) {
            return true;
        }
        if (ticket.coverFar >= 0.0F
                && distSqr > (double) ticket.coverFar * (double) ticket.coverFar) {
            return true;
        }
        return false;
    }

    /** Ready 区每票自增等待（合并不重置）。 */
    private void ageReadyWaits() {
        for (UUID id : this.readyOrder) {
            SimThreadTicket ticket = this.byId.get(id);
            if (ticket != null && ticket.seat == SimThreadTicket.Seat.READY) {
                ticket.waitTicks++;
            }
        }
    }

    private void scrubRunning(ServerLevel level) {
        SimThreadTicket job = this.cpu.running();
        if (job == null) {
            return;
        }
        if (!job.isRunnable() || this.isKnownDead(level, job.uuid)) {
            this.byId.remove(job.uuid);
            this.cpu.clear();
        }
    }

    private void admitFromWait(ServerLevel level) {
        int guard = this.waitOrder.size();
        while (this.readyOrder.size() < READY_CAP && !this.waitOrder.isEmpty() && guard-- > 0) {
            UUID id = this.waitOrder.pollFirst();
            if (id == null) {
                break;
            }
            SimThreadTicket ticket = this.byId.get(id);
            if (ticket == null) {
                continue;
            }
            if (ticket.seat != SimThreadTicket.Seat.WAIT) {
                continue;
            }
            if (!ticket.isRunnable() || this.isKnownDead(level, id)) {
                this.byId.remove(id);
                continue;
            }
            Entity entity = level.getEntity(id);
            if (entity == null) {
                this.waitOrder.addLast(id);
                continue;
            }
            ticket.seat = SimThreadTicket.Seat.READY;
            ticket.resetWait();
            this.readyOrder.add(id);
        }
    }

    @Nullable
    private SimThreadTicket pickBestReady() {
        SimThreadTicket best = null;
        long bestScore = Long.MIN_VALUE;
        for (UUID id : this.readyOrder) {
            SimThreadTicket ticket = this.byId.get(id);
            if (ticket == null || ticket.seat != SimThreadTicket.Seat.READY) {
                continue;
            }
            long score = ticket.scheduleScore(AGE_CAP);
            if (best == null || score > bestScore) {
                best = ticket;
                bestScore = score;
            }
        }
        return best;
    }

    private void detachFromReady(UUID uuid) {
        this.readyOrder.remove(uuid);
    }

    private void requeueWait(SimThreadTicket ticket) {
        ticket.seat = SimThreadTicket.Seat.WAIT;
        ticket.resetWait();
        this.waitOrder.addFirst(ticket.uuid);
    }

    private void remove(UUID uuid) {
        SimThreadTicket ticket = this.byId.remove(uuid);
        this.waitOrder.remove(uuid);
        this.readyOrder.remove(uuid);
        if (ticket != null && this.cpu.running() == ticket) {
            this.cpu.clear();
        }
    }

    /**
     * 仅当实体已加载且确认死亡/非活物时丢弃；{@code getEntity == null} 视为未加载，保留票。
     */
    private boolean isKnownDead(ServerLevel level, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        if (entity == null) {
            return false;
        }
        return !(entity instanceof LivingEntity living && living.isAlive());
    }
}
