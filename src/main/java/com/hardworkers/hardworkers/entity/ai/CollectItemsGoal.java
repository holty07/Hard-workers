package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.blockentity.FarmerBlockEntity;
import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import com.hardworkers.hardworkers.blockentity.MinerBlockEntity;
import com.hardworkers.hardworkers.blockentity.WarehouseBlockEntity;
import com.hardworkers.hardworkers.entity.WarehouseWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CollectItemsGoal extends Goal {

    private static final int SEARCH_RADIUS = 32;
    private static final double REACH_SQ   = 9.0;  // 3-block reach
    private static final int IDLE_COOLDOWN = 60;   // ticks to wait when nothing found

    private final WarehouseWorkerEntity worker;
    private BlockPos sourcePos = null;
    private int cooldown = 0;
    private BlockPos cachedSource = null;
    private int fullScanCooldown = 0;

    public CollectItemsGoal(WarehouseWorkerEntity worker) {
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos home = worker.getHomePosition();
        if (home == null || home.equals(BlockPos.ZERO)) return false;

        // Already carrying — must return to deposit
        if (!worker.carrying.isEmpty()) return true;

        if (cooldown-- > 0) return false;

        // Skip if warehouse is full
        WarehouseBlockEntity wbe = getWarehouse(home);
        if (wbe == null || wbe.isFull()) { cooldown = IDLE_COOLDOWN; return false; }

        sourcePos = findSource(worker.level(), home);
        if (sourcePos == null) { cooldown = IDLE_COOLDOWN; return false; }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos home = worker.getHomePosition();
        return home != null && !home.equals(BlockPos.ZERO)
            && (sourcePos != null || !worker.carrying.isEmpty());
    }

    @Override
    public void start() {
        if (!worker.carrying.isEmpty()) {
            navigateTo(worker.getHomePosition());
        } else if (sourcePos != null) {
            navigateTo(sourcePos);
        }
    }

    @Override
    public void tick() {
        BlockPos home = worker.getHomePosition();
        if (home == null) { stop(); return; }

        if (!worker.carrying.isEmpty()) {
            // Return home and deposit
            if (worker.blockPosition().distSqr(home) <= REACH_SQ) {
                depositItems(home);
                sourcePos = null;
                cooldown = 20;
                stop();
            } else if (!worker.getNavigation().isInProgress()) {
                navigateTo(home);
            }
        } else if (sourcePos != null) {
            // Head to source and collect
            if (worker.blockPosition().distSqr(sourcePos) <= REACH_SQ) {
                collectItems();
                if (worker.carrying.isEmpty()) {
                    // Source was empty — try again later
                    cachedSource = null;
                    sourcePos = null;
                    cooldown = IDLE_COOLDOWN;
                    stop();
                } else {
                    sourcePos = null;
                    navigateTo(home);
                }
            } else if (!worker.getNavigation().isInProgress()) {
                navigateTo(sourcePos);
            }
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
        sourcePos = null;
        worker.getNavigation().stop();
    }

    private void navigateTo(BlockPos pos) {
        worker.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0);
    }

    private void collectItems() {
        if (sourcePos == null) return;
        BlockEntity be = worker.level().getBlockEntity(sourcePos);
        IItemHandler handler = getItemHandler(be);
        if (handler == null) { sourcePos = null; return; }

        int limit = worker.getStacksPerTrip();
        int collected = 0;
        for (int slot = 0; slot < handler.getSlots() && collected < limit; slot++) {
            ItemStack stack = handler.extractItem(slot, 64, false);
            if (!stack.isEmpty()) {
                worker.carrying.add(stack);
                collected++;
            }
        }
        cachedSource = sourcePos;
    }

    private void depositItems(BlockPos home) {
        WarehouseBlockEntity wbe = getWarehouse(home);
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack stack : worker.carrying) {
            if (wbe != null) {
                ItemStack leftover = wbe.insertItem(stack);
                if (!leftover.isEmpty()) overflow.add(leftover);
            } else {
                overflow.add(stack);
            }
        }
        worker.carrying.clear();
        // Drop any overflow near the warehouse
        for (ItemStack stack : overflow) {
            worker.level().addFreshEntity(new ItemEntity(
                worker.level(),
                home.getX() + 0.5, home.getY() + 1.0, home.getZ() + 0.5,
                stack));
        }
    }

    private WarehouseBlockEntity getWarehouse(BlockPos pos) {
        BlockEntity be = worker.level().getBlockEntity(pos);
        return be instanceof WarehouseBlockEntity wbe ? wbe : null;
    }

    private BlockPos findSource(Level level, BlockPos home) {
        // Try cached source first (avoids full scan in steady state)
        if (cachedSource != null) {
            BlockEntity be = level.getBlockEntity(cachedSource);
            if (isNonEmptySource(be)) return cachedSource;
            cachedSource = null;
        }

        if (fullScanCooldown-- > 0) return null;
        fullScanCooldown = 40;

        BlockPos closest = null;
        int closestDistSq = Integer.MAX_VALUE;
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    BlockPos p = home.offset(dx, dy, dz);
                    if (p.equals(home)) continue;
                    BlockEntity be = level.getBlockEntity(p);
                    if (isNonEmptySource(be)) {
                        int distSq = (int) home.distSqr(p);
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            closest = p.immutable();
                        }
                    }
                }
            }
        }
        cachedSource = closest;
        return closest;
    }

    private boolean isNonEmptySource(BlockEntity be) {
        if (be instanceof LumberjackBlockEntity lbe) return !lbe.isEmpty();
        if (be instanceof MinerBlockEntity mbe) return !mbe.isEmpty();
        if (be instanceof FarmerBlockEntity fbe) return !fbe.isEmpty();
        return false;
    }

    private IItemHandler getItemHandler(BlockEntity be) {
        if (be instanceof LumberjackBlockEntity lbe) return lbe.getItemHandler();
        if (be instanceof MinerBlockEntity mbe) return mbe.getItemHandler();
        if (be instanceof FarmerBlockEntity fbe) return fbe.getItemHandler();
        return null;
    }
}
