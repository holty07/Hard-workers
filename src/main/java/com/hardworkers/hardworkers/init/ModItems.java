package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HardWorkers.MODID);

    public static final DeferredItem<BlockItem> LUMBERJACK_WOOD_ITEM      = ITEMS.registerSimpleBlockItem(ModBlocks.LUMBERJACK_WOOD);
    public static final DeferredItem<BlockItem> LUMBERJACK_STONE_ITEM     = ITEMS.registerSimpleBlockItem(ModBlocks.LUMBERJACK_STONE);
    public static final DeferredItem<BlockItem> LUMBERJACK_IRON_ITEM      = ITEMS.registerSimpleBlockItem(ModBlocks.LUMBERJACK_IRON);
    public static final DeferredItem<BlockItem> LUMBERJACK_DIAMOND_ITEM   = ITEMS.registerSimpleBlockItem(ModBlocks.LUMBERJACK_DIAMOND);
    public static final DeferredItem<BlockItem> LUMBERJACK_NETHERITE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.LUMBERJACK_NETHERITE);

    public static final DeferredItem<BlockItem> MINER_WOOD_ITEM      = ITEMS.registerSimpleBlockItem(ModBlocks.MINER_WOOD);
    public static final DeferredItem<BlockItem> MINER_STONE_ITEM     = ITEMS.registerSimpleBlockItem(ModBlocks.MINER_STONE);
    public static final DeferredItem<BlockItem> MINER_IRON_ITEM      = ITEMS.registerSimpleBlockItem(ModBlocks.MINER_IRON);
    public static final DeferredItem<BlockItem> MINER_DIAMOND_ITEM   = ITEMS.registerSimpleBlockItem(ModBlocks.MINER_DIAMOND);
    public static final DeferredItem<BlockItem> MINER_NETHERITE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.MINER_NETHERITE);

    public static final DeferredItem<BlockItem> FARMER_WOOD_ITEM      = ITEMS.registerSimpleBlockItem(ModBlocks.FARMER_WOOD);
    public static final DeferredItem<BlockItem> FARMER_STONE_ITEM     = ITEMS.registerSimpleBlockItem(ModBlocks.FARMER_STONE);
    public static final DeferredItem<BlockItem> FARMER_IRON_ITEM      = ITEMS.registerSimpleBlockItem(ModBlocks.FARMER_IRON);
    public static final DeferredItem<BlockItem> FARMER_DIAMOND_ITEM   = ITEMS.registerSimpleBlockItem(ModBlocks.FARMER_DIAMOND);
    public static final DeferredItem<BlockItem> FARMER_NETHERITE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.FARMER_NETHERITE);
}

