package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.FarmerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class FarmerModel extends HumanoidModel<FarmerEntity> {

    public FarmerModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(FarmerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        if (entity.isWorking()) {
            rightArm.xRot = -1.05f + Mth.sin(ageInTicks * 0.25f) * 0.61f;
            leftArm.xRot  = 0f;
            body.xRot     = -0.26f + Mth.sin(ageInTicks * 0.25f) * 0.17f;
        }
    }
}
