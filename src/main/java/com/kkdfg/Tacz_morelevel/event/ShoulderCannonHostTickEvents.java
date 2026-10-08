package com.kkdfg.Tacz_morelevel.event;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import com.kkdfg.Tacz_morelevel.armor.plugin.shoulder_cannon.AbstractShoulderCannonPluginItem;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 任意活物：胸甲肩炮插件每 tick 确保实体 / 扣重生冷却。 */
@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ShoulderCannonHostTickEvents {

    private ShoulderCannonHostTickEvents() {}

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity host = event.getEntity();
        if (host.level().isClientSide()) {
            return;
        }
        for (ItemStack armor : host.getArmorSlots()) {
            if (!(armor.getItem() instanceof BulletproofArmor)) {
                continue;
            }
            int slots = BulletproofArmor.pluginSlotCount(armor);
            for (int i = 0; i < slots; i++) {
                ItemStack plugin = BulletproofArmor.getPlugin(armor, i);
                if (plugin.getItem() instanceof AbstractShoulderCannonPluginItem cannonPlugin) {
                    cannonPlugin.tickOnHost(host, armor, i, plugin);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCannonKilled(ShoulderCannonKilledEvent event) {
        LivingEntity host = event.getHost();
        int slot = event.getCannon().getPluginSlot();
        ItemStack chest = host.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof BulletproofArmor)) {
            return;
        }
        ItemStack plugin = BulletproofArmor.getPlugin(chest, slot);
        if (plugin.getItem() instanceof AbstractShoulderCannonPluginItem cannonPlugin) {
            cannonPlugin.onCannonKilled(plugin);
            BulletproofArmor.setPlugin(chest, slot, plugin);
        }
    }
}
