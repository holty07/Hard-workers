package com.hardworkers.hardworkers.init;

import com.hardworkers.hardworkers.HardWorkers;
import com.hardworkers.hardworkers.entity.LumberjackEntity;
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
}
