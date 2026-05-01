package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.blockentity.LumberjackBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HardWorkers.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LumberjackBlockEntity>> LUMBERJACK_BLOCK_ENTITY =
        BLOCK_ENTITY_TYPES.register("lumberjack_block", () ->
            BlockEntityType.Builder.of(
                LumberjackBlockEntity::new,
                ModBlocks.LUMBERJACK_WOOD.get(),
                ModBlocks.LUMBERJACK_STONE.get(),
                ModBlocks.LUMBERJACK_IRON.get(),
                ModBlocks.LUMBERJACK_DIAMOND.get(),
                ModBlocks.LUMBERJACK_NETHERITE.get()
            ).build(null)
        );
}
