package com.kkdfg.Tacz_morelevel.block.entity;

import com.kkdfg.Tacz_morelevel.advancement.BlackMarketAdvancements;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketBeaconHelper;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketLevelDisplay;
import com.kkdfg.Tacz_morelevel.blackmarket.BlackMarketSavedData;
import com.kkdfg.Tacz_morelevel.block.ModBlockEntities;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketContactMenu;
import com.kkdfg.Tacz_morelevel.menu.BlackMarketTradeOffer;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class BlackMarketContactBlockEntity extends BlockEntity implements GeoBlockEntity, MenuProvider {

    private static final RawAnimation ANIMATION = RawAnimation.begin().thenLoop("animation");
    private static final String TAG_MARKET_LEVEL = "MarketLevel";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int syncedMarketLevel = 1;

    public BlackMarketContactBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACK_MARKET_CONTACT_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlackMarketContactBlockEntity blockEntity) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlackMarketSavedData marketData = BlackMarketSavedData.get(serverLevel);
        marketData.serverTick(serverLevel);
        int localLevel = BlackMarketBeaconHelper.beaconLevelToMarketLevel(
                BlackMarketBeaconHelper.scanMaxActivatedBeaconLevel(serverLevel, pos));
        blockEntity.updateLocalMarketLevel(serverLevel, localLevel);
    }

    private void updateLocalMarketLevel(ServerLevel serverLevel, int marketLevel) {
        int level = BlackMarketLevelDisplay.clampLevel(marketLevel);
        if (this.syncedMarketLevel == level) {
            return;
        }
        this.syncedMarketLevel = level;
        BlackMarketSavedData.get(serverLevel).ensureLevelTableReady(serverLevel, level);
        this.setChanged();
        serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    private BlackMarketSavedData marketData() {
        if (this.level instanceof ServerLevel serverLevel) {
            return BlackMarketSavedData.get(serverLevel);
        }
        throw new IllegalStateException("Black market data is only available on the server");
    }

    public void ensureInitialized(ServerLevel level) {
        BlackMarketSavedData.get(level).ensureInitialized(level);
    }

    public List<BlackMarketTradeOffer> activeOffers() {
        return this.marketData().offersForLevel(this.marketLevel());
    }

    public BlackMarketTradeOffer activeOffer(int index) {
        return this.marketData().offerForLevel(this.marketLevel(), index);
    }

    public ItemStack refreshCost() {
        return this.marketData().refreshCost();
    }

    public int manualRefreshPaidCount() {
        return this.marketData().manualRefreshPaidCount();
    }

    public void performManualRefresh(ServerLevel level) {
        BlackMarketSavedData.get(level).performManualRefresh(level, this.marketLevel());
    }

    public void writeMenuData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
        List<BlackMarketTradeOffer> offers = this.activeOffers();
        buf.writeVarInt(offers.size());
        for (BlackMarketTradeOffer offer : offers) {
            buf.writeItem(offer.input());
            buf.writeItem(offer.output());
        }
        buf.writeItem(this.refreshCost());
        buf.writeVarInt(this.manualRefreshPaidCount());
    }

    public int marketLevel() {
        return BlackMarketLevelDisplay.clampLevel(this.syncedMarketLevel);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_MARKET_LEVEL, this.syncedMarketLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_MARKET_LEVEL)) {
            this.syncedMarketLevel = BlackMarketLevelDisplay.clampLevel(tag.getInt(TAG_MARKET_LEVEL));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(TAG_MARKET_LEVEL, this.syncedMarketLevel);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag.contains(TAG_MARKET_LEVEL)) {
            this.syncedMarketLevel =
                    BlackMarketLevelDisplay.clampLevel(tag.getInt(TAG_MARKET_LEVEL));
        }
    }

    public static List<BlackMarketTradeOffer> readOffers(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<BlackMarketTradeOffer> offers = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            offers.add(new BlackMarketTradeOffer(buf.readItem(), buf.readItem()));
        }
        return offers;
    }

    @Override
    public Component getDisplayName() {
        return BlackMarketLevelDisplay.title(this.marketLevel());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.level() instanceof ServerLevel serverLevel) {
            this.ensureInitialized(serverLevel);
            BlackMarketAdvancements.onMenuOpened(serverPlayer, this.marketLevel());
        }
        return new BlackMarketContactMenu(containerId, playerInventory, this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "animation", 0, state -> {
                    state.setAnimation(ANIMATION);
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
