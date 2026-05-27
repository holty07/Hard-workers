package com.hardworkers.hardworkers.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class WorkerChunkManager extends SavedData {

    public static final Factory<WorkerChunkManager> FACTORY =
            new Factory<>(WorkerChunkManager::new, WorkerChunkManager::load);
    private static final String DATA_NAME = "hardworkers_chunks";

    private final Map<Long, Integer> refCounts = new HashMap<>();

    public static WorkerChunkManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static WorkerChunkManager load(CompoundTag tag) {
        WorkerChunkManager manager = new WorkerChunkManager();
        ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            int count = e.getInt("count");
            if (count > 0) {
                manager.refCounts.put(ChunkPos.asLong(e.getInt("x"), e.getInt("z")), count);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        refCounts.forEach((key, count) -> {
            CompoundTag e = new CompoundTag();
            e.putInt("x", ChunkPos.getX(key));
            e.putInt("z", ChunkPos.getZ(key));
            e.putInt("count", count);
            list.add(e);
        });
        tag.put("chunks", list);
        return tag;
    }

    public void claimChunk(ServerLevel level, BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        long key = cp.toLong();
        int prev = refCounts.getOrDefault(key, 0);
        refCounts.put(key, prev + 1);
        if (prev == 0) {
            level.setChunkForced(cp.x, cp.z, true);
        }
        setDirty();
    }

    public void releaseChunk(ServerLevel level, BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        long key = cp.toLong();
        int prev = refCounts.getOrDefault(key, 0);
        if (prev <= 0) {
            refCounts.remove(key);
            setDirty();
            return;
        }
        if (prev == 1) {
            refCounts.remove(key);
            level.setChunkForced(cp.x, cp.z, false);
        } else {
            refCounts.put(key, prev - 1);
        }
        setDirty();
    }

    public void claimChunkIfNew(ServerLevel level, BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        long key = cp.toLong();
        if (!refCounts.containsKey(key)) {
            refCounts.put(key, 1);
            level.setChunkForced(cp.x, cp.z, true);
            setDirty();
        }
    }
}
