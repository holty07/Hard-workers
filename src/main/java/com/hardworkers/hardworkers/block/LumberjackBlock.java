package com.hardworkers.hardworkers.block;

import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

public class LumberjackBlock extends Block {

    public LumberjackBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .sound(SoundType.WOOD)
            .strength(2.0f)
        );
    }

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
            AABB searchArea = new AABB(pos).inflate(3.0);
            level.getEntitiesOfClass(LumberjackEntity.class, searchArea)
                .stream()
                .filter(e -> pos.equals(e.getHomePosition()))
                .forEach(LumberjackEntity::discard);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
