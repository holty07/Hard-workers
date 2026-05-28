package com.hardworkers.hardworkers.events;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.init.ModBlockEntities;
import com.hardworkers.hardworkers.world.WorkerChunkManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = HardWorkers.MODID, bus = EventBusSubscriber.Bus.GAME)
public class GameEventSubscriber {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        boolean hasWorker = chunk.getBlockEntities().values().stream().anyMatch(be ->
                be.getType() == ModBlockEntities.FARMER_BLOCK_ENTITY.get()
             || be.getType() == ModBlockEntities.MINER_BLOCK_ENTITY.get()
             || be.getType() == ModBlockEntities.LUMBERJACK_BLOCK_ENTITY.get()
             || be.getType() == ModBlockEntities.WAREHOUSE_BLOCK_ENTITY.get()
        );

        if (hasWorker) {
            WorkerChunkManager.get(serverLevel).claimChunkIfNew(serverLevel, chunk.getPos().getWorldPosition());
        }
    }
}
