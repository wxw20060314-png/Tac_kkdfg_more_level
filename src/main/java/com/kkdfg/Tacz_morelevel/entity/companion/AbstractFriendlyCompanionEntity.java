package com.kkdfg.Tacz_morelevel.entity.companion;

import com.kkdfg.Tacz_morelevel.entity.companion.ai.CompanionSitWhenOrderedGoal;
import com.kkdfg.Tacz_morelevel.entity.companion.inventory.CompanionInventory;
import com.kkdfg.Tacz_morelevel.entity.companion.menu.CompanionInventoryMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * ???? {@link AbstractFriendEntity} ?????????????????Shift ????
 */
public abstract class AbstractFriendlyCompanionEntity extends AbstractFriendEntity implements MenuProvider {

    private static final EntityDataAccessor<Boolean> DATA_ORDERED_TO_SIT =
            SynchedEntityData.defineId(AbstractFriendlyCompanionEntity.class, EntityDataSerializers.BOOLEAN);

    private final CompanionInventory companionInventory = new CompanionInventory(this);

    protected AbstractFriendlyCompanionEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ORDERED_TO_SIT, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new CompanionSitWhenOrderedGoal(this));
    }

    public CompanionInventory getCompanionInventory() {
        return this.companionInventory;
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
        return new CompanionInventoryMenu(containerId, playerInventory, this);
    }

    public void openCompanionInventory(ServerPlayer player) {
        NetworkHooks.openScreen(player, this, buf -> buf.writeVarInt(this.getId()));
    }

    @Override
    public boolean isOrderedToSit() {
        return this.entityData.get(DATA_ORDERED_TO_SIT);
    }

    @Override
    public void setOrderedToSit(boolean orderedToSit) {
        if (this.level().isClientSide()) {
            return;
        }
        this.entityData.set(DATA_ORDERED_TO_SIT, orderedToSit);
        if (orderedToSit) {
            this.setPose(Pose.STANDING);
            this.getNavigation().stop();
            this.setTarget(null);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || this.isTameItem(stack) && !this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (stack.isEmpty()) {
                    if (player.isShiftKeyDown()) {
                        this.setOrderedToSit(!this.isOrderedToSit());
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget(null);
                        return InteractionResult.SUCCESS;
                    }
                    if (player instanceof ServerPlayer serverPlayer) {
                        this.openCompanionInventory(serverPlayer);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return super.mobInteract(player, hand);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sitting", this.isOrderedToSit());
        compound.put("CompanionInventory", this.companionInventory.save());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (!this.level().isClientSide()) {
            this.setOrderedToSit(compound.getBoolean("Sitting"));
        }
        if (compound.contains("CompanionInventory", 10)) {
            this.companionInventory.load(compound.getCompound("CompanionInventory"));
        }
    }
}
