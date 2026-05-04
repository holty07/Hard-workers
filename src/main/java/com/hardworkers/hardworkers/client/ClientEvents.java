package com.hardworkers.hardworkers.client;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.entity.client.FarmerRenderer;
import com.hardworkers.hardworkers.entity.client.LumberjackRenderer;
import com.hardworkers.hardworkers.entity.client.MinerRenderer;
import com.hardworkers.hardworkers.init.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = HardWorkers.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LUMBERJACK.get(), LumberjackRenderer::new);
        event.registerEntityRenderer(ModEntities.MINER.get(), MinerRenderer::new);
        event.registerEntityRenderer(ModEntities.FARMER.get(), FarmerRenderer::new);
    }
}
