package com.kkdfg.Tacz_morelevel.integration.tacz;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

/**
 * 方向为 {@code null} 时只交出那一格创造弹药箱。
 * Forge 的 {@code LivingEntity.getCapability(ITEM_HANDLER, null)} 否则只给 6 格装备。
 */
public final class MobCreativeAmmoBoxReloadAccess {

    private final NonNullList<ItemStack> slot;
    private LazyOptional<IItemHandler> handler;

    public MobCreativeAmmoBoxReloadAccess(NonNullList<ItemStack> slot) {
        this.slot = slot;
        this.handler = this.create();
    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side, boolean alive) {
        if (alive && side == null && cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.handler.cast();
        }
        return null;
    }

    public void invalidate() {
        this.handler.invalidate();
    }

    public void revive() {
        this.handler = this.create();
    }

    private LazyOptional<IItemHandler> create() {
        return LazyOptional.of(() -> new MobCreativeAmmoBoxHandler(this.slot));
    }
}
