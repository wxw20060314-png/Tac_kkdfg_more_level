package com.kkdfg.Tacz_morelevel.entity.companion.inventory;

import com.kkdfg.Tacz_morelevel.entity.companion.AbstractFriendlyCompanionEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** {@link CompanionInventory} {@link IItemHandler} */
public final class CompanionInventoryCapabilityProvider implements ICapabilityProvider, INBTSerializable<Tag> {

    private final LazyOptional<IItemHandler> itemHandler;

    public CompanionInventoryCapabilityProvider(AbstractFriendlyCompanionEntity companion) {
        this.itemHandler =
                LazyOptional.of(() -> new CompanionInventoryItemHandler(companion.getCompanionInventory()));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, this.itemHandler);
    }

    
    @Override
    public Tag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {}
}
