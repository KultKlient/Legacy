package kult.legacy.klient;

import kult.legacy.klient.events.kultklientlegacy.CharTypedEvent;
import kult.legacy.klient.events.kultklientlegacy.KeyEvent;
import kult.legacy.klient.events.kultklientlegacy.MouseButtonEvent;
import kult.legacy.klient.events.world.TickEvent;
import kult.legacy.klient.gui.GuiThemes;
import kult.legacy.klient.gui.renderer.GuiRenderer;
import kult.legacy.klient.gui.tabs.Tabs;
import kult.legacy.klient.eventbus.EventBus;
import kult.legacy.klient.eventbus.EventHandler;
import kult.legacy.klient.eventbus.IEventBus;
import kult.legacy.klient.music.Music;
import kult.legacy.klient.renderer.*;
import kult.legacy.klient.renderer.text.Fonts;
import kult.legacy.klient.systems.Systems;
import kult.legacy.klient.systems.modules.Categories;
import kult.legacy.klient.systems.modules.combat.*;
import kult.legacy.klient.systems.modules.client.*;
import kult.legacy.klient.systems.modules.render.Background;
import kult.legacy.klient.systems.modules.render.Zoom;
import kult.legacy.klient.systems.modules.render.hud.HUD;
import kult.legacy.klient.utils.Version;
import kult.legacy.klient.utils.misc.WindowUtils;
import kult.legacy.klient.utils.misc.FakeClientPlayer;
import kult.legacy.klient.utils.misc.KeyBind;
import kult.legacy.klient.utils.misc.Names;
import kult.legacy.klient.utils.misc.input.KeyAction;
import kult.legacy.klient.utils.misc.input.KeyBinds;
import kult.legacy.klient.utils.network.KultKlientExecutor;
import kult.legacy.klient.utils.player.DamageUtils;
import kult.legacy.klient.utils.player.EChestMemory;
import kult.legacy.klient.utils.player.Rotations;
import kult.legacy.klient.utils.render.EntityShaders;
import kult.legacy.klient.utils.render.color.Color;
import kult.legacy.klient.utils.render.color.RainbowColors;
import kult.legacy.klient.utils.world.BlockIterator;
import kult.legacy.klient.utils.world.BlockUtils;
import kult.legacy.klient.systems.config.Config;
import kult.legacy.klient.systems.modules.Modules;
import kult.legacy.klient.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

/*/------------------------------------------------------------------/*/
/*/ THIS CLIENT IS A FORK OF METEOR CLIENT BY MINEGAME159 & SEASNAIL /*/
/*/ https://meteorclient.com                                         /*/
/*/ https://github.com/MeteorDevelopment/meteor-client               /*/
/*/------------------------------------------------------------------/*/
/*/ Music player used from Motor Tunez made by JFronny               /*/
/*/ https://github.com/JFronny/MotorTunez                            /*/
/*/------------------------------------------------------------------/*/

public class KultKlientLegacy implements ClientModInitializer {
    public static MinecraftClient mc;
    public static KultKlientLegacy INSTANCE;
    public static final IEventBus EVENT_BUS = new EventBus();

    public static final File GAME_FOLDER = new File(FabricLoader.getInstance().getGameDir().toString());
    public static final File FOLDER = new File(GAME_FOLDER, "KultKlient/Legacy");
    public static final File VERSION_FOLDER = new File(FOLDER + "/" + Version.getMinecraft());
    public static final File MUSIC_FOLDER = new File(FOLDER + "/Music");

    public final Color MATHAX_COLOR = new Color(230, 75, 100, 255);
    public final int MATHAX_COLOR_INT = Color.fromRGBA(230, 75, 100, 255);
    public final Color MATHAX_BACKGROUND_COLOR = new Color(30, 30, 45, 255);
    public final int MATHAX_BACKGROUND_COLOR_INT = Color.fromRGBA(30, 30, 45, 255);

    public static final Logger LOG = LogManager.getLogger();
    public static String logPrefix = "[KultKlient Legacy] ";

