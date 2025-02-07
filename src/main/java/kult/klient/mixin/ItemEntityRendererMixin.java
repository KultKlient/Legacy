package kult.klient.mixin;

import kult.klient.events.render.RenderItemEntityEvent;
import kult.klient.KultKlient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    private Random random;

    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        RenderItemEntityEvent event = KultKlient.EVENT_BUS.post(RenderItemEntityEvent.get(itemEntity, f, g, matrixStack, vertexConsumerProvider, i, random, itemRenderer));
        if (event.isCancelled()) info.cancel();
    }
}
