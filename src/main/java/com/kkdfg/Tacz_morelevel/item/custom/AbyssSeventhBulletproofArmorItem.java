package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
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
 * 深渊七级套：与七级防弹相同的 {@link ModArmorMaterials#SEVENTH_BULLETPROOF} 与 TaCZ 防弹数值；
 * Gecko 模型/动画与七级共用，实体贴图为 {@code textures/item/armor/seventh_abyss_bulletproof.png}。
 *
 * <p>移速、跳跃见 {@link com.kkdfg.Tacz_morelevel.event.AbyssSeventhArmorAttributeEvents}；摔落减伤每件 {@link #FALL_REDUCTION_PER_PIECE}（见 {@link FallArmorReduction}）。
 */
public class AbyssSeventhBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    /** 每件：对 {@link net.minecraft.world.entity.ai.attributes.Attributes#MOVEMENT_SPEED} 的 MULTIPLY_BASE 增量（四件共 1.0 = +100% 基础段）。 */
    public static final double SPEED_MULTIPLY_BASE_PER_PIECE = 0.25;

    /** 每件摔落伤害减免比例。 */
    public static final float FALL_REDUCTION_PER_PIECE = 0.25f;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbyssSeventhBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int n = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof AbyssSeventhBulletproofArmorItem) {
                n++;
            }
        }
        return n;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerAbyssSeventhArmor(consumer);
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
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        Style style = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE);
        pTooltipComponents.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_abyss_bulletproof.legendary")
                        .withStyle(style));
        pTooltipComponents.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_abyss_bulletproof.legendary_2")
                        .withStyle(style));
        pTooltipComponents.add(
                Component.translatable(
                                "tooltip.tacz_morelevel.bulletproof_level",
                                TieredBulletproofGeoArmorItem.toRomanNumeral(this.getLevel()))
                        .withStyle(style));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
