package com.hardworkers.hardworkers.block;

import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.init.ModBlockEntities;
import com.hardworkers.hardworkers.init.ModEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class LumberjackBlock extends BaseEntityBlock {

    public static final MapCodec<LumberjackBlock> CODEC = simpleCodec(LumberjackBlock::new);

    public LumberjackBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // -------------------------------------------------------------------------
    // EntityBlock
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LumberjackBlockEntity(pos, state);
    }

    // Keep the normal cube model; BaseEntityBlock defaults to INVISIBLE.
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
                lumberjack.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0f, 0f);
                level.addFreshEntity(lumberjack);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            // Drop stored logs
            if (level.getBlockEntity(pos) instanceof LumberjackBlockEntity be) {
                Containers.dropContents(level, pos, be);
            }
            // Discard the associated lumberjack entity
            AABB searchArea = new AABB(pos).inflate(3.0);
            level.getEntitiesOfClass(LumberjackEntity.class, searchArea)
                .stream()
                .filter(e -> pos.equals(e.getHomePosition()))
                .forEach(LumberjackEntity::discard);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // -------------------------------------------------------------------------
    // Interaction — right-click shows storage status in the action bar
    // -------------------------------------------------------------------------

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LumberjackBlockEntity be) {
            player.displayClientMessage(be.getStorageStatus(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
