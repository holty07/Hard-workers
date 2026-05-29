package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.LumberjackEntity;
import com.hardworkers.hardworkers.entity.client.model.LumberjackModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class LumberjackRenderer extends HumanoidMobRenderer<LumberjackEntity, LumberjackModel> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/lumberjack.png");

    public LumberjackRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LumberjackModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(LumberjackEntity entity) {
        return TEXTURE;
    }
}
