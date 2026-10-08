package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
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
 * 潜行七级套：与隐身斗篷平行的独立系统；档位 7；外观见 {@code geo/armor/stealth_seventh.geo.json}。
 * 开关 / 冷却 / 池均在胸甲 NBT；池上限 100、破隐冷却 8s。
 * 头盔可按键夜视（见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler}）。
 * 技能见 {@link com.kkdfg.Tacz_morelevel.armor.skill.impl.StealthSeventhSkill}。
 */
public class StealthSeventhArmorItem extends BulletproofArmor implements GeoItem {

    public static final int FULL_SET_PIECES = 4;

    /** 破隐后冷却（8s）。 */
    public static final int STEALTH_COOLDOWN_TICKS = 160;

    public static final String STEALTH_COOLDOWN_UNTIL_TAG = "StealthSeventhCooldownUntil";

    /** 隐身开关（胸甲）。无此键或 false = 默认关闭。 */
    public static final String STEALTH_ENABLED_TAG = "StealthSeventhEnabled";

    /** 破隐池 t：上限 100；每 {@link #STEALTH_POOL_REGEN_INTERVAL_TICKS} tick +1。 */
    public static final float STEALTH_POOL_T_MAX = 100.0F;
    public static final int STEALTH_POOL_REGEN_INTERVAL_TICKS = 5;
    public static final String STEALTH_POOL_TAG = "StealthSeventhPoolT";

    /** 物品名：灰色加粗。 */
    public static final Style NAME_STYLE =
            Style.EMPTY.withColor(ChatFormatting.GRAY).withBold(true);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public StealthSeventhArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack)).withStyle(NAME_STYLE);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int n = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof StealthSeventhArmorItem) {
                n++;
            }
        }
        return n;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    public static ItemStack chest(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST);
    }

    public static boolean isStealthSeventhChest(ItemStack stack) {
        return stack.getItem() instanceof StealthSeventhArmorItem armor
                && armor.getType() == ArmorItem.Type.CHESTPLATE;
    }

    public static boolean isStealthEnabled(ItemStack chestStack) {
        if (!isStealthSeventhChest(chestStack)) {
            return false;
        }
        CompoundTag tag = chestStack.getTag();
        return tag != null && tag.getBoolean(STEALTH_ENABLED_TAG);
    }

    public static void setStealthEnabled(ItemStack chestStack, boolean enabled) {
        if (!isStealthSeventhChest(chestStack)) {
            return;
        }
        if (enabled) {
            chestStack.getOrCreateTag().putBoolean(STEALTH_ENABLED_TAG, true);
            return;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag != null) {
            tag.remove(STEALTH_ENABLED_TAG);
            if (tag.isEmpty()) {
                chestStack.setTag(null);
            }
        }
    }

    public static float getStealthPool(ItemStack chestStack) {
        if (!isStealthSeventhChest(chestStack)) {
            return 0.0F;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null || !tag.contains(STEALTH_POOL_TAG)) {
            return STEALTH_POOL_T_MAX;
        }
        return Math.min(STEALTH_POOL_T_MAX, Math.max(0.0F, tag.getFloat(STEALTH_POOL_TAG)));
    }

    public static void setStealthPool(ItemStack chestStack, float t) {
        if (!isStealthSeventhChest(chestStack)) {
            return;
        }
        float clamped = Math.min(STEALTH_POOL_T_MAX, Math.max(0.0F, t));
        if (clamped >= STEALTH_POOL_T_MAX) {
            CompoundTag tag = chestStack.getTag();
            if (tag != null) {
                tag.remove(STEALTH_POOL_TAG);
                if (tag.isEmpty()) {
                    chestStack.setTag(null);
                }
            }
            return;
        }
        chestStack.getOrCreateTag().putFloat(STEALTH_POOL_TAG, clamped);
    }

    public static void fillStealthPool(ItemStack chestStack) {
        setStealthPool(chestStack, STEALTH_POOL_T_MAX);
    }

    public static void tickStealthPoolRegen(LivingEntity entity) {
        ItemStack chestStack = chest(entity);
        if (!isStealthSeventhChest(chestStack)) {
            return;
        }
        if (entity.tickCount % STEALTH_POOL_REGEN_INTERVAL_TICKS != 0) {
            return;
        }
        float t = getStealthPool(chestStack);
        if (t < STEALTH_POOL_T_MAX) {
            setStealthPool(chestStack, t + 1.0F);
        }
    }

    public static long getStealthCooldownUntil(ItemStack chestStack) {
        if (!isStealthSeventhChest(chestStack)) {
            return 0L;
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null || !tag.contains(STEALTH_COOLDOWN_UNTIL_TAG)) {
            return 0L;
        }
        return tag.getLong(STEALTH_COOLDOWN_UNTIL_TAG);
    }

    public static boolean isStealthOnCooldown(ItemStack chestStack, long gameTime) {
        return gameTime < getStealthCooldownUntil(chestStack);
    }

    public static void startStealthCooldown(ItemStack chestStack, long gameTime, int cooldownTicks) {
        if (!isStealthSeventhChest(chestStack)) {
            return;
        }
        long until = gameTime + Math.max(0, cooldownTicks);
        chestStack.getOrCreateTag().putLong(STEALTH_COOLDOWN_UNTIL_TAG, until);
    }

    public static void startStealthCooldown(LivingEntity entity) {
        startStealthCooldown(chest(entity), entity.level().getGameTime(), STEALTH_COOLDOWN_TICKS);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerStealthSeventhArmor(consumer);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.stealth_seventh.legendary")
                        .withStyle(NAME_STYLE));
        tooltip.add(
                Component.translatable("tooltip.tacz_morelevel.stealth_seventh.legendary_2")
                        .withStyle(NAME_STYLE));
        TieredBulletproofGeoArmorItem.appendBulletproofTooltipLines(
                tooltip, null, this.getLevel(), NAME_STYLE);
        if (this.getType() == ArmorItem.Type.CHESTPLATE && level != null) {
            float pool = getStealthPool(stack);
            tooltip.add(
                    Component.translatable(
                                    "tooltip.tacz_morelevel.stealth_seventh.pool",
                                    String.format("%.1f", pool),
                                    String.format("%.0f", STEALTH_POOL_T_MAX))
                            .withStyle(ChatFormatting.DARK_GRAY));
            if (stack.hasTag()) {
                long until = getStealthCooldownUntil(stack);
                long now = level.getGameTime();
                if (now < until) {
                    int sec = (int) Math.ceil((until - now) / 20.0);
                    tooltip.add(
                            Component.translatable(
                                            "tooltip.tacz_morelevel.stealth_seventh.cooldown", sec)
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
