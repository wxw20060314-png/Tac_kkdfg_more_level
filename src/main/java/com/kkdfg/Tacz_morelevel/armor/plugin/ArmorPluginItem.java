package com.kkdfg.Tacz_morelevel.armor.plugin;

import java.util.Arrays;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/**
 * Base item for plates / goggles installed onto {@link com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor}.
 * Allowed {@link ArmorItem.Type}s are soft constraints for later workbench and gameplay events.
 * Default is all four armor pieces.
 */
public abstract class ArmorPluginItem extends Item {

    private static final ArmorItem.Type[] ALL_TYPES = {
            ArmorItem.Type.HELMET,
            ArmorItem.Type.CHESTPLATE,
            ArmorItem.Type.LEGGINGS,
            ArmorItem.Type.BOOTS
    };

    private final ArmorItem.Type[] allowedTypes;

    protected ArmorPluginItem(Properties properties) {
        this(properties, ALL_TYPES);
    }

    protected ArmorPluginItem(Properties properties, ArmorItem.Type... allowedTypes) {
        super(properties);
        if (allowedTypes == null || allowedTypes.length == 0) {
            this.allowedTypes = Arrays.copyOf(ALL_TYPES, ALL_TYPES.length);
        } else {
            this.allowedTypes = Arrays.copyOf(allowedTypes, allowedTypes.length);
        }
    }

    public ArmorItem.Type[] getAllowedTypes() {
        return Arrays.copyOf(this.allowedTypes, this.allowedTypes.length);
    }

    public boolean canInstallOn(ArmorItem.Type type) {
        if (type == null) {
            return false;
        }
        for (ArmorItem.Type allowed : this.allowedTypes) {
            if (allowed == type) {
                return true;
            }
        }
        return false;
    }
}
