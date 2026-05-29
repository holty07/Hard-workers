package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.MinerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class MinerModel extends HumanoidModel<MinerEntity> {

    public MinerModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(MinerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        if (entity.isWorking()) {
            rightArm.xRot = -1.57f + Mth.sin(ageInTicks * 0.4f) * 0.52f;
            leftArm.xRot  = -1.2f + Mth.sin(ageInTicks * 0.4f + 0.4f) * 0.26f;
            body.xRot     = -0.17f;
        }
    }
}
