package kult.klient.systems.modules.render;

import kult.klient.KultKlient;
import kult.klient.eventbus.EventHandler;
import kult.klient.events.render.Render3DEvent;
import kult.klient.settings.BoolSetting;
import kult.klient.settings.ColorSetting;
import kult.klient.settings.Setting;
import kult.klient.settings.SettingGroup;
import kult.klient.systems.config.Config;
import kult.klient.systems.enemies.Enemies;
import kult.klient.systems.friends.Friends;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.systems.modules.Modules;
import kult.klient.utils.player.Rotations;
import kult.klient.utils.render.color.Color;
import kult.klient.utils.render.color.SettingColor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

/*/----------------------------------------------------------------------------------------------------------------------/*/
/*/ Remastered the code using Jex Client Skeletons module                                                                /*/
/*/ https://github.com/DustinRepo/JexClient/blob/main/src/main/java/me/dustin/jex/feature/mod/impl/render/Skeletons.java /*/
/*/----------------------------------------------------------------------------------------------------------------------/*/

// TODO: Fix bed position broken.

public class SkeletonESP extends Module {
    private final Color distanceColor = new Color();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    // General

    private final Setting<Boolean> firstPerson = sgGeneral.add(new BoolSetting.Builder()
        .name("first-person")
        .description("Renders your skeleton in first person.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Stops rendering skeletons of your friends.")
        .defaultValue(false)
        .build()
    );

    // Colors

