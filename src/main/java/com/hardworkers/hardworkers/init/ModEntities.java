package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.entity.FarmerEntity;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.entity.MinerEntity;
import com.hardworkers.hardworkers.entity.WarehouseWorkerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, HardWorkers.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<LumberjackEntity>> LUMBERJACK =
        ENTITY_TYPES.register("lumberjack", () ->
            EntityType.Builder.<LumberjackEntity>of(LumberjackEntity::new, MobCategory.MISC)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(10)
                .build("lumberjack")
        );

    public static final DeferredHolder<EntityType<?>, EntityType<MinerEntity>> MINER =
        ENTITY_TYPES.register("miner", () ->
            EntityType.Builder.<MinerEntity>of(MinerEntity::new, MobCategory.MISC)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(80)
                .build("miner")
        );

    public static final DeferredHolder<EntityType<?>, EntityType<FarmerEntity>> FARMER =
        ENTITY_TYPES.register("farmer", () ->
            EntityType.Builder.<FarmerEntity>of(FarmerEntity::new, MobCategory.MISC)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(10)
                .build("farmer")
        );

    public static final DeferredHolder<EntityType<?>, EntityType<WarehouseWorkerEntity>> WAREHOUSE_WORKER =
        ENTITY_TYPES.register("warehouse_worker", () ->
            EntityType.Builder.<WarehouseWorkerEntity>of(WarehouseWorkerEntity::new, MobCategory.MISC)
                .sized(0.6f, 1.95f)
                .clientTrackingRange(10)
                .build("warehouse_worker")
        );
}

