package kult.legacy.klient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import kult.legacy.klient.KultKlientLegacy;
import kult.legacy.klient.events.render.Render2DEvent;
import kult.legacy.klient.events.render.Render3DEvent;
import kult.legacy.klient.renderer.Renderer2D;
import kult.legacy.klient.renderer.ShapeMode;
import kult.legacy.klient.systems.friends.Friends;
import kult.legacy.klient.systems.modules.Categories;
import kult.legacy.klient.systems.modules.Module;
import kult.legacy.klient.systems.modules.Modules;
import kult.legacy.klient.utils.entity.EntityUtils;
import kult.legacy.klient.utils.misc.Vec3;
import kult.legacy.klient.utils.player.PlayerUtils;
import kult.legacy.klient.utils.render.WireframeEntityRenderer;
import kult.legacy.klient.utils.render.NametagUtils;
import kult.legacy.klient.utils.render.color.Color;
import kult.legacy.klient.utils.render.color.SettingColor;
import kult.legacy.klient.eventbus.EventHandler;
import kult.legacy.klient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class ESP extends Module {
    private final Color lineColor = new Color();
    private final Color sideColor = new Color();
    private final Color distanceColor = new Color();

    private final Vec3 pos1 = new Vec3();
    private final Vec3 pos2 = new Vec3();
    private final Vec3 pos = new Vec3();

    private int count;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");

    // General

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Rendering mode.")
        .defaultValue(Mode.Shader)
        .build()
    );

    public final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("Determines how the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    public final Setting<Integer> fillOpacity = sgGeneral.add(new IntSetting.Builder()
        .name("fill-opacity")
        .description("The opacity of the shape fill.")
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .defaultValue(80)
        .range(0, 255)
        .sliderMax(255)
        .build()
    );

    public final Setting<Integer> outlineWidth = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .description("The width of the shader outline.")
        .visible(() -> mode.get() == Mode.Shader)
        .defaultValue(2)
        .range(1, 10)
        .sliderRange(1, 5)
        .build()
    );

    private final Setting<Double> fadeDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("fade-distance")
        .description("The distance from an entity where the color begins to fade.")
        .defaultValue(0)
        .min(0)
        .sliderMax(12)
        .build()
    );

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Select specific entities.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    public final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Stops rendering for you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Stops rendering for friends.")
        .defaultValue(false)
        .build()
    );

    // Colors

    public final Setting<Boolean> distance = sgColors.add(new BoolSetting.Builder()
        .name("distance-based")
        .description("Changes the color depending on distance.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> playersColor = sgColors.add(new ColorSetting.Builder()
        .name("players")
        .description("The other player's color.")
        .defaultValue(new SettingColor(KultKlientLegacy.INSTANCE.MATHAX_COLOR.r, KultKlientLegacy.INSTANCE.MATHAX_COLOR.g, KultKlientLegacy.INSTANCE.MATHAX_COLOR.b))
        .visible(() -> !distance.get())
        .build()
    );


    public final Setting<SettingColor> selfColor = sgColors.add(new ColorSetting.Builder()
        .name("self")
        .description("Your own color.")
        .defaultValue(new SettingColor(0, 165, 255))
        .visible(() -> !distance.get())
        .build()
    );

    private final Setting<SettingColor> animalsColor = sgColors.add(new ColorSetting.Builder()
        .name("animals")
        .description("The animal's color.")
        .defaultValue(new SettingColor(25, 255, 25))
        .visible(() -> !distance.get())
        .build()
    );

    private final Setting<SettingColor> waterAnimalsColor = sgColors.add(new ColorSetting.Builder()
        .name("water-animals")
        .description("The water animal's color.")
        .defaultValue(new SettingColor(25, 25, 255))
        .visible(() -> !distance.get())
        .build()
    );

    private final Setting<SettingColor> monstersColor = sgColors.add(new ColorSetting.Builder()
        .name("monsters")
        .description("The monster's color.")
        .defaultValue(new SettingColor(255, 25, 25))
        .visible(() -> !distance.get())
        .build()
    );

    private final Setting<SettingColor> ambientColor = sgColors.add(new ColorSetting.Builder()
        .name("ambient")
        .description("The ambient's color.")
        .defaultValue(new SettingColor(25, 25, 25))
        .visible(() -> !distance.get())
        .build()
    );

    private final Setting<SettingColor> miscColor = sgColors.add(new ColorSetting.Builder()
        .name("misc")
        .description("The misc color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .visible(() -> !distance.get())
        .build()
    );

    public ESP() {
        super(Categories.Render, Items.RED_STAINED_GLASS, "esp", "Renders entities through walls.");
    }

    // Box

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mode.get() == Mode.CSGO) return;

        count = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (shouldSkip(entity)) continue;

            if (mode.get() == Mode.Box || mode.get() == Mode.Wireframe) drawBoundingBox(event, entity);
            count++;
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        if (distance.get()) lineColor.set(getColorFromDistance(entity));
        else lineColor.set(getColor(entity));
        sideColor.set(lineColor).a(fillOpacity.get());

        double a = getFadeAlpha(entity);

        int prevLineA = lineColor.a;
        int prevSideA = sideColor.a;

        lineColor.a *= a;
        sideColor.a *= a;

        if (mode.get() == Mode.Box) {
            double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

            Box box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, sideColor, lineColor, shapeMode.get(), 0);
        } else WireframeEntityRenderer.render(event, entity, 1, sideColor, lineColor, shapeMode.get());

        lineColor.a = prevLineA;
        sideColor.a = prevSideA;
    }

    // 2D

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mode.get() != Mode.CSGO) return;

        Renderer2D.COLOR.begin();
        count = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (shouldSkip(entity)) continue;

            Box box = entity.getBoundingBox();

            double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

            // Check corners
            pos1.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            pos2.set(0, 0, 0);

            // Bottom
            if (checkCorner(box.minX + x, box.minY + y, box.minZ + z, pos1, pos2)) continue;
            if (checkCorner(box.maxX + x, box.minY + y, box.minZ + z, pos1, pos2)) continue;
            if (checkCorner(box.minX + x, box.minY + y, box.maxZ + z, pos1, pos2)) continue;
            if (checkCorner(box.maxX + x, box.minY + y, box.maxZ + z, pos1, pos2)) continue;

            // Top
            if (checkCorner(box.minX + x, box.maxY + y, box.minZ + z, pos1, pos2)) continue;
            if (checkCorner(box.maxX + x, box.maxY + y, box.minZ + z, pos1, pos2)) continue;
            if (checkCorner(box.minX + x, box.maxY + y, box.maxZ + z, pos1, pos2)) continue;
            if (checkCorner(box.maxX + x, box.maxY + y, box.maxZ + z, pos1, pos2)) continue;

            // Setup color

            if (distance.get()) lineColor.set(getColorFromDistance(entity));
            else lineColor.set(getColor(entity));
            sideColor.set(lineColor).a(fillOpacity.get());

            double a = getFadeAlpha(entity);

            int prevLineA = lineColor.a;
            int prevSideA = sideColor.a;

            lineColor.a *= a;
            sideColor.a *= a;

            // Render
            if (shapeMode.get() != ShapeMode.Lines && sideColor.a > 0) Renderer2D.COLOR.quad(pos1.x, pos1.y, pos2.x - pos1.x, pos2.y - pos1.y, sideColor);

            if (shapeMode.get() != ShapeMode.Sides) {
                Renderer2D.COLOR.line(pos1.x, pos1.y, pos1.x, pos2.y, lineColor);
                Renderer2D.COLOR.line(pos2.x, pos1.y, pos2.x, pos2.y, lineColor);
                Renderer2D.COLOR.line(pos1.x, pos1.y, pos2.x, pos1.y, lineColor);
                Renderer2D.COLOR.line(pos1.x, pos2.y, pos2.x, pos2.y, lineColor);
            }

            // End
            lineColor.a = prevLineA;
            sideColor.a = prevSideA;

            count++;
        }

        Renderer2D.COLOR.render(null);
    }

    private boolean checkCorner(double x, double y, double z, Vec3 min, Vec3 max) {
        pos.set(x, y, z);
        if (!NametagUtils.to2D(pos, 1)) return true;

        // Check Min
        if (pos.x < min.x) min.x = pos.x;
        if (pos.y < min.y) min.y = pos.y;
        if (pos.z < min.z) min.z = pos.z;

        // Check Max
        if (pos.x > max.x) max.x = pos.x;
        if (pos.y > max.y) max.y = pos.y;
        if (pos.z > max.z) max.z = pos.z;

        return false;
    }

    // EntityShaders

    public boolean shouldDrawOutline(Entity entity) {
        return mode.get() == Mode.Shader && isActive() && getOutlineColor(entity) != null;
    }

    public Color getOutlineColor(Entity entity) {
        if (!entities.get().getBoolean(entity.getType())) return null;
        Color color;
        if (distance.get()) color = getColorFromDistance(entity);
        else color = getColor(entity);
        double alpha = getFadeAlpha(entity);
        return lineColor.set(color).a((int) (alpha * 255));
    }

    // Stuff

    private double getFadeAlpha(Entity entity) {
        double dist = PlayerUtils.distanceToCamera(entity.getX() + entity.getWidth() / 2, entity.getY() + entity.getHeight() / 2, entity.getZ() + entity.getWidth() / 2);
        double fadeDist = fadeDistance.get().floatValue() * fadeDistance.get().floatValue();
        double alpha = 1;
        if (dist <= fadeDist) alpha = (float) (dist / fadeDist);
        if (alpha <= 0.075) alpha = 0;
        return alpha;
    }

    public Color getColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            if (entity.equals(mc.getCameraEntity())) return PlayerUtils.getPlayerColor(((PlayerEntity) entity), selfColor.get());
            else return PlayerUtils.getPlayerColor(((PlayerEntity) entity), playersColor.get());
        }

        return switch (entity.getType().getSpawnGroup()) {
            case CREATURE -> animalsColor.get();
            case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE -> waterAnimalsColor.get();
            case MONSTER -> monstersColor.get();
            case AMBIENT -> ambientColor.get();
            default -> miscColor.get();
        };
    }

    private Color getColorFromDistance(Entity entity) {
        // Credit to Icy from Stackoverflow
        double distance = mc.gameRenderer.getCamera().getPos().distanceTo(entity.getPos());
        double percent = distance / 60;

        if (percent < 0 || percent > 1) {
            distanceColor.set(0, 255, 0, 255);
            return distanceColor;
        }

        int r, g;

        if (percent < 0.5) {
            r = 255;
            g = (int) (255 * percent / 0.5);  // Closer to 0.5, closer to yellow (255,255,0)
        } else {
            g = 255;
            r = 255 - (int) (255 * (percent - 0.5) / 0.5); // Closer to 1.0, closer to green (0,255,0)
        }

        distanceColor.set(r, g, 0, 255);
        return distanceColor;
    }

    private boolean shouldSkip(Entity entity) {
        if ((!Modules.get().isActive(Freecam.class) && entity == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON) || !entities.get().getBoolean(entity.getType())) return true;
        if (entity instanceof PlayerEntity player) return shouldSkipPlayer(player);
        return !EntityUtils.isInRenderDistance(entity);
    }

    private boolean shouldSkipPlayer(PlayerEntity player) {
        if (ignoreSelf.get() && player == mc.player) return true;
        return ignoreFriends.get() && Friends.get().isFriend(player);
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    public boolean isShader() {
        return isActive() && mode.get() == Mode.Shader;
    }

    public enum Mode {
        Box,
        Wireframe,
        Shader,
        CSGO;

        @Override
        public String toString() {
            if (this == CSGO) return "CS:GO";
            return super.toString();
        }
    }
}
