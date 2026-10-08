package com.kkdfg.Tacz_morelevel.armor.plugin.shoulder_cannon;

import com.kkdfg.Tacz_morelevel.armor.plugin.ArmorPluginItem;
import com.kkdfg.Tacz_morelevel.entity.custom.shoulder_cannon.AbstractShoulderCannonEntity;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 肩炮插件基类：仅胸甲。NBT 存绑定实体与重生冷却；每 tick 由宿主事件确保实体存在。
 */
public abstract class AbstractShoulderCannonPluginItem extends ArmorPluginItem {

    public static final String TAG_BOUND_CANNON = "BoundCannon";
    public static final String TAG_RESPAWN_COOLDOWN = "RespawnCooldown";

    /** 打空血后默认重生冷却。 */
    public static final int DEFAULT_RESPAWN_COOLDOWN_TICKS = 1200;

    protected AbstractShoulderCannonPluginItem(Properties properties) {
        super(properties, ArmorItem.Type.CHESTPLATE);
    }

    protected abstract AbstractShoulderCannonEntity createCannon(LivingEntity host);

    public void tickOnHost(LivingEntity host, ItemStack chestArmor, int pluginSlot, ItemStack plugin) {
        if (host.level().isClientSide()) {
            return;
        }
        int cooldown = getRespawnCooldown(plugin);
        if (cooldown > 0) {
            setRespawnCooldown(plugin, cooldown - 1);
            BulletproofArmor.setPlugin(chestArmor, pluginSlot, plugin);
            return;
        }
        UUID bound = getBoundCannonUuid(plugin);
        if (bound != null && findBoundCannon(host.level(), bound) != null) {
            return;
        }
        AbstractShoulderCannonEntity cannon = this.createCannon(host);
        if (cannon == null) {
            return;
        }
        cannon.bind(host, pluginSlot);
        cannon.snapToHeadRight(host);
        host.level().addFreshEntity(cannon);
        setBoundCannonUuid(plugin, cannon.getUUID());
        BulletproofArmor.setPlugin(chestArmor, pluginSlot, plugin);
    }

    public void onCannonKilled(ItemStack plugin) {
        setBoundCannonUuid(plugin, null);
        setRespawnCooldown(plugin, DEFAULT_RESPAWN_COOLDOWN_TICKS);
    }

    @Nullable
    public static UUID getBoundCannonUuid(ItemStack plugin) {
        CompoundTag tag = plugin.getTag();
        if (tag == null || !tag.hasUUID(TAG_BOUND_CANNON)) {
            return null;
        }
        return tag.getUUID(TAG_BOUND_CANNON);
    }

    public static void setBoundCannonUuid(ItemStack plugin, @Nullable UUID uuid) {
        if (uuid == null) {
            CompoundTag tag = plugin.getTag();
            if (tag != null) {
                tag.remove(TAG_BOUND_CANNON);
                if (tag.isEmpty()) {
                    plugin.setTag(null);
                }
            }
            return;
        }
        plugin.getOrCreateTag().putUUID(TAG_BOUND_CANNON, uuid);
    }

    public static int getRespawnCooldown(ItemStack plugin) {
        CompoundTag tag = plugin.getTag();
        if (tag == null) {
            return 0;
        }
        return Math.max(0, tag.getInt(TAG_RESPAWN_COOLDOWN));
    }

    public static void setRespawnCooldown(ItemStack plugin, int ticks) {
        if (ticks <= 0) {
            CompoundTag tag = plugin.getTag();
            if (tag != null) {
                tag.remove(TAG_RESPAWN_COOLDOWN);
                if (tag.isEmpty()) {
                    plugin.setTag(null);
                }
            }
            return;
        }
        plugin.getOrCreateTag().putInt(TAG_RESPAWN_COOLDOWN, ticks);
    }

    @Nullable
    private static AbstractShoulderCannonEntity findBoundCannon(Level level, UUID uuid) {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        Entity entity = server.getEntity(uuid);
        if (entity instanceof AbstractShoulderCannonEntity cannon && cannon.isAlive()) {
            return cannon;
        }
        return null;
    }
}
