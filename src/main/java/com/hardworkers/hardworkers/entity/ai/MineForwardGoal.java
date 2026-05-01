package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.block.MinerBlock;
import com.hardworkers.hardworkers.block.MinerTier;
import com.hardworkers.hardworkers.blockentity.MinerBlockEntity;
import com.hardworkers.hardworkers.entity.MinerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * Mines a 3-block-tall (Y, Y+1, Y+2) tunnel in the direction the miner block
 * faces, up to 64 blocks deep.  One block is mined per interval; the current
 * depth is saved in the entity so progress survives world restarts.
 *
 * Tier capability (blocks in the path that need a higher tier are skipped):
 *   WOOD      – no NEEDS_STONE_TOOL, NEEDS_IRON_TOOL, or NEEDS_DIAMOND_TOOL blocks
 *   STONE     – no NEEDS_IRON_TOOL or NEEDS_DIAMOND_TOOL blocks
 *   IRON      – no NEEDS_DIAMOND_TOOL blocks
 *   DIAMOND / NETHERITE – mines everything (except bedrock)
 */
public class MineForwardGoal extends Goal {

    public static final int MAX_DEPTH    = 64;
    public static final int TUNNEL_HEIGHT = 3;

    private final MinerEntity miner;
    private int mineTimer = 0;

    public MineForwardGoal(MinerEntity miner) {
        this.miner = miner;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return miner.getCurrentDepth() <= MAX_DEPTH && !isStorageFull();
    }

    @Override
    public boolean canContinueToUse() {
        return miner.getCurrentDepth() <= MAX_DEPTH;
    }

    @Override
    public void stop() {
        mineTimer = 0;
        miner.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!(miner.level() instanceof ServerLevel serverLevel)) return;

        int depth = miner.getCurrentDepth();
        if (depth > MAX_DEPTH) return;

        Direction facing = getHomeFacing();
        if (facing == null) return;

        BlockPos homePos  = miner.getHomePosition();
        BlockPos facePos  = homePos.relative(facing, depth);

        // Walk toward the current mining face and look at it
        miner.getNavigation().moveTo(facePos.getX() + 0.5, facePos.getY(), facePos.getZ() + 0.5, 0.85);
        miner.getLookControl().setLookAt(facePos.getX() + 0.5, facePos.getY() + 1.0, facePos.getZ() + 0.5);

        if (isStorageFull()) return;

        mineTimer++;
        if (mineTimer < mineInterval()) return;
        mineTimer = 0;

        // Find and mine the first block at this depth that needs mining
        for (int h = 0; h < TUNNEL_HEIGHT; h++) {
            BlockPos minePos = facePos.above(h);
            BlockState state  = serverLevel.getBlockState(minePos);

            if (!needsMining(state)) continue;

            miner.getLookControl().setLookAt(minePos.getX() + 0.5, minePos.getY() + 0.5, minePos.getZ() + 0.5);
            List<ItemStack> drops = Block.getDrops(state, serverLevel, minePos, null);
            serverLevel.setBlock(minePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            serverLevel.levelEvent(2001, minePos, Block.getId(state));
            depositItems(serverLevel, drops);
            return; // one block per interval
        }

        // All three heights at this depth are clear (air, bedrock, or tier-restricted) → advance
        miner.setCurrentDepth(depth + 1);
    }

    // -------------------------------------------------------------------------

    /**
     * Returns true if this block should be mined (not air, not bedrock, not a
     * liquid, and within the tier's capability).
     */
    private boolean needsMining(BlockState state) {
        if (state.isAir()) return false;
        if (state.is(Blocks.BEDROCK)) return false;
        if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) return false;
        return canMine(state);
    }

    /** Returns true if the current tier can break this block. */
    private boolean canMine(BlockState state) {
        return switch (getHomeTier()) {
            case WOOD      -> !state.is(BlockTags.NEEDS_STONE_TOOL)
                           && !state.is(BlockTags.NEEDS_IRON_TOOL)
                           && !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
            case STONE     -> !state.is(BlockTags.NEEDS_IRON_TOOL)
                           && !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
            case IRON      -> !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
            case DIAMOND, NETHERITE -> true;
        };
    }

    private MinerTier getHomeTier() {
        BlockState homeState = miner.level().getBlockState(miner.getHomePosition());
        if (homeState.getBlock() instanceof MinerBlock mb) return mb.getTier();
        return MinerTier.NETHERITE;
    }

    private Direction getHomeFacing() {
        BlockState homeState = miner.level().getBlockState(miner.getHomePosition());
        if (homeState.hasProperty(MinerBlock.FACING)) return homeState.getValue(MinerBlock.FACING);
        return null;
    }

    private int mineInterval() {
        return getHomeTier().mineInterval;
    }

    private boolean isStorageFull() {
        BlockEntity be = miner.level().getBlockEntity(miner.getHomePosition());
        return be instanceof MinerBlockEntity storage && storage.isFull();
    }

    private void depositItems(ServerLevel level, List<ItemStack> drops) {
        BlockEntity be = level.getBlockEntity(miner.getHomePosition());
        if (be instanceof MinerBlockEntity storage) {
            for (ItemStack drop : drops) {
                ItemStack remainder = storage.insertItem(drop);
                if (!remainder.isEmpty()) {
                    Block.popResource(level, miner.blockPosition(), remainder);
                }
            }
        } else {
            for (ItemStack drop : drops) {
                Block.popResource(level, miner.blockPosition(), drop);
            }
        }
    }
}
