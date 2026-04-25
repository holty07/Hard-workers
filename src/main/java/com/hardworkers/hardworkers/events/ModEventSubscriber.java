package com.hardworkers.hardworkers.events;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.init.ModEntities;
import com.hardworkers.hardworkers.init.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = HardWorkers.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LUMBERJACK.get(), LumberjackEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.LUMBERJACK_BLOCK_ITEM);
        }
    }
}
