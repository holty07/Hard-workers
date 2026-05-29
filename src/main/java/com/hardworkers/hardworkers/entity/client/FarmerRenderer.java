package com.hardworkers.hardworkers.entity.client;

import com.hardworkers.hardworkers.entity.FarmerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class FarmerRenderer extends HumanoidMobRenderer<FarmerEntity, HumanoidModel<FarmerEntity>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/farmer.png");

    public FarmerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(FarmerEntity entity) {
        return TEXTURE;
    }
}
