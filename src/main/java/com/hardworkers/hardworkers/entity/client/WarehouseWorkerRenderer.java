package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.WarehouseWorkerEntity;
import com.hardworkers.hardworkers.entity.client.model.WarehouseWorkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WarehouseWorkerRenderer extends HumanoidMobRenderer<WarehouseWorkerEntity, WarehouseWorkerModel> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/warehouse_worker.png");

    public WarehouseWorkerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WarehouseWorkerModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(WarehouseWorkerEntity entity) {
        return TEXTURE;
    }
}
