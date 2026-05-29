package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.MinerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MinerModel extends GeoModel<MinerEntity> {

    private static final ResourceLocation MODEL =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "geo/miner.geo.json");
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "textures/entity/miner.png");
    private static final ResourceLocation ANIMATION =
        ResourceLocation.fromNamespaceAndPath("hardworkers", "animations/miner.animation.json");

    @Override
    public ResourceLocation getModelResource(MinerEntity entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureResource(MinerEntity entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(MinerEntity entity) { return ANIMATION; }
}
