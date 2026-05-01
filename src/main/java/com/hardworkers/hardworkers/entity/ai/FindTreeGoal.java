package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.HardWorkersConfig;
import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

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
                    if (state.is(BlockTags.LOGS) && isBaseLog(level, candidate)) {
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
}
