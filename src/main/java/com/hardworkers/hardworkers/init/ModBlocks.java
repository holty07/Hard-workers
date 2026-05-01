package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.block.LumberjackBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HardWorkers.MODID);

    public static final DeferredBlock<LumberjackBlock> LUMBERJACK_BLOCK =
        BLOCKS.register("lumberjack_block", () -> new LumberjackBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .strength(2.0f)
        ));
}
