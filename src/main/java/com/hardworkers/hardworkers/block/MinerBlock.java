package com.hardworkers.hardworkers.block;

import com.hardworkers.hardworkers.blockentity.MinerBlockEntity;
import com.hardworkers.hardworkers.entity.MinerEntity;
import com.hardworkers.hardworkers.init.ModEntities;
import com.hardworkers.hardworkers.world.WorkerChunkManager;
import net.minecraft.server.level.ServerLevel;
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

public class MinerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<MinerBlock> WOOD_CODEC      = simpleCodec(p -> new MinerBlock(MinerTier.WOOD, p));
    public static final MapCodec<MinerBlock> STONE_CODEC     = simpleCodec(p -> new MinerBlock(MinerTier.STONE, p));
    public static final MapCodec<MinerBlock> IRON_CODEC      = simpleCodec(p -> new MinerBlock(MinerTier.IRON, p));
    public static final MapCodec<MinerBlock> DIAMOND_CODEC   = simpleCodec(p -> new MinerBlock(MinerTier.DIAMOND, p));
    public static final MapCodec<MinerBlock> NETHERITE_CODEC = simpleCodec(p -> new MinerBlock(MinerTier.NETHERITE, p));

    private final MinerTier tier;

    public MinerBlock(MinerTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public MinerTier getTier() { return tier; }

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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            MinerEntity miner = ModEntities.MINER.get().create(level);
            if (miner != null) {
                miner.setHomePosition(pos);
                miner.setTierEquipment(this.tier);
                miner.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0f, 0f);
                level.addFreshEntity(miner);
            }
            WorkerChunkManager.get((ServerLevel) level).claimChunk((ServerLevel) level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MinerBlockEntity be) {
                Containers.dropContents(level, pos, be);
            }
            AABB searchArea = new AABB(pos).inflate(3.0);
            level.getEntitiesOfClass(MinerEntity.class, searchArea)
                .stream()
                .filter(e -> pos.equals(e.getHomePosition()))
                .forEach(MinerEntity::discard);
            WorkerChunkManager.get((ServerLevel) level).releaseChunk((ServerLevel) level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MinerBlockEntity be) {
            player.displayClientMessage(be.getStorageStatus(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static BlockBehaviour.Properties baseProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .sound(SoundType.STONE)
            .strength(2.5f);
    }
}
