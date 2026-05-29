package com.hardworkers.hardworkers.entity;

import com.hardworkers.hardworkers.block.FarmerTier;
import com.hardworkers.hardworkers.entity.ai.HarvestCropsGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FarmerEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_IS_WORKING =
        SynchedEntityData.defineId(FarmerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_IDLE    = RawAnimation.begin().thenLoop("animation.farmer.idle");
    private static final RawAnimation ANIM_WALK    = RawAnimation.begin().thenLoop("animation.farmer.walk");
    private static final RawAnimation ANIM_HARVEST = RawAnimation.begin().thenLoop("animation.farmer.harvest");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private BlockPos homePosition = BlockPos.ZERO;

    public FarmerEntity(EntityType<? extends FarmerEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_WORKING, false);
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (isWorking()) return state.setAndContinue(ANIM_HARVEST);
            if (state.isMoving()) return state.setAndContinue(ANIM_WALK);
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public boolean isWorking() { return this.entityData.get(DATA_IS_WORKING); }
    public void setWorking(boolean working) { this.entityData.set(DATA_IS_WORKING, working); }

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
