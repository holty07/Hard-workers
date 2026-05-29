package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.block.LumberjackBlock;
import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Navigates to the tree set by FindTreeGoal, breaks every connected log at the
 * configured interval, then instantly clears all associated leaves (collecting
 * their drops — saplings, apples, sticks), and finally plants a sapling.
 *
 * All drops go into the LumberjackBlockEntity; overflow lands on the ground.
 */
public class ChopTreeGoal extends Goal {

    private static final double REACH_DIST_SQ = 16.0; // 4-block reach
    private static final int MAX_LOGS = 256;
    /** Margin around the log bounding box to scan for leaf blocks. */
    private static final int LEAF_MARGIN = 3;
    private static final double PICKUP_RADIUS = 2.0;

    private final LumberjackEntity lumberjack;
    private int chopTimer = 0;
    private final List<BlockPos> logsToChop = new ArrayList<>();
    private final List<BlockPos> leavesToClear = new ArrayList<>();
    private boolean clearingLeaves = false;
    private Block saplingType = null;
    private BlockPos plantPos = null;

    public ChopTreeGoal(LumberjackEntity lumberjack) {
        this.lumberjack = lumberjack;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return lumberjack.getTargetTree() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return lumberjack.getTargetTree() != null
            && (!logsToChop.isEmpty() || clearingLeaves || !lumberjack.getNavigation().isDone());
    }

    @Override
    public void start() {
        BlockPos treeBase = lumberjack.getTargetTree();
        if (treeBase == null) return;

        Level level = lumberjack.level();
        saplingType = saplingFor(level.getBlockState(treeBase).getBlock());
        plantPos = treeBase;

        collectTreeBlocks(level, treeBase);
        navigateTo(treeBase);
    }

    @Override
    public void tick() {
        BlockPos treePos = lumberjack.getTargetTree();
        if (treePos == null) return;

        pickUpNearbyItems();

        double distSq = lumberjack.distanceToSqr(
            treePos.getX() + 0.5, treePos.getY(), treePos.getZ() + 0.5
        );

        if (distSq > REACH_DIST_SQ) {
            navigateTo(treePos);
            return;
        }

        chopTimer++;
        if (!clearingLeaves) {
            if (chopTimer >= chopInterval()) {
                chopTimer = 0;
                chopNext();
            }
            if (logsToChop.isEmpty()) {
                clearingLeaves = true;
                chopTimer = chopInterval(); // fire first leaf immediately next check
            }
        } else {
            if (chopTimer >= chopInterval()) {
                chopTimer = 0;
                if (!clearLeafOne()) {
                    plantSapling();
                    lumberjack.setTargetTree(null);
                }
            }
        }
    }

    @Override
    public void stop() {
        logsToChop.clear();
        leavesToClear.clear();
        clearingLeaves = false;
        saplingType = null;
        plantPos = null;
        chopTimer = 0;
        lumberjack.getNavigation().stop();
    }

    // -------------------------------------------------------------------------

    /** Returns the chop interval from the home block's tier, with a safe fallback. */
    private int chopInterval() {
        BlockState home = lumberjack.level().getBlockState(lumberjack.getHomePosition());
        if (home.getBlock() instanceof LumberjackBlock lb) {
            return lb.getTier().chopInterval;
        }
        return 20;
    }

    private void navigateTo(BlockPos pos) {
        lumberjack.getNavigation().moveTo(
            pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0
        );
    }

