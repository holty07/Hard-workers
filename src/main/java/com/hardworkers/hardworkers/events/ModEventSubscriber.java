package com.hardworkers.hardworkers.events;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.entity.MinerEntity;
import com.hardworkers.hardworkers.init.ModBlockEntities;
import com.hardworkers.hardworkers.init.ModEntities;
import com.hardworkers.hardworkers.init.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = HardWorkers.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LUMBERJACK.get(), LumberjackEntity.createAttributes().build());
        event.put(ModEntities.MINER.get(), MinerEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.LUMBERJACK_WOOD_ITEM);
            event.accept(ModItems.LUMBERJACK_STONE_ITEM);
            event.accept(ModItems.LUMBERJACK_IRON_ITEM);
            event.accept(ModItems.LUMBERJACK_DIAMOND_ITEM);
            event.accept(ModItems.LUMBERJACK_NETHERITE_ITEM);
            event.accept(ModItems.MINER_WOOD_ITEM);
            event.accept(ModItems.MINER_STONE_ITEM);
            event.accept(ModItems.MINER_IRON_ITEM);
            event.accept(ModItems.MINER_DIAMOND_ITEM);
            event.accept(ModItems.MINER_NETHERITE_ITEM);
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.LUMBERJACK_BLOCK_ENTITY.get(),
            (be, side) -> be.getItemHandler()
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.MINER_BLOCK_ENTITY.get(),
            (be, side) -> be.getItemHandler()
        );
    }
}
