package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.FarmerEntity;
import com.hardworkers.hardworkers.entity.client.model.FarmerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FarmerRenderer extends GeoEntityRenderer<FarmerEntity> {

    public FarmerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FarmerModel());
        this.shadowRadius = 0.5f;
    }
}
