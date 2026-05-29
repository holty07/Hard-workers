package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.MinerEntity;
import com.hardworkers.hardworkers.entity.client.model.MinerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MinerRenderer extends GeoEntityRenderer<MinerEntity> {

    public MinerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MinerModel());
        this.shadowRadius = 0.5f;
    }
}
