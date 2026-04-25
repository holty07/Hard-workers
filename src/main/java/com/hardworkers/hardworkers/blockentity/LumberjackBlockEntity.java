package com.hardworkers.hardworkers.blockentity;

import com.hardworkers.hardworkers.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import java.util.function.Supplier;

/**
 * Single-slot (max 64 items) storage for logs harvested by the lumberjack.
 * Implements {@link Container} for vanilla hopper compatibility.
 * Exposes {@link IItemHandler} via capability for other mods' pipes.
 */
public class LumberjackBlockEntity extends BlockEntity implements Container {

    public static final int CAPACITY = 64;

    private ItemStack stored = ItemStack.EMPTY;

    // Cached wrapper — created once per block entity instance.
    private final IItemHandler itemHandler = new InvWrapper(this);

    public LumberjackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUMBERJACK_BLOCK_ENTITY.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * Tries to insert {@code incoming} into the slot.
     *
     * @return the leftover stack (empty if everything was accepted).
     */
    public ItemStack insertItem(ItemStack incoming) {
        if (incoming.isEmpty()) return ItemStack.EMPTY;

        if (stored.isEmpty()) {
            int accept = Math.min(incoming.getCount(), CAPACITY);
            stored = incoming.copyWithCount(accept);
            setChanged();
            int leftover = incoming.getCount() - accept;
            return leftover > 0 ? incoming.copyWithCount(leftover) : ItemStack.EMPTY;
        }

        if (stored.getItem() == incoming.getItem()) {
            int space = CAPACITY - stored.getCount();
            if (space > 0) {
                int accept = Math.min(space, incoming.getCount());
                stored.grow(accept);
                setChanged();
                int leftover = incoming.getCount() - accept;
                return leftover > 0 ? incoming.copyWithCount(leftover) : ItemStack.EMPTY;
            }
        }

        return incoming; // full or different item type
    }

    public boolean isFull() {
        return !stored.isEmpty() && stored.getCount() >= CAPACITY;
    }

    public Component getStorageStatus() {
        if (stored.isEmpty()) {
            return Component.literal("Storage: empty");
        }
        return Component.literal("Storage: " + stored.getCount() + "/" + CAPACITY
            + " " + stored.getHoverName().getString());
    }

    // -------------------------------------------------------------------------
    // Container (vanilla hopper compatibility)
    // -------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return stored.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? stored : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || stored.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = stored.split(amount);
        setChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack was = stored;
        stored = ItemStack.EMPTY;
        return was;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0) return;
        if (!stack.isEmpty() && stack.getCount() > CAPACITY) {
            stack = stack.copyWithCount(CAPACITY);
        }
        stored = stack;
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return CAPACITY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        stored = ItemStack.EMPTY;
        setChanged();
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!stored.isEmpty()) {
            tag.put("Stored", stored.save(registries));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Stored")) {
            stored = ItemStack.parseOptional(registries, tag.getCompound("Stored"));
        } else {
            stored = ItemStack.EMPTY;
        }
    }
}
