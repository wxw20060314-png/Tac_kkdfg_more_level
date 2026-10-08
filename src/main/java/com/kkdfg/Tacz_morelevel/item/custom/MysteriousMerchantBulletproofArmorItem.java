package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.ModItems;
import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
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
 * 神秘商人防弹套：数值同四级甲（{@link ModArmorMaterials#FOURTH_BULLETPROOF}、防弹档位 4）；
 * Gecko 模型见 {@code geo/armor/mysterious_merchant_bulletproof.geo.json}。
 */
public class MysteriousMerchantBulletproofArmorItem extends BulletproofArmor implements GeoItem {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MysteriousMerchantBulletproofArmorItem(ArmorItem.Type type, Properties properties) {
        super(ModArmorMaterials.FOURTH_BULLETPROOF, type, properties);
        this.setLevel(4);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerMysteriousMerchantBulletproofArmor(consumer);
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
        TieredBulletproofGeoArmorItem.appendBulletproofTooltipLines(
                tooltip, null, this.getLevel(), TieredBulletproofGeoArmorItem.styleForTier(BulletproofArmorTier.FOURTH));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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
    public static boolean hasFullMysteriousMerchantSet(LivingEntity living) {
        return armorSlotsMatchItems(
            List.of(
                living.getItemBySlot(EquipmentSlot.HEAD),
                living.getItemBySlot(EquipmentSlot.CHEST),
                living.getItemBySlot(EquipmentSlot.LEGS),
                living.getItemBySlot(EquipmentSlot.FEET)),
            List.of(
                ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_HELMET.get(),
                ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_CHESTPLATE.get(),
                ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_LEGGINGS.get(),
                ModItems.MYSTERIOUS_MERCHANT_BULLETPROOF_BOOTS.get()));
    }
    public static void tickFullSetEffect(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity.level().getGameTime() % 10 != 0) {
            return;
        }
        if (!hasFullMysteriousMerchantSet(entity)) {
            return;
        }
        entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 1, false, false));
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || this.getType() != Type.CHESTPLATE) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            tickFullSetEffect(living);
        }
    }
}
