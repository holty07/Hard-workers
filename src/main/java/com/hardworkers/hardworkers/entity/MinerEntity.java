package com.hardworkers.hardworkers.entity;

import com.hardworkers.hardworkers.block.MinerTier;
import com.hardworkers.hardworkers.entity.ai.MineForwardGoal;
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

public class MinerEntity extends PathfinderMob {

    private BlockPos homePosition = BlockPos.ZERO;
    private int currentDepth = 1;

    public MinerEntity(EntityType<? extends MinerEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 80.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MineForwardGoal(this));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.5));
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
        tag.putInt("CurrentDepth", currentDepth);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeX")) {
            homePosition = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
        if (tag.contains("CurrentDepth")) {
            currentDepth = tag.getInt("CurrentDepth");
        }
    }

    public BlockPos getHomePosition() { return homePosition; }
    public void setHomePosition(BlockPos pos) { this.homePosition = pos; }

    public int getCurrentDepth() { return currentDepth; }
    public void setCurrentDepth(int depth) { this.currentDepth = depth; }

    public void setTierEquipment(MinerTier tier) {
        var pick = switch (tier) {
            case WOOD      -> Items.WOODEN_PICKAXE;
            case STONE     -> Items.STONE_PICKAXE;
            case IRON      -> Items.IRON_PICKAXE;
            case DIAMOND   -> Items.DIAMOND_PICKAXE;
            case NETHERITE -> Items.NETHERITE_PICKAXE;
        };
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(pick));
    }
}
