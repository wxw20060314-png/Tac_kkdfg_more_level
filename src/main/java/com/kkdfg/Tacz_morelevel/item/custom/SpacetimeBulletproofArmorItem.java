package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
 * 时空防弹套：TaCZ 防弹档位 7；Gecko 模型见 {@code geo/armor/spacetime_bulletproof.geo.json}。
 * 传说技能：站立+传说键视线记点；蹲下+传说键回传（胸甲 TTL 冷却，见 {@link #BLINK_COOLDOWN_TTL_MAX}）。
 * <p>全套夜视：按键开关，见 {@link com.kkdfg.Tacz_morelevel.armor.skill.ArmorNightVisionHandler}。
 */
public class SpacetimeBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    public static final String BLINK_COOLDOWN_TTL_TAG = "SpacetimeBlinkCooldownTtl";
    public static final String WAYPOINT_DIM_TAG = "SpacetimeWaypointDim";
    public static final String WAYPOINT_X_TAG = "SpacetimeWaypointX";
    public static final String WAYPOINT_Y_TAG = "SpacetimeWaypointY";
    public static final String WAYPOINT_Z_TAG = "SpacetimeWaypointZ";

    public static final int FULL_SET_PIECES = 4;
    /** 回传冷却：5 秒。 */
    public static final int BLINK_COOLDOWN_TTL_MAX = 100;

    public static final Style NAME_STYLE = Style.EMPTY.withColor(ChatFormatting.BLUE).withBold(true);

    private static final Style SKILL_TOOLTIP_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public record Waypoint(ResourceLocation dimension, double x, double y, double z) {}

    public SpacetimeBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.SEVENTH_BULLETPROOF, type, properties);
        this.setLevel(7);
    }

    public static int countEquippedPieces(LivingEntity entity) {
        int count = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof SpacetimeBulletproofArmorItem) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return countEquippedPieces(entity) >= FULL_SET_PIECES;
    }

    public static int getBlinkCooldownTtl(ItemStack chestStack) {
        if (!(chestStack.getItem() instanceof SpacetimeBulletproofArmorItem)) {
            return 0;
        }
        CompoundTag tag = chestStack.getTag();
        return tag != null && tag.contains(BLINK_COOLDOWN_TTL_TAG) ? tag.getInt(BLINK_COOLDOWN_TTL_TAG) : 0;
    }

    public static boolean isBlinkOnCooldown(ItemStack chestStack) {
        return getBlinkCooldownTtl(chestStack) > 0;
    }

    public static void setBlinkCooldownTtl(ItemStack chestStack, int ttl) {
        if (!(chestStack.getItem() instanceof SpacetimeBulletproofArmorItem)) {
            return;
        }
        int clamped = Mth.clamp(ttl, 0, BLINK_COOLDOWN_TTL_MAX);
        if (clamped <= 0) {
            CompoundTag tag = chestStack.getTag();
            if (tag != null) {
                tag.remove(BLINK_COOLDOWN_TTL_TAG);
                if (tag.isEmpty()) {
                    chestStack.setTag(null);
                }
            }
            return;
        }
        chestStack.getOrCreateTag().putInt(BLINK_COOLDOWN_TTL_TAG, clamped);
    }

    public static boolean hasWaypoint(ItemStack chestStack) {
        return readWaypoint(chestStack).isPresent();
    }

    public static Optional<Waypoint> readWaypoint(ItemStack chestStack) {
        if (!(chestStack.getItem() instanceof SpacetimeBulletproofArmorItem)) {
            return Optional.empty();
        }
        CompoundTag tag = chestStack.getTag();
        if (tag == null
                || !tag.contains(WAYPOINT_DIM_TAG)
                || !tag.contains(WAYPOINT_X_TAG)
                || !tag.contains(WAYPOINT_Y_TAG)
                || !tag.contains(WAYPOINT_Z_TAG)) {
            return Optional.empty();
        }
        ResourceLocation dim = ResourceLocation.tryParse(tag.getString(WAYPOINT_DIM_TAG));
        if (dim == null) {
            return Optional.empty();
        }
        return Optional.of(
                new Waypoint(
                        dim,
                        tag.getDouble(WAYPOINT_X_TAG),
                        tag.getDouble(WAYPOINT_Y_TAG),
                        tag.getDouble(WAYPOINT_Z_TAG)));
    }

    public static void writeWaypoint(
            ItemStack chestStack, ResourceLocation dimension, double x, double y, double z) {
        if (!(chestStack.getItem() instanceof SpacetimeBulletproofArmorItem)) {
            return;
        }
        CompoundTag tag = chestStack.getOrCreateTag();
        tag.putString(WAYPOINT_DIM_TAG, dimension.toString());
        tag.putDouble(WAYPOINT_X_TAG, x);
        tag.putDouble(WAYPOINT_Y_TAG, y);
        tag.putDouble(WAYPOINT_Z_TAG, z);
    }

    private static void tickBlinkCooldown(ItemStack chestStack) {
        int ttl = getBlinkCooldownTtl(chestStack);
        if (ttl > 0) {
            setBlinkCooldownTtl(chestStack, ttl - 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || this.getType() != Type.CHESTPLATE) {
            return;
        }
        // 仅穿在胸甲槽时倒数冷却；手上/背包不减 TTL
        if (!(entity instanceof LivingEntity living)
                || living.getItemBySlot(EquipmentSlot.CHEST) != stack) {
            return;
        }
        tickBlinkCooldown(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerSpacetimeBulletproofArmor(consumer);
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
                Component.translatable("tooltip.tacz_morelevel.spacetime_bulletproof.blink")
                        .withStyle(SKILL_TOOLTIP_STYLE));
        TieredBulletproofGeoArmorItem.appendBulletproofTooltipLines(
                tooltip, null, this.getLevel(), NAME_STYLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
