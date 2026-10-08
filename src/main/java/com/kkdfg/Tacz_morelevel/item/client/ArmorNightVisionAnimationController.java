package com.kkdfg.Tacz_morelevel.item.client;

import com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler;
import com.kkdfg.Tacz_morelevel.item.custom.ArmorNightVisionAnimState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

/**
 * 夜视头盔面罩：不做过渡动画，仅在 {@code clip_on} / {@code clip_off} 两帧姿态间瞬切。
 * 与 zhuan 等其它控制器可并行。
 */
public final class ArmorNightVisionAnimationController {

    private final String onClipName;
    private final String offClipName;
    private final RawAnimation onAnimation;
    private final RawAnimation offAnimation;

    private ArmorNightVisionAnimationController(String clipBase) {
        this.onClipName = clipBase + "_on";
        this.offClipName = clipBase + "_off";
        this.onAnimation = RawAnimation.begin().thenLoop(onClipName);
        this.offAnimation = RawAnimation.begin().thenLoop(offClipName);
    }

    /** @param clipBase 动画前缀，对应资源中的 {@code {clipBase}_on} 与 {@code {clipBase}_off} */
    public static ArmorNightVisionAnimationController forClip(String clipBase) {
        return new ArmorNightVisionAnimationController(clipBase);
    }

    public <T extends GeoItem> PlayState tick(AnimationState<T> state) {
        if (!(state.getAnimatable() instanceof ArmorItem armor)
                || armor.getType() != ArmorItem.Type.HELMET) {
            return PlayState.STOP;
        }

        Entity entity = state.getData(DataTickets.ENTITY);
        if (!(entity instanceof LivingEntity living)) {
            return PlayState.STOP;
        }

        ItemStack helmet = living.getItemBySlot(EquipmentSlot.HEAD);
        if (!ArmorNightVisionHandler.isEligibleHelmet(helmet)) {
            ArmorNightVisionAnimState.clear(living);
            state.getController().setAnimationSpeed(1);
            return PlayState.STOP;
        }

        boolean nvOn = ArmorNightVisionHandler.isEnabled(helmet);
        boolean wasOn = ArmorNightVisionAnimState.wasEnabled(living);
        AnimationController<T> controller = state.getController();
        RawAnimation target = nvOn ? onAnimation : offAnimation;
        String targetClip = nvOn ? onClipName : offClipName;

        if (!ArmorNightVisionAnimState.isInitialized(living) || nvOn != wasOn) {
            ArmorNightVisionAnimState.setInitialized(living, true);
            ArmorNightVisionAnimState.setWasEnabled(living, nvOn);
            controller.forceAnimationReset();
            controller.setAnimationSpeed(0);
            return state.setAndContinue(target);
        }

        if (!state.isCurrentAnimationStage(targetClip)) {
            controller.forceAnimationReset();
            controller.setAnimationSpeed(0);
            return state.setAndContinue(target);
        }

        controller.setAnimationSpeed(0);
        return PlayState.CONTINUE;
    }
}
