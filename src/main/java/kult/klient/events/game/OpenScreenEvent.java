package kult.klient.events.game;

import kult.klient.events.packets.PacketEvent;
import kult.klient.events.Cancellable;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends Cancellable {
    private static final OpenScreenEvent INSTANCE = new OpenScreenEvent();

    public Screen screen;

    public static OpenScreenEvent get(Screen screen) {
        INSTANCE.setCancelled(false);
        INSTANCE.screen = screen;
        return INSTANCE;
    }

    public static class getOpenedScreen extends PacketEvent {

        public Screen screen;

        public getOpenedScreen(Screen screen) {
            this.screen = screen;
        }

        public Screen getScreen() {
            return screen;
        }
    }
}
