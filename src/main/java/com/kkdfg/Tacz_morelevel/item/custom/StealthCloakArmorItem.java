package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
 * 隐身斗篷套（帽/斗篷/裤/靴）：继承 {@link BulletproofArmor}，档位 3（三级套数值）。
 * 穿戴 Gecko 见 {@code geo/armor/stealth_cloak.geo.json}（64×64 贴图）。
 * <p>开关 / 冷却 / 池 t 均在斗篷（胸甲）NBT；开关默认关（无键）。
 * 技能见 {@link com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthCloakSkill}。规范手册§五。
 */
public class StealthCloakArmorItem extends BulletproofArmor implements GeoItem {

    public static final int FULL_SET_PIECES = 4;

    /** 破隐后冷却（10s）。 */
    public static final int STEALTH_COOLDOWN_TICKS = 200;

    public static final String STEALTH_COOLDOWN_UNTIL_TAG = "StealthCloakCooldownUntil";

    /**
     * 隐身开关（胸甲）。无此键或 false = 默认关闭；仅技能可打开。
     */
    public static final String STEALTH_ENABLED_TAG = "StealthCloakEnabled";

    /** 破隐池 t：上限；每 {@link #STEALTH_POOL_REGEN_INTERVAL_TICKS} tick +1。 */
    public static final float STEALTH_POOL_T_MAX = 4.0F;
    public static final int STEALTH_POOL_REGEN_INTERVAL_TICKS = 5;
    public static final String STEALTH_POOL_TAG = "StealthCloakPoolT";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StealthCloakArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.THIRD_BULLETPROOF, type, properties);
        this.setLevel(3);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int n = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof StealthCloakArmorItem) {
                n++;
            }
        }
        return n;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    public static ItemStack cloak(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST);
    }

    public static boolean isStealthCloak(ItemStack stack) {
        return stack.getItem() instanceof StealthCloakArmorItem armor
                && armor.getType() == ArmorItem.Type.CHESTPLATE;
    }

    /** 开关是否开；无 NBT 键 = 默认关闭。 */
    public static boolean isStealthEnabled(ItemStack cloakStack) {
        if (!isStealthCloak(cloakStack)) {
            return false;
        }
        CompoundTag tag = cloakStack.getTag();
        return tag != null && tag.getBoolean(STEALTH_ENABLED_TAG);
    }

    /** 写胸甲开关；关时移除键，保持默认关。 */
    public static void setStealthEnabled(ItemStack cloakStack, boolean enabled) {
        if (!isStealthCloak(cloakStack)) {
            return;
        }
        if (enabled) {
            cloakStack.getOrCreateTag().putBoolean(STEALTH_ENABLED_TAG, true);
            return;
        }
        CompoundTag tag = cloakStack.getTag();
        if (tag != null) {
            tag.remove(STEALTH_ENABLED_TAG);
            if (tag.isEmpty()) {
                cloakStack.setTag(null);
            }
        }
    }

    /** 当前池 t；无标签视为满。 */
    public static float getStealthPool(ItemStack cloakStack) {
        if (!isStealthCloak(cloakStack)) {
            return 0.0F;
        }
        CompoundTag tag = cloakStack.getTag();
        if (tag == null || !tag.contains(STEALTH_POOL_TAG)) {
            return STEALTH_POOL_T_MAX;
        }
        return Math.min(STEALTH_POOL_T_MAX, Math.max(0.0F, tag.getFloat(STEALTH_POOL_TAG)));
    }

    public static void setStealthPool(ItemStack cloakStack, float t) {
        if (!isStealthCloak(cloakStack)) {
            return;
        }
        float clamped = Math.min(STEALTH_POOL_T_MAX, Math.max(0.0F, t));
        if (clamped >= STEALTH_POOL_T_MAX) {
            CompoundTag tag = cloakStack.getTag();
            if (tag != null) {
                tag.remove(STEALTH_POOL_TAG);
                if (tag.isEmpty()) {
                    cloakStack.setTag(null);
                }
            }
            return;
        }
        cloakStack.getOrCreateTag().putFloat(STEALTH_POOL_TAG, clamped);
    }

    public static void fillStealthPool(ItemStack cloakStack) {
        setStealthPool(cloakStack, STEALTH_POOL_T_MAX);
    }

    /** 每 r tick：t = min(t+1, T_MAX)。需穿着斗篷。 */
    public static void tickStealthPoolRegen(LivingEntity entity) {
        ItemStack cloakStack = cloak(entity);
        if (!isStealthCloak(cloakStack)) {
            return;
        }
        if (entity.tickCount % STEALTH_POOL_REGEN_INTERVAL_TICKS != 0) {
            return;
        }
        float t = getStealthPool(cloakStack);
        if (t < STEALTH_POOL_T_MAX) {
            setStealthPool(cloakStack, t + 1.0F);
        }
    }

    public static long getStealthCooldownUntil(ItemStack cloakStack) {
        if (!isStealthCloak(cloakStack)) {
            return 0L;
        }
        CompoundTag tag = cloakStack.getTag();
        if (tag == null || !tag.contains(STEALTH_COOLDOWN_UNTIL_TAG)) {
            return 0L;
        }
        return tag.getLong(STEALTH_COOLDOWN_UNTIL_TAG);
    }

    public static boolean isStealthOnCooldown(ItemStack cloakStack, long gameTime) {
        return gameTime < getStealthCooldownUntil(cloakStack);
    }

    public static void startStealthCooldown(ItemStack cloakStack, long gameTime, int cooldownTicks) {
        if (!isStealthCloak(cloakStack)) {
            return;
        }
        long until = gameTime + Math.max(0, cooldownTicks);
        cloakStack.getOrCreateTag().putLong(STEALTH_COOLDOWN_UNTIL_TAG, until);
    }

    public static void startStealthCooldown(LivingEntity entity) {
        startStealthCooldown(
                cloak(entity), entity.level().getGameTime(), STEALTH_COOLDOWN_TICKS);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerStealthCloakArmor(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TieredBulletproofGeoArmorItem.appendBulletproofTooltipLines(
                tooltip, null, this.getLevel(), TieredBulletproofGeoArmorItem.styleForTier(BulletproofArmorTier.THIRD));
        tooltip.add(Component.translatable("tooltip.tacz_morelevel.stealth_cloak.skill"));
        if (this.getType() == ArmorItem.Type.CHESTPLATE && level != null) {
            float pool = getStealthPool(stack);
            tooltip.add(
                    Component.translatable(
                                    "tooltip.tacz_morelevel.stealth_cloak.pool",
                                    String.format("%.1f", pool),
                                    String.format("%.0f", STEALTH_POOL_T_MAX))
                            .withStyle(ChatFormatting.DARK_AQUA));
            if (stack.hasTag()) {
                long until = getStealthCooldownUntil(stack);
                long now = level.getGameTime();
                if (now < until) {
                    int sec = (int) Math.ceil((until - now) / 20.0);
                    tooltip.add(
                            Component.translatable(
                                            "tooltip.tacz_morelevel.stealth_cloak.cooldown", sec)
                                    .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
