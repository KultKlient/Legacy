package kult.klient.mixininterface;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public interface ILivingEntityRenderer {
    void setupTransformsInterface(LivingEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta);
    void scaleInterface(LivingEntity entity, MatrixStack matrices, float amount);
    boolean isVisibleInterface(LivingEntity entity);
    float getAnimationCounterInterface(LivingEntity entity, float tickDelta);
}
