package com.hardworkers.hardworkers.entity.client.model;

import com.hardworkers.hardworkers.entity.WarehouseWorkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class WarehouseWorkerModel extends HumanoidModel<WarehouseWorkerEntity> {

    public WarehouseWorkerModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(WarehouseWorkerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        if (entity.isWorking()) {
            rightArm.xRot = -0.7f + Mth.sin(ageInTicks * 0.35f) * 0.35f;
            leftArm.xRot  = -0.7f + Mth.sin(ageInTicks * 0.35f) * 0.35f;
        }
    }
}
