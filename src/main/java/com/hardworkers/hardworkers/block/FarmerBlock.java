package com.hardworkers.hardworkers.block;

import com.hardworkers.hardworkers.blockentity.FarmerBlockEntity;
import com.hardworkers.hardworkers.entity.FarmerEntity;
import com.hardworkers.hardworkers.init.ModBlockEntities;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class FarmerBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final MapCodec<FarmerBlock> WOOD_CODEC      = simpleCodec(p -> new FarmerBlock(FarmerTier.WOOD, p));
    public static final MapCodec<FarmerBlock> STONE_CODEC     = simpleCodec(p -> new FarmerBlock(FarmerTier.STONE, p));
    public static final MapCodec<FarmerBlock> IRON_CODEC      = simpleCodec(p -> new FarmerBlock(FarmerTier.IRON, p));
    public static final MapCodec<FarmerBlock> DIAMOND_CODEC   = simpleCodec(p -> new FarmerBlock(FarmerTier.DIAMOND, p));
    public static final MapCodec<FarmerBlock> NETHERITE_CODEC = simpleCodec(p -> new FarmerBlock(FarmerTier.NETHERITE, p));

    private final FarmerTier tier;

    public FarmerBlock(FarmerTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public FarmerTier getTier() { return tier; }

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
        return new FarmerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
            : createTickerHelper(type, ModBlockEntities.FARMER_BLOCK_ENTITY.get(), FarmerBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            FarmerEntity farmer = ModEntities.FARMER.get().create(level);
            if (farmer != null) {
                farmer.setHomePosition(pos);
                farmer.setTierEquipment(this.tier);
                farmer.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0f, 0f);
                level.addFreshEntity(farmer);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FarmerBlockEntity be) {
                Containers.dropContents(level, pos, be);
            }
            AABB searchArea = new AABB(pos).inflate(3.0);
            level.getEntitiesOfClass(FarmerEntity.class, searchArea)
                .stream()
                .filter(e -> pos.equals(e.getHomePosition()))
                .forEach(FarmerEntity::discard);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FarmerBlockEntity be) {
            player.displayClientMessage(be.getStorageStatus(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static BlockBehaviour.Properties baseProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .sound(SoundType.WOOD)
            .strength(2.0f);
    }
}
