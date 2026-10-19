package com.kkdfg.Tacz_morelevel.item.custom;

import com.kkdfg.Tacz_morelevel.item.client.ModGeoItemClientExtensions;
import com.kkdfg.Tacz_morelevel.item.medicine.ChannelMedicineItem;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineContext;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineFinishHandler;
import com.kkdfg.Tacz_morelevel.item.medicine.MedicineSpec;
import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 盔甲维修包：读条用药流程，结束时修复身上每件盔甲 25% 最大耐久。 */
public final class ArmorRepairKitItem extends ChannelMedicineItem implements GeoItem, MedicineFinishHandler {

    private static final float REPAIR_FRACTION = 0.25F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ArmorRepairKitItem(Properties properties, MedicineSpec spec) {
        super(properties, spec);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ModGeoItemClientExtensions.registerArmorRepairKit(consumer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected boolean canStartUse(Level level, Player player, ItemStack stack) {
        return super.canStartUse(level, player, stack) && hasDamagedArmor(player);
    }

    @Override
    public Result onMedicineFinish(MedicineContext context) {
        repairWornArmor(context.player());
        return Result.PASS;
    }

    private static boolean hasDamagedArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isDamageableItem() && armor.getDamageValue() > 0) {
                return true;
            }
        }
        return false;
    }

    private static void repairWornArmor(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack armor = player.getItemBySlot(slot);
            if (!armor.isDamageableItem() || armor.getMaxDamage() <= 0 || armor.getDamageValue() <= 0) {
                continue;
            }
            int repair = Math.max(1, (int) Math.ceil(armor.getMaxDamage() * REPAIR_FRACTION));
            armor.setDamageValue(Math.max(0, armor.getDamageValue() - repair));
        }
    }
}
