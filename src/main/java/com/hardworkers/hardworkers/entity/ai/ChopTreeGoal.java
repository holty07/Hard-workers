package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.HardWorkersConfig;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Navigates to the tree set by FindTreeGoal, breaks every connected log
 * (BFS-collected, chopped bottom-to-top) at the configured interval, then
 * plants the matching sapling at the base position.
 */
public class ChopTreeGoal extends Goal {

    private static final double REACH_DIST_SQ = 16.0; // 4-block reach
    private static final int MAX_LOGS = 256;

    private final LumberjackEntity lumberjack;
    private int chopTimer = 0;
    private final List<BlockPos> logsToChop = new ArrayList<>();
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
            && (!logsToChop.isEmpty() || !lumberjack.getNavigation().isDone());
    }

    @Override
    public void start() {
        BlockPos treeBase = lumberjack.getTargetTree();
        if (treeBase == null) return;

        Level level = lumberjack.level();
        saplingType = saplingFor(level.getBlockState(treeBase).getBlock());
        plantPos = treeBase;

        collectTreeLogs(level, treeBase);
        navigateTo(treeBase);
    }

    @Override
    public void tick() {
        BlockPos treePos = lumberjack.getTargetTree();
        if (treePos == null) return;

        double distSq = lumberjack.distanceToSqr(
            treePos.getX() + 0.5, treePos.getY(), treePos.getZ() + 0.5
        );

        if (distSq > REACH_DIST_SQ) {
            navigateTo(treePos);
            return;
        }

        chopTimer++;
        if (chopTimer >= HardWorkersConfig.LUMBERJACK_CHOP_INTERVAL.get()) {
            chopTimer = 0;
            chopNext();
        }

        if (logsToChop.isEmpty()) {
            plantSapling();
            lumberjack.setTargetTree(null);
            // canContinueToUse() will return false next tick, triggering stop()
        }
    }

    @Override
    public void stop() {
        logsToChop.clear();
        saplingType = null;
        plantPos = null;
        chopTimer = 0;
        lumberjack.getNavigation().stop();
    }

    // -------------------------------------------------------------------------

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

        if (!state.is(BlockTags.LOGS)) return; // already broken by something else

        lumberjack.getLookControl().setLookAt(
            logPos.getX() + 0.5, logPos.getY() + 0.5, logPos.getZ() + 0.5
        );

        if (level instanceof ServerLevel serverLevel) {
            Block.dropResources(state, serverLevel, logPos, null, lumberjack, ItemStack.EMPTY);
            level.setBlock(logPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, logPos, Block.getId(state));
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

    /** BFS from the base log to collect all connected log blocks. */
    private void collectTreeLogs(Level level, BlockPos base) {
        logsToChop.clear();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(base);
        visited.add(base);

        while (!queue.isEmpty() && logsToChop.size() < MAX_LOGS) {
            BlockPos current = queue.poll();
            if (!level.getBlockState(current).is(BlockTags.LOGS)) continue;

            logsToChop.add(current);

            // Logs connect upward, diagonally-up, and horizontally (for large oak etc.)
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

        // Bottom logs first so the tree "falls" from the base
        logsToChop.sort(Comparator.comparingInt(BlockPos::getY));
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
        if (log == Blocks.PALE_OAK_LOG     || log == Blocks.STRIPPED_PALE_OAK_LOG)     return Blocks.PALE_OAK_SAPLING;
        return Blocks.OAK_SAPLING;
    }
}
