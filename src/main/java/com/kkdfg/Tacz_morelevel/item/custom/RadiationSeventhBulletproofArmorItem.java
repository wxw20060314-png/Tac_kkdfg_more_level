package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 辐抗七级套：与七级防弹相同材质与档位 7；Gecko 共用七级 geo/动画；
 * 贴图 {@code textures/item/armor/seventh_radiation_bulletproof.png}。
 * 每件辐射抗性 {@link #RADIATION_RESISTANCE_PER_PIECE}，见辐射抗板事件。
 */
public class RadiationSeventhBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    /** 每件对辐射等级比较的抗性增量。 */
    public static final float RADIATION_RESISTANCE_PER_PIECE = 0.8f;

    private static final Style NAME_STYLE =
            Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RadiationSeventhBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int n = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof RadiationSeventhBulletproofArmorItem) {
                n++;
            }
        }
        return n;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack)).withStyle(NAME_STYLE);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerRadiationSeventhArmor(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 10, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_radiation_bulletproof.legendary")
                        .withStyle(NAME_STYLE));
        tooltip.add(
                Component.translatable(
                                "tooltip.tacz_morelevel.bulletproof_level",
                                TieredBulletproofGeoArmorItem.toRomanNumeral(this.getLevel()))
                        .withStyle(NAME_STYLE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
