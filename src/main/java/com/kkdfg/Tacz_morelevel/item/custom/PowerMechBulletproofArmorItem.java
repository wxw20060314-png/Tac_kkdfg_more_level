package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import com.kkdfg.Tacz_morelevel.item.client.PowerMechFlightAnimationController;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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
 * 动力机甲防弹套：继承 {@link BulletproofArmor}，TaCZ 防弹档位 7；Gecko 模型见 {@code geo/armor/power_mech_bulletproof.geo.json}。
 *
 * <p>每件传说技能：一、爆炸减伤见 {@code armor.toml} {@code explosionReductionPerPiece.powerMech}（默认
 * {@link #EXPLOSION_REDUCTION_PER_PIECE}；运行时 {@link ExplosionArmorReduction}）；二、全套「过载」（见 {@link com.kkdfg.Tacz_morelevel.armor.skill.power_mech.PowerMechLegendarySkills}）。
 * <p>飞行能量存于胸甲 NBT，见 {@link com.kkdfg.Tacz_morelevel.event.PowerMechFlightEvents}、{@link PowerMechFlightState}。
 * <p>全套夜视：按键开关，见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler}。
 */
public class PowerMechBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    public static final String FLIGHT_ENERGY_TAG = "FlightEnergy";

    public static final int FLIGHT_ENERGY_MAX = 2000;
    public static final float FALL_REDUCTION_BOOTS = 1.0f;
    public static final int FLIGHT_DRAIN_PER_TICK = 8;

    public static final int FLIGHT_REGEN_PER_PIECE = 1;

    /** 能量高于该比例时，飞行速度与消耗按 {@link #SPEED_PERSENT} 倍率放大。 */
    public static final float FLIGHT_SPEED_ENERGY_THRESHOLD = 0.2f;

    public static final float SPEED_PERSENT = 1f;

    /** 动力机甲基础飞行速度（原版默认 0.05）；实际速度 = 此值 × {@link #getFlightSpeedMultiplier(int)}。 */
    public static final float FLIGHT_SPEED = 0.025f;

    /** 每件爆炸伤害减免比例（传说技能一）。 */
    public static final float EXPLOSION_REDUCTION_PER_PIECE = 0.08f;

    public static final int FULL_SET_PIECES = 4;

    /** 传说技能二「过载」消耗胸甲能量。 */
    public static final int OVERLOAD_ENERGY_COST = 2000;

    /** 过载：伤害吸收 IV，持续 20 秒。 */
    public static final int OVERLOAD_ABSORPTION_DURATION_TICKS = 20 * 10;

    /** 伤害吸收等级 IV → amplifier 3。 */
    public static final int OVERLOAD_ABSORPTION_AMPLIFIER = 1;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PowerMechBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int n = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof PowerMechBulletproofArmorItem) {
                n++;
            }
        }
        return n;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    public static int getFlightEnergy(ItemStack chestStack) {
        if (!(chestStack.getItem() instanceof PowerMechBulletproofArmorItem)) {
            return 0;
        }
        if (!chestStack.hasTag()) {
            chestStack.setTag(new CompoundTag());
        }
        CompoundTag tag = chestStack.getTag();
        if (tag != null && !tag.contains(FLIGHT_ENERGY_TAG)) {
            tag.putInt(FLIGHT_ENERGY_TAG, FLIGHT_ENERGY_MAX);
        }
        return tag == null
                ? FLIGHT_ENERGY_MAX
                : Mth.clamp(tag.getInt(FLIGHT_ENERGY_TAG), 0, FLIGHT_ENERGY_MAX);
    }

    public static void setFlightEnergy(ItemStack chestStack, int energy) {
        if (!(chestStack.getItem() instanceof PowerMechBulletproofArmorItem)) {
            return;
        }
        chestStack.getOrCreateTag().putInt(FLIGHT_ENERGY_TAG, Mth.clamp(energy, 0, FLIGHT_ENERGY_MAX));
    }

    /**
     * 按当前能量比例计算飞行速度/消耗倍率。
     * 能量 &lt; 40% 时为 1；≥ 40% 时为 (能量比例 / 0.4) × {@link #SPEED_PERSENT}。
     */
    public static float getFlightSpeedMultiplier(int energy) {
        float percent = energy / (float) FLIGHT_ENERGY_MAX;
        if (percent < FLIGHT_SPEED_ENERGY_THRESHOLD) {
            return 1f;
        }
        float t=percent / FLIGHT_SPEED_ENERGY_THRESHOLD * SPEED_PERSENT;
        return t*t ;
    }

    public static float getFlightSpeed(int energy) {
        return FLIGHT_SPEED * getFlightSpeedMultiplier(energy);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerPowerMechBulletproofArmor(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "flight", 0, PowerMechFlightAnimationController::tick));
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
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.power_mech_bulletproof.legendary")
                        .withStyle(style));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.power_mech_bulletproof.overload")
                        .withStyle(style));
        tooltip.add(
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
