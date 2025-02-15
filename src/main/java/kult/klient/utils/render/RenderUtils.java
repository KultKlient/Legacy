package kult.klient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import kult.klient.systems.modules.Modules;
import kult.klient.mixininterface.IMatrix4f;
import kult.klient.systems.modules.render.NoBob;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import static kult.klient.KultKlient.mc;

public class RenderUtils {
    public static Vec3d center;

    // Items
    public static void drawItem(ItemStack itemStack, int x, int y, double scale, boolean overlay) {
        //RenderSystem.disableDepthTest();

        MatrixStack matrices = RenderSystem.getModelViewStack();

        matrices.push();
        matrices.scale((float) scale, (float) scale, 1);

        mc.getItemRenderer().renderGuiItemIcon(itemStack, (int) (x / scale), (int) (y / scale));
        if (overlay) mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, (int) (x / scale), (int) (y / scale), null);

        matrices.pop();
        //RenderSystem.enableDepthTest();
    }

    public static void drawItem(ItemStack itemStack, int x, int y, boolean overlay) {
        drawItem(itemStack, x, y, 1, overlay);
    }

    public static void updateScreenCenter() {
        Vec3d pos = new Vec3d(0, 0, 1);

        if (mc.options.bobView) {
            if (!Modules.get().isActive(NoBob.class)) {
                MatrixStack bobViewMatrices = new MatrixStack();

                bobView(bobViewMatrices);
                bobViewMatrices.peek().getPositionMatrix().invert();

                pos = ((IMatrix4f) (Object) bobViewMatrices.peek().getPositionMatrix()).mul(pos);
            }
        }

        center = new Vec3d(pos.x, -pos.y, pos.z).rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch())).rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw())).add(mc.gameRenderer.getCamera().getPos());
    }

    private static void bobView(MatrixStack matrices) {
        Entity cameraEntity = mc.getCameraEntity();

        if (cameraEntity instanceof PlayerEntity playerEntity) {
            float f = mc.getTickDelta();
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp(f, playerEntity.prevStrideDistance, playerEntity.strideDistance);

            matrices.translate(-(MathHelper.sin(h * 3.1415927f) * i * 0.5), -(-Math.abs(MathHelper.cos(h * 3.1415927f) * i)), 0);
            matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(MathHelper.sin(h * 3.1415927f) * i * 3));
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(Math.abs(MathHelper.cos(h * 3.1415927f - 0.2f) * i) * 5));
        }
    }
}
