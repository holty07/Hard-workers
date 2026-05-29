package com.hardworkers.hardworkers.entity.ai;

import com.hardworkers.hardworkers.block.FarmerBlock;
import com.hardworkers.hardworkers.block.FarmerTier;
import com.hardworkers.hardworkers.blockentity.FarmerBlockEntity;
import com.hardworkers.hardworkers.entity.FarmerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.EnumSet;
import java.util.List;

/**
 * Scans the 5×5 area around the farmer's home block for mature crops,
 * walks to each one, harvests it, replants it (resetting the block to age 0
 * so the stem/plant remains), and deposits the drops into storage.
 *
 * Supported crop types:
 *   CropBlock          – wheat, carrots, potatoes, beetroot
 *   NetherWartBlock    – nether wart
 *   CocoaBlock         – cocoa beans (facing preserved on replant)
 *   SweetBerryBushBlock – sweet berries (set to age 1 on replant)
 *   Melon / Pumpkin    – fruit block is removed; stem stays to regrow
 */
public class HarvestCropsGoal extends Goal {

    private static final int FARM_RADIUS   = 2;    // 5×5 area
    private static final double REACH_SQ   = 6.25; // 2.5-block reach
    private static final int SCAN_Y_MIN    = -1;
    private static final int SCAN_Y_MAX    = 3;

    private final FarmerEntity farmer;
    private BlockPos targetCrop  = null;
    private int harvestTimer     = 0;
    private int searchCooldown   = 0;

    public HarvestCropsGoal(FarmerEntity farmer) {
        this.farmer = farmer;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (isStorageFull()) return false;
        if (searchCooldown > 0) { searchCooldown--; return false; }
        targetCrop = findMatureCrop();
        return targetCrop != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetCrop != null && !isStorageFull();
    }

    @Override
    public void start() {
        harvestTimer = 0;
        navigateTo(targetCrop);
    }

    @Override
    public void stop() {
        farmer.setWorking(false);
        targetCrop = null;
        harvestTimer = 0;
        farmer.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (targetCrop == null) return;

        farmer.setWorking(true);

        // Re-validate: another farmer (or random decay) may have cleared it
        if (!isMatureCrop(farmer.level().getBlockState(targetCrop))) {
            targetCrop = findMatureCrop();
            if (targetCrop == null) { searchCooldown = 40; return; }
        }

        farmer.getLookControl().setLookAt(
            targetCrop.getX() + 0.5, targetCrop.getY() + 0.5, targetCrop.getZ() + 0.5);

        double distSq = farmer.distanceToSqr(
            targetCrop.getX() + 0.5, targetCrop.getY() + 0.5, targetCrop.getZ() + 0.5);

        if (distSq > REACH_SQ) {
            // Navigate to the farmland tile below the crop so the entity walks on ground
            navigateTo(targetCrop);
            return;
        }

        harvestTimer++;
        if (harvestTimer < harvestInterval()) return;
        harvestTimer = 0;

        harvest(targetCrop);

        // Find the next crop immediately
        targetCrop = findMatureCrop();
        if (targetCrop == null) searchCooldown = 40;
    }

    // -------------------------------------------------------------------------

    private void harvest(BlockPos pos) {
        Level level = farmer.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockState state = serverLevel.getBlockState(pos);
        if (!isMatureCrop(state)) return;

        Block block = state.getBlock();
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null);

        // Replant by resetting to the youngest growth stage
        if (block instanceof CropBlock crop) {
            serverLevel.setBlock(pos, crop.defaultBlockState(), Block.UPDATE_ALL);
        } else if (block instanceof NetherWartBlock) {
            serverLevel.setBlock(pos, Blocks.NETHER_WART.defaultBlockState(), Block.UPDATE_ALL);
        } else if (block instanceof CocoaBlock) {
            // Keep the attached facing when replanting
            serverLevel.setBlock(pos, state.setValue(CocoaBlock.AGE, 0), Block.UPDATE_ALL);
        } else if (block instanceof SweetBerryBushBlock) {
            // Age 1 = regrowth stage (age 0 is the initial tiny sprout)
            serverLevel.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_ALL);
        } else {
            // Melon / pumpkin — just remove the fruit; stem regrows on its own
            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        serverLevel.levelEvent(2001, pos, Block.getId(state));
        depositItems(serverLevel, drops);
    }

    private BlockPos findMatureCrop() {
        Level level = farmer.level();
        BlockPos home = farmer.getHomePosition();

        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (int dx = -FARM_RADIUS; dx <= FARM_RADIUS; dx++) {
            for (int dz = -FARM_RADIUS; dz <= FARM_RADIUS; dz++) {
                for (int dy = SCAN_Y_MIN; dy <= SCAN_Y_MAX; dy++) {
                    BlockPos candidate = home.offset(dx, dy, dz);
                    if (isMatureCrop(level.getBlockState(candidate))) {
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

    private boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) return crop.isMaxAge(state);
        if (block instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) == 3;
        if (block instanceof CocoaBlock) return state.getValue(CocoaBlock.AGE) == 2;
        if (block instanceof SweetBerryBushBlock) return state.getValue(SweetBerryBushBlock.AGE) == 3;
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) return true;
        return false;
    }

    private void navigateTo(BlockPos cropPos) {
        // Walk to the tile at the crop's Y level (entity walks on same Y as crop for tall crops,
        // or one below for standard crops; navigator handles the adjustment)
        farmer.getNavigation().moveTo(cropPos.getX() + 0.5, cropPos.getY(), cropPos.getZ() + 0.5, 0.9);
    }

    private int harvestInterval() {
        BlockState homeState = farmer.level().getBlockState(farmer.getHomePosition());
        if (homeState.getBlock() instanceof FarmerBlock fb) return fb.getTier().harvestInterval;
        return 30;
    }

    private boolean isStorageFull() {
        BlockEntity be = farmer.level().getBlockEntity(farmer.getHomePosition());
        return be instanceof FarmerBlockEntity storage && storage.isFull();
    }

    private void depositItems(ServerLevel level, List<ItemStack> drops) {
        BlockEntity be = level.getBlockEntity(farmer.getHomePosition());
        if (be instanceof FarmerBlockEntity storage) {
            for (ItemStack drop : drops) {
                ItemStack remainder = storage.insertItem(drop);
                if (!remainder.isEmpty()) {
                    Block.popResource(level, farmer.blockPosition(), remainder);
                }
            }
        } else {
            drops.forEach(d -> Block.popResource(level, farmer.blockPosition(), d));
        }
    }
}
