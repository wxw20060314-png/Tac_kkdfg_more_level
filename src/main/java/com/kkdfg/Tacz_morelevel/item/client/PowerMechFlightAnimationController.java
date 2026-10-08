package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.item.custom.PowerMechBulletproofArmorItem;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightAnimState;
import com.kkdfg.Tacz_morelevel.item.custom.PowerMechFlightState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

/**
 * fly 动画：0 帧展开、末帧收拢。起飞锁第 0 帧；结束飞行正播一次回收。
 */
public final class PowerMechFlightAnimationController {

    private static final RawAnimation FLY = RawAnimation.begin().thenPlay("fly");

    private PowerMechFlightAnimationController() {}

    public static PlayState tick(AnimationState<PowerMechBulletproofArmorItem> state) {
        Entity entity = state.getData(DataTickets.ENTITY);
        if (!(entity instanceof LivingEntity living)) {
            return PlayState.STOP;
        }

        if (living instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                resetController(state);
                PowerMechFlightAnimState.clear(living);
                return PlayState.STOP;
            }
        }

        if (!PowerMechFlightState.hasChestplate(living)) {
            resetController(state);
            PowerMechFlightAnimState.clear(living);
            return PlayState.STOP;
        }

        boolean flying = PowerMechFlightState.isPowerMechFlying(living);
        boolean wasFlying = PowerMechFlightAnimState.wasFlying(living);
        PowerMechFlightAnimState.Phase phase = PowerMechFlightAnimState.getPhase(living);
        AnimationController<PowerMechBulletproofArmorItem> controller = state.getController();

        if (flying && !wasFlying) {
            PowerMechFlightAnimState.setWasFlying(living, true);
            PowerMechFlightAnimState.setPhase(living, PowerMechFlightAnimState.Phase.DEPLOYED);
            controller.forceAnimationReset();
            controller.setAnimationSpeed(0);
            return state.setAndContinue(FLY);
        }

        if (!flying && wasFlying) {
            PowerMechFlightAnimState.setWasFlying(living, false);
            PowerMechFlightAnimState.setPhase(living, PowerMechFlightAnimState.Phase.RETRACTING);
            controller.setAnimationSpeed(1);
            if (!state.isCurrentAnimationStage("fly")) {
                controller.forceAnimationReset();
                return state.setAndContinue(FLY);
            }
            return PlayState.CONTINUE;
        }

        if (phase == PowerMechFlightAnimState.Phase.RETRACTING && controller.hasAnimationFinished()) {
            resetController(state);
            PowerMechFlightAnimState.setPhase(living, PowerMechFlightAnimState.Phase.DEFAULT);
            return PlayState.STOP;
        }

        if (phase == PowerMechFlightAnimState.Phase.RETRACTING) {
            return PlayState.CONTINUE;
        }

        if (flying && phase == PowerMechFlightAnimState.Phase.DEPLOYED) {
            controller.setAnimationSpeed(0);
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    private static void resetController(AnimationState<PowerMechBulletproofArmorItem> state) {
        state.getController().setAnimationSpeed(1);
    }
}
