package com.hardworkers.hardworkers.block;

import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.init.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class LumberjackBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // One codec per tier — simpleCodec only encodes Properties, so we capture
    // the tier via a lambda for each variant.
    public static final MapCodec<LumberjackBlock> WOOD_CODEC      = simpleCodec(p -> new LumberjackBlock(LumberjackTier.WOOD, p));
    public static final MapCodec<LumberjackBlock> STONE_CODEC     = simpleCodec(p -> new LumberjackBlock(LumberjackTier.STONE, p));
    public static final MapCodec<LumberjackBlock> IRON_CODEC      = simpleCodec(p -> new LumberjackBlock(LumberjackTier.IRON, p));
    public static final MapCodec<LumberjackBlock> DIAMOND_CODEC   = simpleCodec(p -> new LumberjackBlock(LumberjackTier.DIAMOND, p));
    public static final MapCodec<LumberjackBlock> NETHERITE_CODEC = simpleCodec(p -> new LumberjackBlock(LumberjackTier.NETHERITE, p));

    private final LumberjackTier tier;

    public LumberjackBlock(LumberjackTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public LumberjackTier getTier() {
        return tier;
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return switch (tier) {
            case WOOD      -> WOOD_CODEC;
            case STONE     -> STONE_CODEC;
            case IRON      -> IRON_CODEC;
            case DIAMOND   -> DIAMOND_CODEC;
            case NETHERITE -> NETHERITE_CODEC;
        };
    }

    // -------------------------------------------------------------------------
    // EntityBlock
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LumberjackBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            LumberjackEntity lumberjack = ModEntities.LUMBERJACK.get().create(level);
            if (lumberjack != null) {
                lumberjack.setHomePosition(pos);
                lumberjack.setTierEquipment(this.tier);
                lumberjack.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0f, 0f);
                level.addFreshEntity(lumberjack);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof LumberjackBlockEntity be) {
                Containers.dropContents(level, pos, be);
            }
            AABB searchArea = new AABB(pos).inflate(3.0);
            level.getEntitiesOfClass(LumberjackEntity.class, searchArea)
                .stream()
                .filter(e -> pos.equals(e.getHomePosition()))
                .forEach(LumberjackEntity::discard);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // -------------------------------------------------------------------------
    // Interaction
    // -------------------------------------------------------------------------

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LumberjackBlockEntity be) {
            player.displayClientMessage(be.getStorageStatus(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public static BlockBehaviour.Properties baseProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .sound(SoundType.WOOD)
            .strength(2.0f);
    }
}