    public static final String URL = "https://kultklient.github.io";
    public static final String API_URL = "https://kultklient.github.io/kultklient.github.io-api/";

    public static List<String> getDeveloperUUIDs() {
        return Arrays.asList(

            // MATEJKO06
            "3e24ef27e66d45d2bf4b2c7ade68ff47",
            "7c73f84473c33a7d9978004ba0a6436e"

        );
    }

    public static List<String> getSplashes() {
        return Arrays.asList(

            // SPLASHES
            Formatting.RED + "KultKlient on top!",
            Formatting.GRAY + "KultKollektive" + Formatting.RED + " based god",
            Formatting.RED + "KultKlientClient.xyz",
            Formatting.RED + "KultKlientClient.xyz/Discord",
            Formatting.RED + Version.getStylized(),
            Formatting.RED + Version.getMinecraft(),

            // MEME SPLASHES
            Formatting.YELLOW + "cope",
            Formatting.YELLOW + "I am funny -HiIAmFunny",
            Formatting.YELLOW + "IntelliJ IDEa",
            Formatting.YELLOW + "I <3 nns",
            Formatting.YELLOW + "haha 69",
            Formatting.YELLOW + "420 XDDDDDD",
            Formatting.YELLOW + "ayy",
            Formatting.YELLOW + "too ez",
            Formatting.YELLOW + "owned",
            Formatting.YELLOW + "your mom :joy:",
            Formatting.YELLOW + "BOOM BOOM BOOM!",
            Formatting.YELLOW + "I <3 forks",
            Formatting.YELLOW + "based",
            Formatting.YELLOW + "Pog",
            Formatting.YELLOW + "Big Rat on top!",
            Formatting.YELLOW + "bigrat.monster",

            // PERSONALIZED
            Formatting.YELLOW + "You're cool, " + Formatting.GRAY + MinecraftClient.getInstance().getSession().getUsername(),
            Formatting.YELLOW + "Owning with " + Formatting.GRAY + MinecraftClient.getInstance().getSession().getUsername(),
            Formatting.YELLOW + "Who is " + Formatting.GRAY + MinecraftClient.getInstance().getSession().getUsername() + Formatting.YELLOW + "?"

        );
    }

    @Override
    public void onInitializeClient() {
        // Instance
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }

        // Log
        LOG.info(logPrefix + "Initializing KultKlient Legacy " + Version.getStylized() + "...");

        // Global Minecraft client accessor
        mc = MinecraftClient.getInstance();

        // Icon & Title
        WindowUtils.KultKlient.setIcon();
        WindowUtils.KultKlient.setTitleLoading();

