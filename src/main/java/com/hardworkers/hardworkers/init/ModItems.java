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
}
