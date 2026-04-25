package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.HardWorkersConfig;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

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
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }
        return true;
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

        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius / 2; y <= radius / 2; y++) {
                    BlockPos candidate = home.offset(x, y, z);
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

    /** A base log is a log block that does not have another log directly below it. */
    private boolean isBaseLog(Level level, BlockPos pos) {
        return !level.getBlockState(pos.below()).is(BlockTags.LOGS);
    }
}