    private void chopNext() {
        if (logsToChop.isEmpty()) return;

        Level level = lumberjack.level();
        BlockPos logPos = logsToChop.remove(0);
        BlockState state = level.getBlockState(logPos);

        if (!state.is(BlockTags.LOGS)) return;

        lumberjack.getLookControl().setLookAt(
            logPos.getX() + 0.5, logPos.getY() + 0.5, logPos.getZ() + 0.5
        );

        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, logPos, null);
            serverLevel.setBlock(logPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            serverLevel.levelEvent(2001, logPos, Block.getId(state)); // 2001 = block break particles + sound
            depositItems(serverLevel, drops);
        }
    }

    /**
     * Removes the next leaf block in {@link #leavesToClear}, collecting its drops.
     * Skips already-decayed leaves without wasting a tick interval.
     * @return true if there are still more leaves to process
     */
    private boolean clearLeafOne() {
        Level level = lumberjack.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            leavesToClear.clear();
            return false;
        }
        while (!leavesToClear.isEmpty()) {
            BlockPos leafPos = leavesToClear.remove(0);
            BlockState leafState = serverLevel.getBlockState(leafPos);
            if (!leafState.is(BlockTags.LEAVES)) continue; // already decayed, skip
            List<ItemStack> drops = Block.getDrops(leafState, serverLevel, leafPos, null);
            serverLevel.setBlock(leafPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            serverLevel.levelEvent(2001, leafPos, Block.getId(leafState));
            depositItems(serverLevel, drops);
            return !leavesToClear.isEmpty();
        }
        return false;
    }

    /** Picks up any nearby item entities and deposits them into the block entity. */
    private void pickUpNearbyItems() {
        Level level = lumberjack.level();
        if (!(level instanceof ServerLevel serverLevel)) return;
        AABB box = lumberjack.getBoundingBox().inflate(PICKUP_RADIUS);
        for (ItemEntity itemEnt : serverLevel.getEntitiesOfClass(ItemEntity.class, box)) {
            if (itemEnt.isRemoved()) continue;
            ItemStack stack = itemEnt.getItem().copy();
            itemEnt.discard();
            depositItems(serverLevel, List.of(stack));
        }
    }

    private void plantSapling() {
        if (plantPos == null || saplingType == null) return;

        Level level = lumberjack.level();
        BlockState ground = level.getBlockState(plantPos.below());
        boolean canPlant = level.getBlockState(plantPos).isAir()
            && (ground.is(BlockTags.DIRT) || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.PODZOL));

        if (canPlant) {
            level.setBlock(plantPos, saplingType.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * Sends all {@code drops} to the block entity's storage.
     * Items that don't fit are popped as entities near the lumberjack.
     */
    private void depositItems(ServerLevel level, List<ItemStack> drops) {
        BlockEntity be = level.getBlockEntity(lumberjack.getHomePosition());
        if (be instanceof LumberjackBlockEntity storage) {
            for (ItemStack drop : drops) {
                ItemStack remainder = storage.insertItem(drop);
                if (!remainder.isEmpty()) {
                    Block.popResource(level, lumberjack.blockPosition(), remainder);
                }
            }
        } else {
            for (ItemStack drop : drops) {
                Block.popResource(level, lumberjack.blockPosition(), drop);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tree scanning
    // -------------------------------------------------------------------------

    /**
     * BFS from {@code base} to collect all connected log blocks into
     * {@link #logsToChop}, then scans the log bounding box for associated
     * leaf blocks into {@link #leavesToClear}.
     */
    private void collectTreeBlocks(Level level, BlockPos base) {
        logsToChop.clear();
        leavesToClear.clear();

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(base);
        visited.add(base);

        while (!queue.isEmpty() && logsToChop.size() < MAX_LOGS) {
            BlockPos current = queue.poll();
            if (!level.getBlockState(current).is(BlockTags.LOGS)) continue;

            logsToChop.add(current);

            BlockPos[] neighbours = {
                current.above(),
                current.north(), current.south(), current.east(), current.west(),
                current.above().north(), current.above().south(),
                current.above().east(), current.above().west()
            };
            for (BlockPos next : neighbours) {
                if (!visited.contains(next)) {
                    visited.add(next);
                    if (level.getBlockState(next).is(BlockTags.LOGS)) {
                        queue.add(next);
                    }
                }
            }
        }

        logsToChop.sort(Comparator.comparingInt(BlockPos::getY));
        collectLeafBlocks(level);
    }

    /**
     * Scans a box around the collected logs (+ {@link #LEAF_MARGIN}) for
     * leaf blocks and populates {@link #leavesToClear}.
     */
    private void collectLeafBlocks(Level level) {
        if (logsToChop.isEmpty()) return;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : logsToChop) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }

        Set<BlockPos> logSet = new HashSet<>(logsToChop);
        for (int x = minX - LEAF_MARGIN; x <= maxX + LEAF_MARGIN; x++) {
            for (int y = minY - 1; y <= maxY + LEAF_MARGIN; y++) {
                for (int z = minZ - LEAF_MARGIN; z <= maxZ + LEAF_MARGIN; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!logSet.contains(p) && level.getBlockState(p).is(BlockTags.LEAVES)) {
                        leavesToClear.add(p);
                    }
                }
            }
        }
    }

    private Block saplingFor(Block log) {
        if (log == Blocks.OAK_LOG         || log == Blocks.STRIPPED_OAK_LOG)         return Blocks.OAK_SAPLING;
        if (log == Blocks.BIRCH_LOG        || log == Blocks.STRIPPED_BIRCH_LOG)        return Blocks.BIRCH_SAPLING;
        if (log == Blocks.SPRUCE_LOG       || log == Blocks.STRIPPED_SPRUCE_LOG)       return Blocks.SPRUCE_SAPLING;
        if (log == Blocks.JUNGLE_LOG       || log == Blocks.STRIPPED_JUNGLE_LOG)       return Blocks.JUNGLE_SAPLING;
        if (log == Blocks.ACACIA_LOG       || log == Blocks.STRIPPED_ACACIA_LOG)       return Blocks.ACACIA_SAPLING;
        if (log == Blocks.DARK_OAK_LOG     || log == Blocks.STRIPPED_DARK_OAK_LOG)     return Blocks.DARK_OAK_SAPLING;
        if (log == Blocks.MANGROVE_LOG     || log == Blocks.STRIPPED_MANGROVE_LOG)     return Blocks.MANGROVE_PROPAGULE;
        if (log == Blocks.CHERRY_LOG       || log == Blocks.STRIPPED_CHERRY_LOG)       return Blocks.CHERRY_SAPLING;
        return Blocks.OAK_SAPLING;
    }
}
