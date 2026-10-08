package com.kkdfg.Tacz_morelevel.client;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.integration.tacz.AssistAimHelper;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunFireEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * 歼灭者辅助瞄准：按玩家 UUID 分别存储命中记忆与拉枪状态；{@link EntityHurtByGunEvent.Post} 记录目标，
 * {@link GunFireEvent} 校验后延迟 1 tick 步进准心。
 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AssistAimExecutor {

    private static final Map<UUID, PlayerAssistState> STATE_BY_PLAYER = new HashMap<>();

    private AssistAimExecutor() {}

    @SubscribeEvent
    public static void onGunHurtPost(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide() != LogicalSide.CLIENT) {
            return;
        }
        if (!AssistAimHelper.GUN_ID.equals(event.getGunId())) {
            return;
        }
        if (!(event.getAttacker() instanceof LocalPlayer player)) {
            return;
        }
        Entity hurt = event.getHurtEntity();
        if (!(hurt instanceof LivingEntity living)) {
            return;
        }
        PlayerAssistState state = stateFor(player.getUUID());
        state.lastHitTargetId = living.getUUID();
        state.lastHitTargetEntityId = living.getId();
        state.assistMemoryTicksRemaining = AssistAimHelper.HIT_MEMORY_TICKS;
    }

    @SubscribeEvent
    public static void onGunFire(GunFireEvent event) {
        if (event.isCanceled() || event.getLogicalSide() != LogicalSide.CLIENT) {
            return;
        }
        if (!(event.getShooter() instanceof LocalPlayer player)) {
            return;
        }
        if (!AssistAimHelper.isAnnihilator(event.getGunItemStack())) {
            return;
        }
        PlayerAssistState state = stateFor(player.getUUID());
        if (state.pendingTarget != null) {
            return;
        }
        LivingEntity cached = resolveCachedTarget(player, state);
        if (cached == null) {
            return;
        }
        state.pendingTarget = cached;
        state.aimPullDelayTicks = 1;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tickDownAssistMemoryForAllPlayers();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        PlayerAssistState state = STATE_BY_PLAYER.get(player.getUUID());
        if (state == null) {
            return;
        }
        if (state.aimPullDelayTicks > 0) {
            state.aimPullDelayTicks--;
        }
        if (state.pendingTarget == null || state.aimPullDelayTicks > 0) {
            pruneIfEmpty(player.getUUID(), state);
            return;
        }
        if (!state.pendingTarget.isAlive() || state.pendingTarget.level() != player.level()) {
            state.clearPending();
            pruneIfEmpty(player.getUUID(), state);
            return;
        }
        if (!AssistAimHelper.isAnnihilator(player.getMainHandItem())) {
            state.clearPending();
            pruneIfEmpty(player.getUUID(), state);
            return;
        }
        if (!AssistAimHelper.canAssistToward(player, state.pendingTarget)) {
            state.clearPending();
            pruneIfEmpty(player.getUUID(), state);
            return;
        }

        float yaw = AssistAimHelper.stepYawToward(player, state.pendingTarget);
        float pitch = AssistAimHelper.stepPitchToward(player, state.pendingTarget);
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(pitch);
        state.clearPending();
        pruneIfEmpty(player.getUUID(), state);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STATE_BY_PLAYER.remove(event.getEntity().getUUID());
    }

    /** 每位玩家的记忆 tick 独立递减，与是否持歼灭者无关。 */
    private static void tickDownAssistMemoryForAllPlayers() {
        Iterator<Map.Entry<UUID, PlayerAssistState>> iterator = STATE_BY_PLAYER.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerAssistState> entry = iterator.next();
            PlayerAssistState state = entry.getValue();
            if (state.assistMemoryTicksRemaining > 0) {
                state.assistMemoryTicksRemaining--;
                if (state.assistMemoryTicksRemaining <= 0) {
                    state.clearHitMemory();
                }
            }
            if (state.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static PlayerAssistState stateFor(UUID playerId) {
        return STATE_BY_PLAYER.computeIfAbsent(playerId, id -> new PlayerAssistState());
    }

    private static void pruneIfEmpty(UUID playerId, PlayerAssistState state) {
        if (state.isEmpty()) {
            STATE_BY_PLAYER.remove(playerId);
        }
    }

    @Nullable
    private static LivingEntity resolveCachedTarget(LocalPlayer player, PlayerAssistState state) {
        if (state.lastHitTargetId == null
                || state.lastHitTargetEntityId < 0
                || state.assistMemoryTicksRemaining <= 0) {
            return null;
        }
        Entity entity = player.level().getEntity(state.lastHitTargetEntityId);
        if (!(entity instanceof LivingEntity living) || !living.getUUID().equals(state.lastHitTargetId)) {
            return null;
        }
        if (!AssistAimHelper.canAssistToward(player, living)) {
            return null;
        }
        return living;
    }

    private static final class PlayerAssistState {
        @Nullable
        private UUID lastHitTargetId;
        private int lastHitTargetEntityId = -1;
        private int assistMemoryTicksRemaining;
        @Nullable
        private LivingEntity pendingTarget;
        private int aimPullDelayTicks;

        private void clearHitMemory() {
            this.lastHitTargetId = null;
            this.lastHitTargetEntityId = -1;
            this.assistMemoryTicksRemaining = 0;
        }

        private void clearPending() {
            this.pendingTarget = null;
            this.aimPullDelayTicks = 0;
        }

        private boolean isEmpty() {
            return this.lastHitTargetId == null
                    && this.lastHitTargetEntityId < 0
                    && this.assistMemoryTicksRemaining <= 0
                    && this.pendingTarget == null
                    && this.aimPullDelayTicks <= 0;
        }
    }
}