        // Register event handlers
        EVENT_BUS.registerLambdaFactory("kult.legacy.klient", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        // Pre-load
        Systems.addPreLoadTask(() -> {
            if (!Modules.get().getFile().exists()) {
                // ACTIVATE
                Modules.get().get(CapesModule.class).forceToggle(true); // CAPES
                Modules.get().get(DiscordRPC.class).forceToggle(true); // DISCORD RPC
                Modules.get().get(Background.class).forceToggle(true); // BACKGROUND
                Modules.get().get(MiddleClickFriend.class).forceToggle(true); // MIDDLE CLICK FRIEND
                Modules.get().get(HUD.class).forceToggle(true); // HUD

                // VISIBILITY
                Modules.get().get(ClientSpoof.class).setVisible(false); // CLIENT SPOOF
                Modules.get().get(CapesModule.class).setVisible(false); // CAPES
                Modules.get().get(DiscordRPC.class).setVisible(false); // DISCORD RPC
                Modules.get().get(Background.class).setVisible(false); // BACKGROUND
                Modules.get().get(MiddleClickFriend.class).setVisible(false); // MIDDLE CLICK FRIEND
                Modules.get().get(Zoom.class).setVisible(false); // ZOOM
                Modules.get().get(HUD.class).setVisible(false); // HUD

                // KEYBINDS
                Modules.get().get(Zoom.class).keybind.set(KeyBind.fromKey(GLFW.GLFW_KEY_C)); // ZOOM

                // KEYBIND OPTIONS
                Modules.get().get(Zoom.class).toggleOnBindRelease = true; // ZOOM

                // TOASTS
                Modules.get().get(AnchorAura.class).setToggleToast(true); // ANCHOR AURA
                Modules.get().get(BedAura.class).setToggleToast(true); // BED AURA
                Modules.get().get(CEVBreaker.class).setToggleToast(true); // CEV BREAKER
                Modules.get().get(CrystalAura.class).setToggleToast(true); // CRYSTAL AURA
                Modules.get().get(KillAura.class).setToggleToast(true); // KILL AURA

                // MESSAGES
                Modules.get().get(Zoom.class).setToggleMessage(false); // ZOOM

                // RESET HUD LOCATIONS
                Modules.get().get(HUD.class).reset.run(); // HUD
            }
        });

        // Pre init
        Utils.init();
        GL.init();
        Shaders.init();
        Renderer2D.init();
        EntityShaders.initOutlines();
        KultKlientExecutor.init();
        RainbowColors.init();
        BlockIterator.init();
        EChestMemory.init();
        Rotations.init();
        Names.init();
        FakeClientPlayer.init();
        PostProcessRenderer.init();
        Tabs.init();
        GuiThemes.init();
        Fonts.init();
        DamageUtils.init();
        BlockUtils.init();
        Music.init();

        // Register module categories
        Categories.init();

        // Load systems
        Systems.init();

        // Event bus
        EVENT_BUS.subscribe(this);

        // Sorting modules
        Modules.get().sortModules();

        // Load saves
        Systems.load();

        // Post init
        Fonts.load();
        GuiRenderer.init();
        GuiThemes.postInit();

        // Title
        WindowUtils.KultKlient.setTitleLoaded();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            net.arikia.dev.drpc.DiscordRPC.discordClearPresence();
            net.arikia.dev.drpc.DiscordRPC.discordShutdown();
            Systems.save();
            GuiThemes.save();
        }));

        // Icon & Title
        ClientSpoof cs = Modules.get().get(ClientSpoof.class);
        if (cs.isActive() && cs.changeWindowIcon()) WindowUtils.Meteor.setIcon();
        else WindowUtils.KultKlient.setIcon();
        if (cs.isActive() && cs.changeWindowTitle()) WindowUtils.Meteor.setTitle();
        else WindowUtils.KultKlient.setTitle();

        // Log
        LOG.info(logPrefix + "KultKlient Legacy " + Version.getStylized() + " initialized!");
    }

    // Music Volume

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (Music.player == null) return;
        if (Music.player.getVolume() != Config.get().musicVolume) Music.player.setVolume(Config.get().musicVolume);
    }

    // Developer

    public static boolean isDeveloper(String uuid) {
        uuid = uuid.replace("-", "");
        return getDeveloperUUIDs().contains(uuid);
    }

    // Click GUI keys

    @EventHandler
    private void onKeyGUI(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_CLICK_GUI.matchesKey(event.key, 0)) {
            if (mc.getOverlay() instanceof SplashOverlay) return;
            if (Utils.canOpenClickGUI()) openClickGUI();
        }
    }

    @EventHandler
    private void onMouseButtonGUI(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && event.button != GLFW.GLFW_MOUSE_BUTTON_LEFT && KeyBinds.OPEN_CLICK_GUI.matchesMouse(event.button) && Utils.canOpenClickGUI()) openClickGUI();
    }

    // Click GUI

    private void openClickGUI() {
        Tabs.get().get(0).openScreen(GuiThemes.get());
    }

    // Console

    @EventHandler
    private void onCharTyped(CharTypedEvent event) {
        if (mc.currentScreen != null || !Config.get().prefixOpensConsole || Config.get().prefix.isBlank()) return;

        if (event.c == Config.get().prefix.charAt(0)) {
            mc.setScreen(new ChatScreen(Config.get().prefix));
            event.cancel();
        }
    }
}
