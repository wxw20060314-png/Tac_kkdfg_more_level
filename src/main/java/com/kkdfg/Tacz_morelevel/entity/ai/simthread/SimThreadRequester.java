package com.kkdfg.Tacz_morelevel.entity.ai.simthread;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * 模拟线程请求入口：静态 {@link #submit}/{@link #cancel} 可直接用 uuid；
 * 实体实现本接口后可用 {@link #requestSimThread}，并由 {@link #canSimThreadEnqueue()} 作总闸。
 */
public interface SimThreadRequester {

    UUID getUUID();

    /**
     * 总闸：地面行走、非特殊机体等。为 false 时 {@link #submit} / {@link #requestSimThread} 拒绝入队。
     * 战争机器等可恒为 false。
     */
    boolean canSimThreadEnqueue();

    /**
     * 按类型提交；{@code type == null} 视为取消。
     *
     * @return 是否被管理器接受（含合并已有票）
     */
    default boolean requestSimThread(
            ServerLevel level, @Nullable SimThreadEventType type, int urgency) {
        return this.requestSimThread(level, type, urgency, -1.0F, -1.0F);
    }

    /**
     * 带找掩体距离闸提交（写入票供 CPU 上机前再验）。
     *
     * @param coverNear 近界；{@code <0} 不限
     * @param coverFar 远界；{@code <0} 不限
     */
    default boolean requestSimThread(
            ServerLevel level,
            @Nullable SimThreadEventType type,
            int urgency,
            float coverNear,
            float coverFar) {
        if (!this.canSimThreadEnqueue()) {
            return false;
        }
        return submit(level, this.getUUID(), type, urgency, coverNear, coverFar);
    }

    /**
     * @param type {@code null} 表示取消并清除该 uuid 进程/排队
     * @return 是否入队或合并成功；取消时返回 false
     */
    static boolean submit(
            ServerLevel level, UUID uuid, @Nullable SimThreadEventType type, int urgency) {
        return submit(level, uuid, type, urgency, -1.0F, -1.0F);
    }

    static boolean submit(
            ServerLevel level,
            UUID uuid,
            @Nullable SimThreadEventType type,
            int urgency,
            float coverNear,
            float coverFar) {
        if (level == null || level.isClientSide() || uuid == null) {
            return false;
        }
        if (!SimThreadEventType.isRunnable(type)) {
            SimThreadServices.manager(level).cancel(uuid);
            return false;
        }
        Entity entity = level.getEntity(uuid);
        if (entity instanceof SimThreadRequester requester && !requester.canSimThreadEnqueue()) {
            return false;
        }
        return SimThreadServices.manager(level).submit(uuid, type, urgency, coverNear, coverFar);
    }

    static void cancel(ServerLevel level, UUID uuid) {
        if (level == null || uuid == null) {
            return;
        }
        SimThreadServices.manager(level).cancel(uuid);
    }
}
