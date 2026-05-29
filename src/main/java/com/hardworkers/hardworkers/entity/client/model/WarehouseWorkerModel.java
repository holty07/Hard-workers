package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.WarehouseWorkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WarehouseWorkerModel extends GeoModel<WarehouseWorkerEntity> {

    private static final ResourceLocation MODEL =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "geo/warehouse_worker.geo.json");
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/warehouse_worker.png");
    private static final ResourceLocation ANIMATION =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "animations/warehouse_worker.animation.json");

    @Override
    public ResourceLocation getModelResource(WarehouseWorkerEntity entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureResource(WarehouseWorkerEntity entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(WarehouseWorkerEntity entity) { return ANIMATION; }
}
