package kult.legacy.klient.systems.modules.render.hud.modules;

import kult.legacy.klient.systems.modules.render.hud.DoubleTextHudElement;
import kult.legacy.klient.systems.modules.render.hud.HUD;
import kult.legacy.klient.systems.modules.render.hud.TripleTextHudElement;
import kult.legacy.klient.utils.Utils;

public class GameTimeHud extends DoubleTextHudElement {

    public GameTimeHud(HUD hud) {
        super(hud, "game-time", "Displays the in-game time.", true);
    }

    @Override
    protected String getLeft() {
        return "Game Time: ";
    }

    @Override
    protected String getRight() {
        if (isInEditor()) return "12:00:00";

        return Utils.getWorldTime();
    }
}
