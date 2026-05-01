package com.hardworkers.hardworkers.blockentity;

import com.hardworkers.hardworkers.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class MinerBlockEntity extends BlockEntity implements Container {

    public static final int SLOT_COUNT = 27;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final IItemHandler itemHandler = new InvWrapper(this);

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MINER_BLOCK_ENTITY.get(), pos, state);
    }

    public IItemHandler getItemHandler() { return itemHandler; }

    public ItemStack insertItem(ItemStack incoming) {
        if (incoming.isEmpty()) return ItemStack.EMPTY;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty()
                    && slot.getItem() == incoming.getItem()
                    && ItemStack.isSameItemSameComponents(slot, incoming)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, incoming.getCount());
                    slot.grow(toAdd);
                    setChanged();
                    incoming = incoming.copyWithCount(incoming.getCount() - toAdd);
                    if (incoming.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (items.get(i).isEmpty()) {
                int toStore = Math.min(incoming.getCount(), incoming.getMaxStackSize());
                items.set(i, incoming.copyWithCount(toStore));
                setChanged();
                int leftover = incoming.getCount() - toStore;
                return leftover > 0 ? incoming.copyWithCount(leftover) : ItemStack.EMPTY;
            }
        }
        return incoming;
    }

    public boolean isFull() {
        for (ItemStack stack : items) {
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) return false;
        }
        return true;
    }

    public Component getStorageStatus() {
        int usedSlots = 0, totalItems = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) { usedSlots++; totalItems += stack.getCount(); }
        }
        if (usedSlots == 0) return Component.literal("Storage: empty");
        return Component.literal("Storage: " + usedSlots + "/" + SLOT_COUNT + " slots  (" + totalItems + " items)");
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { for (ItemStack s : items) { if (!s.isEmpty()) return false; } return true; }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack r = ContainerHelper.removeItem(items, slot, amount); if (!r.isEmpty()) setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { if (slot >= 0 && slot < SLOT_COUNT) { items.set(slot, stack); setChanged(); } }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY); setChanged(); }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