    public final Setting<Boolean> distance = sgColors.add(new BoolSetting.Builder()
        .name("distance-colors")
        .description("Changes the color of the skeleton depending on distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> playersColor = sgColors.add(new ColorSetting.Builder()
        .name("players-color")
        .description("The other player's color.")
        .defaultValue(new SettingColor(KultKlient.INSTANCE.KULTKLIENT_COLOR.r, KultKlient.INSTANCE.KULTKLIENT_COLOR.g, KultKlient.INSTANCE.KULTKLIENT_COLOR.b))
        .visible(() -> !distance.get())
        .build()
    );

    public final Setting<SettingColor> selfColor = sgColors.add(new ColorSetting.Builder()
        .name("self-color")
        .description("The color of your skeleton.")
        .defaultValue(new SettingColor(0, 165, 255))
        .visible(() -> !distance.get())
        .build()
    );

    public SkeletonESP() {
        super(Categories.Render, Items.SKELETON_SKULL, "skeleton-esp", "Renders the skeleton of players.");
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        MatrixStack matrixStack = event.matrices;

        float g = event.tickDelta;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();

        mc.world.getEntities().forEach(entity -> {
            if (!(entity instanceof PlayerEntity player)) return;
            if (ignoreFriends.get() && Friends.get().isFriend((PlayerEntity) entity)) return;
            if (!firstPerson.get() && player == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON && !Modules.get().isActive(Freecam.class) && !Modules.get().isActive(FreeLook.class)) return;
            int rotationHoldTicks = Config.get().rotationHoldTicks.get();

            Color color;
            if (distance.get()) color = getColorFromDistance(player);
            else if (player == mc.player) color = selfColor.get();
            else if (Friends.get().isFriend(player)) color = Friends.get().color;
            else if (Enemies.get().isEnemy(player)) color = Enemies.get().color;
            else color = playersColor.get();

            Vec3d footPos = getEntityRenderPosition(player, g);
            PlayerEntityRenderer livingEntityRenderer = (PlayerEntityRenderer) (LivingEntityRenderer<?, ?>) mc.getEntityRenderDispatcher().getRenderer(player);
            PlayerEntityModel<PlayerEntity> playerEntityModel = (PlayerEntityModel) livingEntityRenderer.getModel();

            float h = MathHelper.lerpAngleDegrees(g, player.prevBodyYaw, player.bodyYaw);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) h = Rotations.serverYaw;
            float j = MathHelper.lerpAngleDegrees(g, player.prevHeadYaw, player.headYaw);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) j = Rotations.serverYaw;

            float q = player.limbAngle - player.limbDistance * (1.0F - g);
            float p = MathHelper.lerp(g, player.lastLimbDistance, player.limbDistance);
            float o = (float) player.age + g;
            float k = j - h;
            float m = player.getPitch(g);
            if (mc.player == entity && Rotations.rotationTimer < rotationHoldTicks) m = Rotations.serverPitch;

            playerEntityModel.animateModel(player, q, p, g);
            playerEntityModel.setAngles(player, q, p, o, k, m);

            boolean swimming = player.isInSwimmingPose();
            boolean sneaking = player.isSneaking();
            boolean flying = player.isFallFlying();

            ModelPart head = playerEntityModel.head;
            ModelPart leftArm = playerEntityModel.leftArm;
            ModelPart rightArm = playerEntityModel.rightArm;
            ModelPart leftLeg = playerEntityModel.leftLeg;
            ModelPart rightLeg = playerEntityModel.rightLeg;

            matrixStack.translate(footPos.x, footPos.y, footPos.z);
            if (swimming) matrixStack.translate(0, 0.35f, 0);

            matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), h + 180, true));
            if (swimming || flying) matrixStack.multiply(new Quaternion(new Vec3f(-1, 0, 0), 90 + m, true));
            if (swimming) matrixStack.translate(0, -0.95f, 0);

            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, 0, sneaking ? 1.05f : 1.4f, 0).color(color.r, color.g, color.b, color.a).next();//spine

            bufferBuilder.vertex(matrix4f, -0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.r, color.g, color.b, color.a).next();//shoulders
            bufferBuilder.vertex(matrix4f, 0.37f, sneaking ? 1.05f : 1.35f, 0).color(color.r, color.g, color.b, color.a).next();

            bufferBuilder.vertex(matrix4f, -0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.r, color.g, color.b, color.a).next();//pelvis
            bufferBuilder.vertex(matrix4f, 0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0).color(color.r, color.g, color.b, color.a).next();

            // Head
            matrixStack.push();
            matrixStack.translate(0, sneaking ? 1.05f : 1.4f, 0);
            rotate(matrixStack, head);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, 0, 0.15f, 0).color(color.r, color.g, color.b, color.a).next();
            matrixStack.pop();

            // Right Leg
            matrixStack.push();
            matrixStack.translate(0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
            rotate(matrixStack, rightLeg);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.r, color.g, color.b, color.a).next();
            matrixStack.pop();

            // Left Leg
            matrixStack.push();
            matrixStack.translate(-0.15f, sneaking ? 0.6f : 0.7f, sneaking ? 0.23f : 0);
            rotate(matrixStack, leftLeg);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.6f, 0).color(color.r, color.g, color.b, color.a).next();
            matrixStack.pop();

            // Right Arm
            matrixStack.push();
            matrixStack.translate(0.37f, sneaking ? 1.05f : 1.35f, 0);
            rotate(matrixStack, rightArm);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.r, color.g, color.b, color.a).next();
            matrixStack.pop();

            // Left Arm
            matrixStack.push();
            matrixStack.translate(-0.37f, sneaking ? 1.05f : 1.35f, 0);
            rotate(matrixStack, leftArm);
            matrix4f = matrixStack.peek().getPositionMatrix();
            bufferBuilder.vertex(matrix4f, 0, 0, 0).color(color.r, color.g, color.b, color.a).next();
            bufferBuilder.vertex(matrix4f, 0, -0.55f, 0).color(color.r, color.g, color.b, color.a).next();
            matrixStack.pop();

            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);

            if (swimming) matrixStack.translate(0, 0.95f, 0);
            if (swimming || flying) matrixStack.multiply(new Quaternion(new Vec3f(1, 0, 0), 90 + m, true));
            if (swimming) matrixStack.translate(0, -0.35f, 0);

            matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), h + 180, true));
            matrixStack.translate(-footPos.x, -footPos.y, -footPos.z);
        });

        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    private void rotate(MatrixStack matrix, ModelPart modelPart) {
        if (modelPart.roll != 0.0F) matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(modelPart.roll));
        if (modelPart.yaw != 0.0F) matrix.multiply(Vec3f.NEGATIVE_Y.getRadialQuaternion(modelPart.yaw));
        if (modelPart.pitch != 0.0F) matrix.multiply(Vec3f.NEGATIVE_X.getRadialQuaternion(modelPart.pitch));
    }

    private Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - mc.getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - mc.getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    private Color getColorFromDistance(Entity entity) {
        double distance = mc.gameRenderer.getCamera().getPos().distanceTo(entity.getPos());
        double percent = distance / 60;

        if (percent < 0 || percent > 1) {
            color.set(0, 255, 0, 255);
            return color;
        }

        int r, g;

        if (percent < 0.5) {
            r = 255;
            g = (int) (255 * percent / 0.5);
        } else {
            g = 255;
            r = 255 - (int) (255 * (percent - 0.5) / 0.5);
        }

        color.set(r, g, 0, 255);
        return color;
    }
}
