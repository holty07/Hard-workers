package com.hardworkers.hardworkers;

import com.hardworkers.hardworkers.init.ModBlocks;
import com.hardworkers.hardworkers.init.ModEntities;
import com.hardworkers.hardworkers.init.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(HardWorkers.MODID)
public class HardWorkers {

    public static final String MODID = "hardworkers";

    public HardWorkers(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, HardWorkersConfig.SPEC);
    }
}
