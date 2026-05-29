package com.hardworkers.hardworkers.entity;

import com.hardworkers.hardworkers.block.LumberjackTier;
import com.hardworkers.hardworkers.entity.ai.ChopTreeGoal;
import com.hardworkers.hardworkers.entity.ai.FindTreeGoal;
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

public class LumberjackEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_IS_WORKING =
        SynchedEntityData.defineId(LumberjackEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("animation.lumberjack.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.lumberjack.walk");
    private static final RawAnimation ANIM_CHOP = RawAnimation.begin().thenLoop("animation.lumberjack.chop");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private BlockPos homePosition = BlockPos.ZERO;
    private BlockPos targetTree = null;

    public LumberjackEntity(EntityType<? extends LumberjackEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_WORKING, false);
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (isWorking()) return state.setAndContinue(ANIM_CHOP);
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
            homePosition = new BlockPos(
                tag.getInt("HomeX"),
                tag.getInt("HomeY"),
                tag.getInt("HomeZ")
            );
        }
    }

    public BlockPos getHomePosition() { return homePosition; }
    public void setHomePosition(BlockPos pos) { this.homePosition = pos; }

    public BlockPos getTargetTree() { return targetTree; }
    public void setTargetTree(BlockPos pos) { this.targetTree = pos; }

    public void setTierEquipment(LumberjackTier tier) {
        var axe = switch (tier) {
            case WOOD      -> Items.WOODEN_AXE;
            case STONE     -> Items.STONE_AXE;
            case IRON      -> Items.IRON_AXE;
            case DIAMOND   -> Items.DIAMOND_AXE;
            case NETHERITE -> Items.NETHERITE_AXE;
        };
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(axe));
    }
}
