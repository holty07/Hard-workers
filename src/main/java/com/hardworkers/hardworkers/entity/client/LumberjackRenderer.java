package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.entity.client.model.LumberjackModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LumberjackRenderer extends GeoEntityRenderer<LumberjackEntity> {

    public LumberjackRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LumberjackModel());
        this.shadowRadius = 0.5f;
    }
}
