package com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon;

import com.kkdfg.Tacz_morelevel.entity.ai.PunisherShoulderCannonShootGoal;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 判罚者肩炮：无目标随机转头；有目标则朝向并每 20 tick 打 RPG。 */
public class PunisherShoulderCannonEntity extends AbstractShoulderCannonEntity implements GeoEntity {

    public static final int FIRE_INTERVAL_TICKS = 20;
    public static final int PLAYER_TARGET_LOS_TTL_TICKS = 200;
    public static final byte SHOOT_ANIM_EVENT = 61;

    @Nullable
    private UUID losTrackedId;
    private long lastLosGameTime;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PunisherShoulderCannonEntity(
            EntityType<? extends PunisherShoulderCannonEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new PunisherShoulderCannonShootGoal(this));
        super.registerGoals();
    }

    /** 玩家目标连续看不见超过 {@link #PLAYER_TARGET_LOS_TTL_TICKS} 则清空 lastHurt。 */
    public void tickPlayerTargetLos(LivingEntity host, LivingEntity target, boolean seen) {
        if (!(host instanceof Player player) || target == null) {
            return;
        }
        long now = this.level().getGameTime();
        UUID id = target.getUUID();
        if (!id.equals(this.losTrackedId)) {
            this.losTrackedId = id;
            this.lastLosGameTime = now;
        }
        if (seen) {
            this.lastLosGameTime = now;
            return;
        }
        if (now - this.lastLosGameTime < PLAYER_TARGET_LOS_TTL_TICKS) {
            return;
        }
        if (player.getLastHurtMob() == target) {
            player.setLastHurtMob(null);
        }
        if (player.getLastHurtByMob() == target) {
            player.setLastHurtByMob(null);
        }
        this.losTrackedId = null;
        this.setTarget(null);
    }

    public void playShootAnimation() {
        this.triggerAnim("cannon", "shoot");
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, SHOOT_ANIM_EVENT);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == SHOOT_ANIM_EVENT) {
            this.triggerAnim("cannon", "shoot");
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(
                                this,
                                "cannon",
                                0,
                                state -> {
                                    if (state.isCurrentAnimation(SHOOT)
                                            && !state.getController().hasAnimationFinished()) {
                                        return PlayState.CONTINUE;
                                    }
                                    return state.setAndContinue(IDLE);
                                })
                        .triggerableAnim("shoot", SHOOT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
