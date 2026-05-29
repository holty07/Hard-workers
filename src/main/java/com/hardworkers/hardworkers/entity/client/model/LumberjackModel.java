package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LumberjackModel extends GeoModel<LumberjackEntity> {

    private static final ResourceLocation MODEL =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "geo/lumberjack.geo.json");
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/lumberjack.png");
    private static final ResourceLocation ANIMATION =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "animations/lumberjack.animation.json");

    @Override
    public ResourceLocation getModelResource(LumberjackEntity entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureResource(LumberjackEntity entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(LumberjackEntity entity) { return ANIMATION; }
}
