package com.kkdfg.Tacz_morelevel.menu;

import com.kkdfg.Tacz_morelevel.armor.plugin.ArmorPluginItem;
import com.kkdfg.Tacz_morelevel.block.ModBlocks;
import com.kkdfg.Tacz_morelevel.item.custom.BulletproofArmor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * a1 + b1 → a2 + b2。
 * <ul>
 *   <li>a1 无插件且 b1 有插件 → 装上（a2=盔甲+插件，b2=空）</li>
 *   <li>a1 有插件且 b1 空 → 取出（a2=空槽盔甲，b2=插件）</li>
 * </ul>
 */
public final class PluginWorkbenchMenu extends AbstractContainerMenu {

    public static final int SLOT_A1 = 0;
    public static final int SLOT_B1 = 1;
    public static final int SLOT_A2 = 2;
    public static final int SLOT_B2 = 3;

    private final ContainerLevelAccess access;
    private final SimpleContainer inputSlots =
            new SimpleContainer(2) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    PluginWorkbenchMenu.this.slotsChanged(this);
                }
            };
    private final SimpleContainer resultSlots = new SimpleContainer(2);
    private boolean updatingResults;

    public PluginWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(
                containerId,
                playerInventory,
                ContainerLevelAccess.create(playerInventory.player.level(), extraData.readBlockPos()));
    }

    public PluginWorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.PLUGIN_WORKBENCH_MENU.get(), containerId);
        this.access = access;

        // Matches textures/gui/container/plugin_workbench.png (a1 + b1 → a2 + b2).
        this.addSlot(new ArmorInputSlot(this.inputSlots, 0, 19, 41));
        this.addSlot(new PluginInputSlot(this.inputSlots, 1, 55, 41));
        this.addSlot(new ResultSlot(this.resultSlots, 0, 101, 41));
        this.addSlot(new ResultSlot(this.resultSlots, 1, 137, 41));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.inputSlots) {
            this.createResult();
        }
        super.slotsChanged(container);
    }

    private void createResult() {
        if (this.updatingResults) {
            return;
        }
        ItemStack a1 = this.inputSlots.getItem(0);
        ItemStack b1 = this.inputSlots.getItem(1);
        ItemStack a2 = ItemStack.EMPTY;
        ItemStack b2 = ItemStack.EMPTY;

        if (a1.getItem() instanceof BulletproofArmor) {
            ItemStack installed = BulletproofArmor.getPlugin(a1, 0);
            if (!installed.isEmpty() && b1.isEmpty()) {
                a2 = a1.copyWithCount(1);
                BulletproofArmor.setPlugin(a2, 0, ItemStack.EMPTY);
                b2 = installed.copy();
            } else if (installed.isEmpty()
                    && !b1.isEmpty()
                    && BulletproofArmor.mayInsertPlugin(a1, b1)) {
                a2 = a1.copyWithCount(1);
                ItemStack one = b1.copy();
                one.setCount(1);
                BulletproofArmor.setPlugin(a2, 0, one);
            }
        }

        this.updatingResults = true;
        this.resultSlots.setItem(0, a2);
        this.resultSlots.setItem(1, b2);
        this.updatingResults = false;
        this.broadcastChanges();
    }

    private void consumeInputs() {
        ItemStack a1 = this.inputSlots.getItem(0);
        ItemStack b1 = this.inputSlots.getItem(1);
        if (!(a1.getItem() instanceof BulletproofArmor)) {
            return;
        }
        ItemStack installed = BulletproofArmor.getPlugin(a1, 0);
        boolean install = installed.isEmpty() && !b1.isEmpty();
        a1.shrink(1);
        if (install) {
            b1.shrink(1);
        }
        this.inputSlots.setChanged();
    }

    /** Called after a result stack has already been removed from its slot. */
    void onResultTaken(Player player, int resultIndex) {
        int otherIndex = resultIndex == 0 ? 1 : 0;
        ItemStack other = this.resultSlots.getItem(otherIndex).copy();
        if (!other.isEmpty()) {
            this.resultSlots.setItem(otherIndex, ItemStack.EMPTY);
            if (!player.getInventory().add(other)) {
                player.drop(other, false);
            }
        }
        this.consumeInputs();
        this.createResult();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack remaining = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return remaining;
        }
        ItemStack stack = slot.getItem();
        remaining = stack.copy();
        if (index == SLOT_A2 || index == SLOT_B2) {
            int resultIndex = index == SLOT_A2 ? 0 : 1;
            if (!this.moveItemStackTo(stack, 4, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, remaining);
        } else if (index == SLOT_A1 || index == SLOT_B1) {
            if (!this.moveItemStackTo(stack, 4, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stack.getItem() instanceof BulletproofArmor) {
                if (!this.moveItemStackTo(stack, SLOT_A1, SLOT_A1 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof ArmorPluginItem) {
                if (!this.moveItemStackTo(stack, SLOT_B1, SLOT_B1 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 31) {
                if (!this.moveItemStackTo(stack, 31, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 4, 31, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return remaining;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.PLUGIN_WORKBENCH.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.inputSlots));
    }

    private static final class ArmorInputSlot extends Slot {
        ArmorInputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof BulletproofArmor;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class PluginInputSlot extends Slot {
        PluginInputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ArmorPluginItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private final class ResultSlot extends Slot {
        private final int resultIndex;

        ResultSlot(Container container, int resultIndex, int x, int y) {
            super(container, resultIndex, x, y);
            this.resultIndex = resultIndex;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            PluginWorkbenchMenu.this.onResultTaken(player, this.resultIndex);
            super.onTake(player, stack);
        }
    }
}
