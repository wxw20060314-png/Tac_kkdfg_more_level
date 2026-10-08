package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ArmorNightVisionAnimationController;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
 * 冲击波套：数值同七级防弹；Gecko {@code geo/armor/shockwave_bulletproof.geo.json}。
 * 发光层 {@code shockwave_bulletproof_glowmask.png}（{@link com.kkdfg.Tacz_morelevel.item.client.ShockwaveBulletproofArmorRenderer}）。
 * <p>每件爆炸减伤见 {@code armor.toml} {@code explosionReductionPerPiece.shockwave}（默认
 * {@link #EXPLOSION_REDUCTION_PER_PIECE}）；摔落减伤每件 {@link #FALL_REDUCTION_PER_PIECE}。
 * <p>头盔可按键夜视（见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler}）。
 * <p>传说技能：能量→动能池蓄力冲刺（见 {@link
 * com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill}）。
 */
public class ShockwaveBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    public static final int FULL_SET_PIECES = 4;

    /** 每件爆炸伤害减免比例（默认 8%；运行时读 Config）。 */
    public static final float EXPLOSION_REDUCTION_PER_PIECE = 0.08f;
    /** 每件摔落伤害减免比例（四件合计 100%）。 */
    public static final float FALL_REDUCTION_PER_PIECE = 0.25f;

    public static final String ENERGY_TAG = "ShockwaveEnergy";
    public static final String KINETIC_TAG = "ShockwaveKinetic";
    public static final String CHARGING_TAG = "ShockwaveCharging";
    public static final String DASH_COOLDOWN_UNTIL_TAG = "ShockwaveDashCooldownUntil";

    public static final int ENERGY_MAX = 2000;
    /** 每 tick 回能（玩家；含蓄力中）。 */
    public static final int REGEN_PER_TICK = 10;
    /**
     * 非玩家（Boss/NPC）回能倍率：实际回能 = {@link #REGEN_PER_TICK} × 本值。
     * 默认 3 → NPC 每 tick +30。
     */
    public static final int NPC_ENERGY_REGEN_MULTIPLIER = 3;
    /** 按住每 tick 从能量转入动能池。 */
    public static final int DRAIN_PER_TICK = 40;
    /** impulse = K * kinetic。 */
    public static final double IMPULSE_K = 0.005D;
    /** 松开 / 能量耗尽释放后的冷却 TTL。 */
    public static final int COOLDOWN_TICKS = 50;

    /** 当前实体本 tick 应回复的能量（玩家为 {@link #REGEN_PER_TICK}，NPC 乘 {@link #NPC_ENERGY_REGEN_MULTIPLIER}）。 */
    public static int energyRegenPerTick(LivingEntity entity) {
        int base = REGEN_PER_TICK;
        if (entity instanceof Player) {
            return base;
        }
        return base * Math.max(1, NPC_ENERGY_REGEN_MULTIPLIER);
    }

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ZHUAN = RawAnimation.begin().thenLoop("zhuan");
    private static final ArmorNightVisionAnimationController FANZ_CONTROLLER =
            ArmorNightVisionAnimationController.forClip("fanz");
    private static final Style TOOLTIP_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF));

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ShockwaveBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof ShockwaveBulletproofArmorItem) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    public static boolean isShockwaveChest(ItemStack chestStack) {
        return chestStack.getItem() instanceof ShockwaveBulletproofArmorItem;
    }

    public static int getEnergy(ItemStack chestStack) {
        if (!isShockwaveChest(chestStack)) {
            return 0;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null || !tag.contains(ENERGY_TAG)) {
            return ENERGY_MAX;
        }
        return Mth.clamp(tag.getInt(ENERGY_TAG), 0, ENERGY_MAX);
    }

    public static void setEnergy(ItemStack chestStack, int energy) {
        if (!isShockwaveChest(chestStack)) {
            return;
        }
        chestStack.getOrCreateTag().putInt(ENERGY_TAG, Mth.clamp(energy, 0, ENERGY_MAX));
    }

    public static int getKinetic(ItemStack chestStack) {
        if (!isShockwaveChest(chestStack)) {
            return 0;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null || !tag.contains(KINETIC_TAG)) {
            return 0;
        }
        return Math.max(0, tag.getInt(KINETIC_TAG));
    }

    public static void setKinetic(ItemStack chestStack, int kinetic) {
        if (!isShockwaveChest(chestStack)) {
            return;
        }
        int value = Math.max(0, kinetic);
        CompoundTag tag = chestStack.getOrCreateTag();
        if (value <= 0) {
            tag.remove(KINETIC_TAG);
            if (tag.isEmpty()) {
                chestStack.setTag(null);
            }
            return;
        }
        tag.putInt(KINETIC_TAG, value);
    }

    public static boolean isCharging(ItemStack chestStack) {
        if (!isShockwaveChest(chestStack)) {
            return false;
        }
        CompoundTag tag = chestStack.getTag();
        return tag != null && tag.getBoolean(CHARGING_TAG);
    }

    public static void setCharging(ItemStack chestStack, boolean charging) {
        if (!isShockwaveChest(chestStack)) {
            return;
        }
        CompoundTag tag = chestStack.getOrCreateTag();
        if (!charging) {
            tag.remove(CHARGING_TAG);
            if (tag.isEmpty()) {
                chestStack.setTag(null);
            }
            return;
        }
        tag.putBoolean(CHARGING_TAG, true);
    }

    public static long getDashCooldownUntil(ItemStack chestStack) {
        if (!isShockwaveChest(chestStack)) {
            return 0L;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null || !tag.contains(DASH_COOLDOWN_UNTIL_TAG)) {
            return 0L;
        }
        return tag.getLong(DASH_COOLDOWN_UNTIL_TAG);
    }

    public static boolean isDashOnCooldown(ItemStack chestStack, long gameTime) {
        return gameTime < getDashCooldownUntil(chestStack);
    }

    public static void startDashCooldown(ItemStack chestStack, long gameTime, int cooldownTicks) {
        if (!isShockwaveChest(chestStack)) {
            return;
        }
        long until = gameTime + Math.max(0, cooldownTicks);
        chestStack.getOrCreateTag().putLong(DASH_COOLDOWN_UNTIL_TAG, until);
    }

    public static double impulseFromKinetic(int kinetic) {
        if (kinetic <= 0) {
            return 0.0D;
        }
        return IMPULSE_K * kinetic;
    }

    /**
     * 满能量开蓄力、按与 {@link com.kkdfg.Tacz_morelevel.armor.skill.impl.ShockwaveDashSkill#tickCharge}
     * 相同顺序（先回能再 drain）耗尽时的动能上限。随 {@link #ENERGY_MAX}/{@link #REGEN_PER_TICK}/{@link
     * #DRAIN_PER_TICK} 变化。若回能 ≥ 消耗则能量不降，返回当前已积动能（防护循环上限）。
     */
    public static int maxKineticFromFullCharge() {
        if (DRAIN_PER_TICK <= 0) {
            return 0;
        }
        int energy = ENERGY_MAX;
        int kinetic = 0;
        int guardLimit = ENERGY_MAX * 4 + 8;
        for (int guard = 0; guard < guardLimit; guard++) {
            energy = Math.min(ENERGY_MAX, energy + REGEN_PER_TICK);
            int spend = Math.min(DRAIN_PER_TICK, energy);
            if (spend <= 0) {
                break;
            }
            energy -= spend;
            kinetic += spend;
            if (energy <= 0) {
                return kinetic;
            }
        }
        return kinetic;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerShockwaveBulletproofArmor(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.shockwave_bulletproof.legendary")
                        .withStyle(TOOLTIP_STYLE));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.shockwave_bulletproof.legendary_2")
                        .withStyle(TOOLTIP_STYLE));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.shockwave_bulletproof.legendary_3")
                        .withStyle(TOOLTIP_STYLE));
        tooltip.add(
                Component.translatable(
                                "tooltip.tacz_morelevel.bulletproof_level",
                                TieredBulletproofGeoArmorItem.toRomanNumeral(this.getLevel()))
                        .withStyle(TOOLTIP_STYLE));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "idle", 10, state -> {
                    state.setAnimation(IDLE);
                    return PlayState.CONTINUE;
                }));
        controllers.add(
                new AnimationController<>(this, "zhuan", 0, state -> {
                    state.setAnimation(ZHUAN);
                    return PlayState.CONTINUE;
                }));
        controllers.add(
                new AnimationController<>(this, "fanz", 0, FANZ_CONTROLLER::tick));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
