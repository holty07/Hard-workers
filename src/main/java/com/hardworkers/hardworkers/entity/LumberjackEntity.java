package com.hardworkers.hardworkers.entity;

import com.hardworkers.hardworkers.entity.ai.ChopTreeGoal;
import com.hardworkers.hardworkers.entity.ai.FindTreeGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;

public class LumberjackEntity extends PathfinderMob {

    private BlockPos homePosition = BlockPos.ZERO;
    private BlockPos targetTree = null;

    public LumberjackEntity(EntityType<? extends LumberjackEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ChopTreeGoal(this));
        goalSelector.addGoal(2, new FindTreeGoal(this));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount % 100 == 0 && !homePosition.equals(BlockPos.ZERO)) {
            if (level().getBlockState(homePosition).isAir()) {
                discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HomeX", homePosition.getX());
        tag.putInt("HomeY", homePosition.getY());
        tag.putInt("HomeZ", homePosition.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeX")) {
            homePosition = new BlockPos(
                tag.getInt("HomeX"),
                tag.getInt("HomeY"),
                tag.getInt("HomeZ")
            );
        }
    }

    public BlockPos getHomePosition() {
        return homePosition;
    }

    public void setHomePosition(BlockPos pos) {
        this.homePosition = pos;
    }

    public BlockPos getTargetTree() {
        return targetTree;
    }

    public void setTargetTree(BlockPos pos) {
        this.targetTree = pos;
    }
}
