package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 爆炎七级套：与七级防弹相同的 {@link ModArmorMaterials#SEVENTH_BULLETPROOF} 与 TaCZ 防弹数值；
 * Gecko 模型/动画与七级共用，实体贴图为 {@code textures/item/armor/seventh_blazeflame_bulletproof.png}。
 *
 * <p>爆炸伤害：每件见 {@code armor.toml} {@code explosionReductionPerPiece.blazeflame}（默认
 * {@link #EXPLOSION_REDUCTION_PER_PIECE}；运行时 {@link ExplosionArmorReduction}）。
 * <p>全套抗火：{@link com.kkdfg.Tacz_morelevel.event.FullSetFireResistanceEvents} 查四格。
 */
public class BlazeflameSeventhBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    /** 每件爆炸伤害减免比例。 */
    public static final float EXPLOSION_REDUCTION_PER_PIECE = 0.2f;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BlazeflameSeventhBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerBlazeflameSeventhArmor(consumer);
    }
    /** 全套时每 4 tick 刷新抗火 II（40 tick）。 */
    public static void tickFullSetFireResistance(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity.level().getGameTime() % 4 != 0) {
            return;
        }
        if (!hasFullBlazeflameSet(entity)) {
            return;
        }
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 1, false, false));
    }

    public static boolean hasFullBlazeflameSet(LivingEntity living) {
        return armorSlotsMatchItems(
            List.of(
                living.getItemBySlot(EquipmentSlot.HEAD),
                living.getItemBySlot(EquipmentSlot.CHEST),
                living.getItemBySlot(EquipmentSlot.LEGS),
                living.getItemBySlot(EquipmentSlot.FEET)),
            List.of(
                ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_HELMET.get(),
                ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_CHESTPLATE.get(),
                ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_LEGGINGS.get(),
                ModItems.SEVENTH_BLAZEFLAME_BULLETPROOF_BOOTS.get()));
    }

    private static boolean armorSlotsMatchItems(List<ItemStack> slots, List<Item> expectedItems) {
        if (slots.size() != expectedItems.size()) {
            return false;
        }
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getItem() != expectedItems.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 10, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
    }

    private static final Style BLAZEFLAME_TOOLTIP_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xFF9500));

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_blazeflame_bulletproof.legendary")
                        .withStyle(BLAZEFLAME_TOOLTIP_STYLE));
        pTooltipComponents.add(
                Component.translatable("tooltip.tacz_morelevel.seventh_blazeflame_bulletproof.legendary_2")
                        .withStyle(BLAZEFLAME_TOOLTIP_STYLE));
        pTooltipComponents.add(
                Component.translatable(
                                "tooltip.tacz_morelevel.bulletproof_level",
                                TieredBulletproofGeoArmorItem.toRomanNumeral(this.getLevel()))
                        .withStyle(BLAZEFLAME_TOOLTIP_STYLE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
