package kult.klient.systems.hud;

import kult.klient.KultKlient;
import kult.klient.eventbus.EventHandler;
import kult.klient.events.render.Render2DEvent;
import kult.klient.gui.screens.hud.HudEditorScreen;
import kult.klient.gui.screens.hud.HudElementScreen;
import kult.klient.settings.*;
import kult.klient.systems.System;
import kult.klient.systems.hud.modules.*;
import kult.klient.utils.misc.NbtUtils;
import kult.klient.utils.render.AlignmentX;
import kult.klient.utils.render.AlignmentY;
import kult.klient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.*;
import java.util.function.Predicate;

import static kult.klient.KultKlient.mc;

public class HUD extends System<HUD> {
    public final Settings settings = new Settings();

    private final HudRenderer RENDERER = new HudRenderer();

    public final List<HudElement> elements = new ArrayList<>();
    private final HudElementLayer mainInfo, moduleInfo, breakingLooking, coords, lag, modules, invPot, binds, radar, itemsArmor, crosshair, crosshair2;

    public boolean active = true;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgEditor = settings.createGroup("Editor");

    // General

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the HUD.")
        .defaultValue(1)
        .min(0.75)
        .sliderRange(0.75, 5)
        .build()
    );

    public final Setting<SettingColor> primaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("primary-color")
        .description("Primary color of text.")
        .defaultValue(new SettingColor(KultKlient.INSTANCE.KULTKLIENT_COLOR.r, KultKlient.INSTANCE.KULTKLIENT_COLOR.g, KultKlient.INSTANCE.KULTKLIENT_COLOR.b))
        .build()
    );

    public final Setting<SettingColor> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("secondary-color")
        .description("Secondary color of text.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    // Editor

    public final Setting<Integer> snappingRange = sgEditor.add(new IntSetting.Builder()
        .name("snapping-range")
        .description("Snapping range in editor.")
        .defaultValue(6)
        .build()
    );

    public HUD() {
        super("HUD");

        // MAIN INFO
        mainInfo = new HudElementLayer(RENDERER, elements, AlignmentX.Left, AlignmentY.Top, 2, 2);
        // Modules
        mainInfo.add(new WatermarkHud(this));
        mainInfo.add(new WelcomeHud(this));
        mainInfo.add(new FPSHud(this));
        mainInfo.add(new PingHud(this));
        mainInfo.add(new TPSHud(this));
        mainInfo.add(new SpeedHud(this));
        mainInfo.add(new ServerHud(this));
        mainInfo.add(new ServerBrandHud(this));
        mainInfo.add(new DurabilityHud(this));
        mainInfo.add(new BiomeHud(this));
        mainInfo.add(new PlayerModelHud(this));
        mainInfo.add(new DateHud(this));
        mainInfo.add(new RealTimeHud(this));
        mainInfo.add(new GameTimeHud(this));
        mainInfo.add(new MusicHud(this));

        // MODULE INFO
        moduleInfo = new HudElementLayer(RENDERER, elements, AlignmentX.Left, AlignmentY.Top, 100, 200);
        // Modules
        moduleInfo.add(new ModuleInfoHud(this));

        // LOOKING AT & BREAKING
        breakingLooking = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Center, 0, 215);
        // Modules
        breakingLooking.add(new LookingAtHud(this));
        breakingLooking.add(new BreakingBlockHud(this));

        // COORDS
        coords = new HudElementLayer(RENDERER, elements, AlignmentX.Left, AlignmentY.Bottom, 2, 2);
        // Modules
        coords.add(new PositionHud(this));
        coords.add(new RotationHud(this));

        // LAG
        lag = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Top, 0, 2);
        // Modules
        lag.add(new LagNotifierHud(this));
        lag.add(new BigRatHud(this));

        // MODULES
        modules = new HudElementLayer(RENDERER, elements, AlignmentX.Right, AlignmentY.Bottom, 2, 2);
        // Modules
        modules.add(new ActiveModulesHud(this));

        // INVPOT
        invPot = new HudElementLayer(RENDERER, elements, AlignmentX.Right, AlignmentY.Top, 2, 2);
        // Modules
        invPot.add(new InventoryViewerHud(this));
        invPot.add(new ContainerViewerHud(this));
        invPot.add(new PotionTimersHud(this));

        // BINDS
        binds = new HudElementLayer(RENDERER, elements, AlignmentX.Right, AlignmentY.Center, 2, 5);
        // Modules
        binds.add(new VisualBinds(this));

        // TEXT RADAR
        radar = new HudElementLayer(RENDERER, elements, AlignmentX.Left, AlignmentY.Center, 2, 100);
        // Modules
        radar.add(new CombatInfoHud(this));
        radar.add(new TextRadarHud(this));

        // ITEMS & ARMOR
        itemsArmor = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Center, -150, -50);
        // Modules
        itemsArmor.add(new TotemHud(this));
        itemsArmor.add(new CrystalHud(this));
        itemsArmor.add(new EGapHud(this));
        itemsArmor.add(new XPBottleHud(this));
        itemsArmor.add(new ObsidianHud(this));
        itemsArmor.add(new BedHud(this));
        itemsArmor.add(new ArmorHud(this));

        // CROSSHAIR
        crosshair = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Center, 0, 0);
        // Modules
        crosshair.add(new HoleHud(this));

        // CROSSHAIR 2
        crosshair2 = new HudElementLayer(RENDERER, elements, AlignmentX.Center, AlignmentY.Center, 0, 0);
        // Modules
        crosshair2.add(new CompassHud(this));

        align();
    }

    private void align() {
        RENDERER.begin(scale.get(), 0, true);

        mainInfo.align();
        moduleInfo.align();
        breakingLooking.align();
        coords.align();
        lag.align();
        modules.align();
        invPot.align();
        binds.align();
        radar.align();
        itemsArmor.align();
        crosshair.align();
        crosshair2.align();

        RENDERER.end();
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (mc.options.debugEnabled || mc.options.hudHidden) return;

        render(event.tickDelta, hudElement -> isEditorScreen() || (hudElement.active && active));
    }

    public void render(float delta, Predicate<HudElement> shouldRender) {
        RENDERER.begin(scale.get(), delta, false);

        for (HudElement element : elements) {
            if (shouldRender.test(element)) {
                element.update(RENDERER);
                element.render(RENDERER);
            }
        }

        RENDERER.end();
    }

    public static boolean isEditorScreen() {
        return mc.currentScreen instanceof HudEditorScreen || mc.currentScreen instanceof HudElementScreen;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putBoolean("active", active);
        tag.put("settings", settings.toTag());

        tag.put("elements", NbtUtils.listToTag(elements));

        return tag;
    }

    @Override
    public HUD fromTag(NbtCompound tag) {
        if (tag.contains("active")) active = tag.getBoolean("active");
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));
        if (tag.contains("elements")) {
            NbtList elementsTag = tag.getList("elements", 10);

            for (NbtElement t : elementsTag) {
                NbtCompound elementTag = (NbtCompound) t;

                for (HudElement element : elements) {
                    if (element.name.equals(elementTag.getString("name"))) {
                        element.fromTag(elementTag);
                        break;
                    }
                }
            }
        }

        return super.fromTag(tag);
    }

    public final Runnable reset = () -> {
        align();
        elements.forEach(element -> {
            element.active = element.defaultActive;
            element.settings.forEach(group -> group.forEach(Setting::reset));
        });
    };
}
