package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HardWorkers.MODID);

    public static final DeferredItem<BlockItem> LUMBERJACK_BLOCK_ITEM =
        ITEMS.registerSimpleBlockItem(ModBlocks.LUMBERJACK_BLOCK);
}
