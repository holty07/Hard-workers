package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.HardWorkersConfig;
import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Scans the configurable area around the lumberjack's home block for the base
 * of a tree (a log whose block below is not a log). Sets the target on the
 * entity so that ChopTreeGoal can take over. Runs once per activation then
 * backs off with a cooldown if no tree is found.
 */
public class FindTreeGoal extends Goal {

    private final LumberjackEntity lumberjack;
    private int searchCooldown = 0;

    public FindTreeGoal(LumberjackEntity lumberjack) {
        this.lumberjack = lumberjack;
        // No movement/look flags — this goal just updates entity state.
        setFlags(EnumSet.noneOf(Flag.class));
    }

    @Override
    public boolean canUse() {
        if (lumberjack.getTargetTree() != null) return false;
        if (isStorageFull()) return false;
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        return true;
    }

    private boolean isStorageFull() {
        Level level = lumberjack.level();
        BlockEntity be = level.getBlockEntity(lumberjack.getHomePosition());
        return be instanceof LumberjackBlockEntity storage && storage.isFull();
    }

    @Override
    public boolean canContinueToUse() {
        return false; // one-shot; ChopTreeGoal picks up from here
    }

    @Override
    public void start() {
        BlockPos tree = findNearestTreeBase();
        if (tree != null) {
            lumberjack.setTargetTree(tree);
        } else {
            searchCooldown = 100; // ~5 seconds before retrying
        }
    }

    private BlockPos findNearestTreeBase() {
        Level level = lumberjack.level();
        BlockPos home = lumberjack.getHomePosition();
        int radius = HardWorkersConfig.LUMBERJACK_SEARCH_RADIUS.get();

        Set<BlockPos> claimed = getClaimedTrees(level, home, radius);

        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius / 2; y <= radius / 2; y++) {
                    BlockPos candidate = home.offset(x, y, z);
                    if (claimed.contains(candidate)) continue;

                    BlockState state = level.getBlockState(candidate);
                    if (state.is(BlockTags.LOGS) && isBaseLog(level, candidate)
                            && isActualTree(level, candidate)) {
                        double distSq = home.distSqr(candidate);
                        if (distSq < nearestDistSq) {
                            nearestDistSq = distSq;
                            nearest = candidate;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    /** Returns the set of tree-base positions already claimed by other lumberjacks in the area. */
    private Set<BlockPos> getClaimedTrees(Level level, BlockPos home, int radius) {
        AABB searchBox = new AABB(
            home.getX() - radius, home.getY() - radius / 2.0, home.getZ() - radius,
            home.getX() + radius, home.getY() + radius / 2.0, home.getZ() + radius
        );
        Set<BlockPos> claimed = new HashSet<>();
        level.getEntitiesOfClass(LumberjackEntity.class, searchBox, e -> e != lumberjack)
             .forEach(e -> { if (e.getTargetTree() != null) claimed.add(e.getTargetTree()); });
        return claimed;
    }

    /** A base log is a log block that does not have another log directly below it. */
    private boolean isBaseLog(Level level, BlockPos pos) {
        return !level.getBlockState(pos.below()).is(BlockTags.LOGS);
    }

    /**
     * Verifies that the log cluster rooted at {@code base} is a real tree by
     * confirming at least 2 leaf blocks of the matching wood type are present
     * within a 3-block margin of the connected logs.
     */
    private boolean isActualTree(Level level, BlockPos base) {
        Block expectedLeaves = leavesFor(level.getBlockState(base).getBlock());

        // BFS to collect connected logs (capped to avoid O(n²) in dense areas).
        Set<BlockPos> logSet = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(base);
        logSet.add(base);

        while (!queue.isEmpty() && logSet.size() < 256) {
            BlockPos current = queue.poll();
            BlockPos[] neighbours = {
                current.above(),
                current.north(), current.south(), current.east(), current.west(),
                current.above().north(), current.above().south(),
                current.above().east(), current.above().west()
            };
            for (BlockPos next : neighbours) {
                if (!logSet.contains(next) && level.getBlockState(next).is(BlockTags.LOGS)) {
                    logSet.add(next);
                    queue.add(next);
                }
            }
        }

        // Compute bounding box of all logs.
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos p : logSet) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }

        // Scan a 3-block margin for at least 2 matching leaf blocks.
        int margin = 3;
        int leafCount = 0;
        outer:
        for (int x = minX - margin; x <= maxX + margin; x++) {
            for (int y = minY - 1; y <= maxY + margin; y++) {
                for (int z = minZ - margin; z <= maxZ + margin; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (logSet.contains(p)) continue;
                    BlockState state = level.getBlockState(p);
                    if (state.is(BlockTags.LEAVES)
                            && (expectedLeaves == null || state.getBlock() == expectedLeaves)) {
                        if (++leafCount >= 2) break outer;
                    }
                }
            }
        }
        return leafCount >= 2;
    }

    /** Maps a log block to its corresponding leaf block; returns null for unknown types. */
    private Block leavesFor(Block log) {
        if (log == Blocks.OAK_LOG         || log == Blocks.STRIPPED_OAK_LOG)         return Blocks.OAK_LEAVES;
        if (log == Blocks.BIRCH_LOG        || log == Blocks.STRIPPED_BIRCH_LOG)        return Blocks.BIRCH_LEAVES;
        if (log == Blocks.SPRUCE_LOG       || log == Blocks.STRIPPED_SPRUCE_LOG)       return Blocks.SPRUCE_LEAVES;
        if (log == Blocks.JUNGLE_LOG       || log == Blocks.STRIPPED_JUNGLE_LOG)       return Blocks.JUNGLE_LEAVES;
        if (log == Blocks.ACACIA_LOG       || log == Blocks.STRIPPED_ACACIA_LOG)       return Blocks.ACACIA_LEAVES;
        if (log == Blocks.DARK_OAK_LOG     || log == Blocks.STRIPPED_DARK_OAK_LOG)     return Blocks.DARK_OAK_LEAVES;
        if (log == Blocks.MANGROVE_LOG     || log == Blocks.STRIPPED_MANGROVE_LOG)     return Blocks.MANGROVE_LEAVES;
        if (log == Blocks.CHERRY_LOG       || log == Blocks.STRIPPED_CHERRY_LOG)       return Blocks.CHERRY_LEAVES;
        return null; // unknown log type — accept any leaves
    }
}
