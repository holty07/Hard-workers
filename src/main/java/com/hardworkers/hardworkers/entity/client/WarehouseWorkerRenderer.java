package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.WarehouseWorkerEntity;
import com.hardworkers.hardworkers.entity.client.model.WarehouseWorkerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WarehouseWorkerRenderer extends GeoEntityRenderer<WarehouseWorkerEntity> {

    public WarehouseWorkerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WarehouseWorkerModel());
        this.shadowRadius = 0.5f;
    }
}
