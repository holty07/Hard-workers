package com.hardworkers.hardworkers.events;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
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
import net.neoforged.neoforge.items.wrapper.InvWrapper;

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

    /**
     * Exposes the block entity's inventory as IItemHandler so that hoppers and
     * pipes from other mods (e.g. Mekanism, Pipez) can extract logs from it.
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.LUMBERJACK_BLOCK_ENTITY.get(),
            (be, side) -> be.getItemHandler()
        );
    }
}
