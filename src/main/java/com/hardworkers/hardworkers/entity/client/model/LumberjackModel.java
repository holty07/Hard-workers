package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.LumberjackEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class LumberjackModel extends HumanoidModel<LumberjackEntity> {

    public LumberjackModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(LumberjackEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        if (entity.isWorking()) {
            rightArm.xRot = -1.3f + Mth.sin(ageInTicks * 0.3f) * 0.44f;
            leftArm.xRot  = 0f;
            body.xRot     = -0.26f;
        }
    }
}
