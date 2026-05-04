package com.hardworkers.hardworkers.entity;

import com.hardworkers.hardworkers.block.FarmerTier;
import com.hardworkers.hardworkers.entity.ai.HarvestCropsGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FarmerEntity extends PathfinderMob {

    private BlockPos homePosition = BlockPos.ZERO;

    public FarmerEntity(EntityType<? extends FarmerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new HarvestCropsGoal(this));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
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
            homePosition = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
    }

    public BlockPos getHomePosition() { return homePosition; }
    public void setHomePosition(BlockPos pos) { this.homePosition = pos; }

    public void setTierEquipment(FarmerTier tier) {
        var hoe = switch (tier) {
            case WOOD      -> Items.WOODEN_HOE;
            case STONE     -> Items.STONE_HOE;
            case IRON      -> Items.IRON_HOE;
            case DIAMOND   -> Items.DIAMOND_HOE;
            case NETHERITE -> Items.NETHERITE_HOE;
        };
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(hoe));
    }
}
