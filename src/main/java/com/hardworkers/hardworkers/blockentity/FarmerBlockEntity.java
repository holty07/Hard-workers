package com.hardworkers.hardworkers.blockentity;

import com.hardworkers.hardworkers.block.FarmerBlock;
import com.hardworkers.hardworkers.block.FarmerTier;
import com.hardworkers.hardworkers.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/**
 * Storage block for the farmer worker.
 *
 * Server tick responsibilities:
 *   1. Keep all farmland within HYDRATION_RADIUS at maximum moisture so the
 *      block substitutes for a water source in a typical farm layout.
 *   2. Call randomTick on every crop in the 5×5 harvest area at the tier's
 *      growthBoostInterval so higher tiers accelerate plant growth.
 */
public class FarmerBlockEntity extends BlockEntity implements Container {

    public static final int SLOT_COUNT = 27;
    private static final int FARM_RADIUS     = 2;   // 5×5 harvest area
    private static final int HYDRATION_RADIUS = 3;  // slightly wider to cover farm edges

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final IItemHandler itemHandler = new InvWrapper(this);

    public FarmerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FARMER_BLOCK_ENTITY.get(), pos, state);
    }

    public IItemHandler getItemHandler() { return itemHandler; }

    // -------------------------------------------------------------------------
    // Server tick
    // -------------------------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, FarmerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Hydrate surrounding farmland every 20 ticks (well ahead of random-tick decay)
        if (level.getGameTime() % 20 == 0) {
            hydrateFarmland(level, pos);
        }

        FarmerTier tier = getTier(serverLevel, pos);
        if (tier.growthBoostInterval > 0 && level.getGameTime() % tier.growthBoostInterval == 0) {
            boostCropGrowth(serverLevel, pos);
        }
    }

    private static void hydrateFarmland(Level level, BlockPos center) {
        for (int dx = -HYDRATION_RADIUS; dx <= HYDRATION_RADIUS; dx++) {
            for (int dz = -HYDRATION_RADIUS; dz <= HYDRATION_RADIUS; dz++) {
                for (int dy = -2; dy <= 1; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (s.is(Blocks.FARMLAND) && s.getValue(FarmBlock.MOISTURE) < 7) {
                        level.setBlock(p, s.setValue(FarmBlock.MOISTURE, 7), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static void boostCropGrowth(ServerLevel level, BlockPos center) {
        for (int dx = -FARM_RADIUS; dx <= FARM_RADIUS; dx++) {
            for (int dz = -FARM_RADIUS; dz <= FARM_RADIUS; dz++) {
                for (int dy = -1; dy <= 3; dy++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    Block b = s.getBlock();
                    if (b instanceof BonemealableBlock bm && bm.isValidBonemealTarget(level, p, s)) {
                        bm.performBonemeal(level, level.random, p, s);
                    } else if (b instanceof NetherWartBlock) {
                        advanceAge(level, p, s, NetherWartBlock.AGE, 3);
                    } else if (b instanceof CocoaBlock) {
                        advanceAge(level, p, s, CocoaBlock.AGE, 2);
                    } else if (b instanceof SweetBerryBushBlock) {
                        advanceAge(level, p, s, SweetBerryBushBlock.AGE, 3);
                    }
                }
            }
        }
    }

    private static void advanceAge(ServerLevel level, BlockPos pos, BlockState state,
                                   IntegerProperty ageProp, int maxAge) {
        int age = state.getValue(ageProp);
        if (age < maxAge) {
            level.setBlock(pos, state.setValue(ageProp, age + 1), Block.UPDATE_CLIENTS);
        }
    }

    private static FarmerTier getTier(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.getBlock() instanceof FarmerBlock fb) return fb.getTier();
        return FarmerTier.WOOD;
    }

    // -------------------------------------------------------------------------
    // Storage API
    // -------------------------------------------------------------------------

    public ItemStack insertItem(ItemStack incoming) {
        if (incoming.isEmpty()) return ItemStack.EMPTY;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && slot.getItem() == incoming.getItem()
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
        for (ItemStack s : items) {
            if (s.isEmpty() || s.getCount() < s.getMaxStackSize()) return false;
        }
        return true;
    }

    public Component getStorageStatus() {
        int used = 0, total = 0;
        for (ItemStack s : items) { if (!s.isEmpty()) { used++; total += s.getCount(); } }
        if (used == 0) return Component.literal("Storage: empty");
        return Component.literal("Storage: " + used + "/" + SLOT_COUNT + " slots  (" + total + " items)");
    }

    // -------------------------------------------------------------------------
    // Container
    // -------------------------------------------------------------------------

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { for (ItemStack s : items) { if (!s.isEmpty()) return false; } return true; }
    @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack r = ContainerHelper.removeItem(items, slot, amount); if (!r.isEmpty()) setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { if (slot >= 0 && slot < SLOT_COUNT) { items.set(slot, stack); setChanged(); } }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY); setChanged(); }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

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
