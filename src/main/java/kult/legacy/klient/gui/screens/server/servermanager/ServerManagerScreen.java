package kult.legacy.klient.gui.screens.server.servermanager;

import kult.legacy.klient.gui.GuiTheme;
import kult.legacy.klient.gui.WindowScreen;
import kult.legacy.klient.gui.widgets.containers.WContainer;
import kult.legacy.klient.gui.widgets.containers.WHorizontalList;
import kult.legacy.klient.gui.widgets.pressable.WButton;
import kult.legacy.klient.utils.misc.IGetter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class ServerManagerScreen extends WindowScreen {
    private final MultiplayerScreen multiplayerScreen;

    public ServerManagerScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen) {
        super(theme, "Server Manager");
        this.parent = multiplayerScreen;
        this.multiplayerScreen = multiplayerScreen;
    }

    @Override
    public void initWidgets() {
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();
        addButton(l, "Find Servers", () -> new ServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "Clean Up", () -> new ServerCleanUpScreen(theme, multiplayerScreen, this));
    }

    private void addButton(WContainer c, String text, IGetter<Screen> action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = () -> client.setScreen(action.get());
    }
}
