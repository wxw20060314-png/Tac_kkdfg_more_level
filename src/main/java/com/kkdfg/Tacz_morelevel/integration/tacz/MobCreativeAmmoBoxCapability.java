package com.kkdfg.Tacz_morelevel.integration.tacz;

import com.kkdfg.Tacz_morelevel.Tacz_MoreLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Tacz_MoreLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MobCreativeAmmoBoxCapability implements ICapabilityProvider, INBTSerializable<Tag> {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Tacz_MoreLevel.MOD_ID, "mob_creative_ammo_box");

    private final LazyOptional<IItemHandler> itemHandler;

    public MobCreativeAmmoBoxCapability(MobCreativeAmmoBoxHolder holder) {
        this.itemHandler = LazyOptional.of(() -> new MobCreativeAmmoBoxHandler(holder.mobCreativeAmmoBox()));
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof MobCreativeAmmoBoxHolder holder) {
            event.addCapability(ID, new MobCreativeAmmoBoxCapability(holder));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, this.itemHandler);
    }

    @Override
    public Tag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(Tag nbt) {}
}
