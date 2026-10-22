package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

/**
 * 单进程执行器：任意时刻至多处理一张票。成功则解读投递；失败静默（UDP）。
 */
public final class SimThreadCpu {

    private final List<SimThreadSolver> solvers = new ArrayList<>();

    @Nullable
    private SimThreadTicket running;

    public SimThreadCpu() {
        this.solvers.add(new FindCoverSolver());
    }

    public void registerSolver(SimThreadSolver solver) {
        if (solver != null) {
            this.solvers.add(solver);
        }
    }

    public boolean isBusy() {
        return this.running != null;
    }

    @Nullable
    public SimThreadTicket running() {
        return this.running;
    }

    /** 接受上机；已有进程时拒绝。 */
    public boolean accept(SimThreadTicket ticket) {
        if (this.running != null || ticket == null || !ticket.isRunnable()) {
            return false;
        }
        this.running = ticket;
        ticket.seat = SimThreadTicket.Seat.RUNNING;
        return true;
    }

    /** 类型非法或需强制清槽。 */
    public void clear() {
        this.running = null;
    }

    /**
     * 跑完当前进程（框架：一 tick 一次算完）。返回被消费的 uuid（无论成败），无进程返回 null。
     */
    @Nullable
    public UUID runOnce(ServerLevel level) {
        SimThreadTicket job = this.running;
        if (job == null) {
            return null;
        }
        UUID id = job.uuid;
        if (!job.isRunnable()) {
            this.running = null;
            return id;
        }
        SimThreadEventType type = job.type;
        Optional<SimThreadInfo> result = Optional.empty();
        for (SimThreadSolver solver : this.solvers) {
            if (solver.supports(type)) {
                result = solver.solve(level, id, type);
                break;
            }
        }
        result.ifPresent(info -> SimThreadInterpreter.deliver(level, id, info));
        this.running = null;
        return id;
    }
}
