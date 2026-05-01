package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.block.LumberjackBlock;
import com.hardworkers.hardworkers.block.LumberjackTier;
import com.hardworkers.hardworkers.block.MinerBlock;
import com.hardworkers.hardworkers.block.MinerTier;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HardWorkers.MODID);

    public static final DeferredBlock<LumberjackBlock> LUMBERJACK_WOOD =
        BLOCKS.register("lumberjack_wood",
            () -> new LumberjackBlock(LumberjackTier.WOOD, LumberjackBlock.baseProperties(MapColor.WOOD)));

    public static final DeferredBlock<LumberjackBlock> LUMBERJACK_STONE =
        BLOCKS.register("lumberjack_stone",
            () -> new LumberjackBlock(LumberjackTier.STONE, LumberjackBlock.baseProperties(MapColor.STONE)));

    public static final DeferredBlock<LumberjackBlock> LUMBERJACK_IRON =
        BLOCKS.register("lumberjack_iron",
            () -> new LumberjackBlock(LumberjackTier.IRON, LumberjackBlock.baseProperties(MapColor.METAL)));

    public static final DeferredBlock<LumberjackBlock> LUMBERJACK_DIAMOND =
        BLOCKS.register("lumberjack_diamond",
            () -> new LumberjackBlock(LumberjackTier.DIAMOND, LumberjackBlock.baseProperties(MapColor.DIAMOND)));

    public static final DeferredBlock<LumberjackBlock> LUMBERJACK_NETHERITE =
        BLOCKS.register("lumberjack_netherite",
            () -> new LumberjackBlock(LumberjackTier.NETHERITE, LumberjackBlock.baseProperties(MapColor.COLOR_BLACK)));

    public static final DeferredBlock<MinerBlock> MINER_WOOD =
        BLOCKS.register("miner_wood",
            () -> new MinerBlock(MinerTier.WOOD, MinerBlock.baseProperties(MapColor.STONE)));

    public static final DeferredBlock<MinerBlock> MINER_STONE =
        BLOCKS.register("miner_stone",
            () -> new MinerBlock(MinerTier.STONE, MinerBlock.baseProperties(MapColor.STONE)));

    public static final DeferredBlock<MinerBlock> MINER_IRON =
        BLOCKS.register("miner_iron",
            () -> new MinerBlock(MinerTier.IRON, MinerBlock.baseProperties(MapColor.METAL)));

    public static final DeferredBlock<MinerBlock> MINER_DIAMOND =
        BLOCKS.register("miner_diamond",
            () -> new MinerBlock(MinerTier.DIAMOND, MinerBlock.baseProperties(MapColor.DIAMOND)));

    public static final DeferredBlock<MinerBlock> MINER_NETHERITE =
        BLOCKS.register("miner_netherite",
            () -> new MinerBlock(MinerTier.NETHERITE, MinerBlock.baseProperties(MapColor.COLOR_BLACK)));
}

