package kult.klient.mixin;

import kult.klient.systems.modules.Modules;
import kult.klient.utils.render.color.Color;
import kult.klient.systems.modules.world.Ambience;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.LightningEntityRenderer;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntityRenderer.class)
public class LightningEntityRendererMixin {
    /**
     * @author Walaryne
     */
    @Inject(method = "drawBranch", at = @At(value = "HEAD"), cancellable = true)
    private static void onSetLightningVertex(Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, int i, float h, float j, float k, float l, float m, float n, float o, boolean bl, boolean bl2, boolean bl3, boolean bl4, CallbackInfo info) {
        Ambience ambience = Modules.get().get(Ambience.class);

        if (ambience.isActive() && ambience.changeLightningColor.get()) {
            Color color = ambience.lightningColor.get();

            vertexConsumer.vertex(matrix4f, f + (bl ? o : -o), (float)(i * 16), g + (bl2 ? o : -o)).color(color.r / 255f, color.g / 255f, color.b / 255f, 0.3F).next();
            vertexConsumer.vertex(matrix4f, h + (bl ? n : -n), (float)((i + 1) * 16), j + (bl2 ? n : -n)).color(color.r / 255f, color.g / 255f, color.b / 255f, 0.3F).next();
            vertexConsumer.vertex(matrix4f, h + (bl3 ? n : -n), (float)((i + 1) * 16), j + (bl4 ? n : -n)).color(color.r / 255f, color.g / 255f, color.b / 255f, 0.3F).next();
            vertexConsumer.vertex(matrix4f, f + (bl3 ? o : -o), (float)(i * 16), g + (bl4 ? o : -o)).color(color.r / 255f, color.g / 255f, color.b / 255f, 0.3F).next();

            info.cancel();
        }
    }
}
