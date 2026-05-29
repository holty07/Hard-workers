package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.FarmerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FarmerModel extends GeoModel<FarmerEntity> {

    private static final ResourceLocation MODEL =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "geo/farmer.geo.json");
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/farmer.png");
    private static final ResourceLocation ANIMATION =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "animations/farmer.animation.json");

    @Override
    public ResourceLocation getModelResource(FarmerEntity entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureResource(FarmerEntity entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(FarmerEntity entity) { return ANIMATION; }
}
