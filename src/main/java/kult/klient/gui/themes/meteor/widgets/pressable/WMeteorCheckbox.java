package kult.klient.gui.themes.meteor.widgets.pressable;

import kult.klient.gui.renderer.GuiRenderer;
import kult.klient.gui.themes.meteor.MeteorGuiTheme;
import kult.klient.gui.themes.meteor.MeteorWidget;
import kult.klient.gui.widgets.pressable.WCheckbox;
import kult.klient.utils.Utils;

public class WMeteorCheckbox extends WCheckbox implements MeteorWidget {
    private double animProgress;

    public WMeteorCheckbox(boolean checked) {
        super(checked);
        animProgress = checked ? 1 : 0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorGuiTheme theme = theme();

        animProgress += (checked ? 1 : -1) * delta * 14;
        animProgress = Utils.clamp(animProgress, 0, 1);

        renderBackground(renderer, this, pressed, mouseOver);

        if (animProgress > 0) {
            double cs = (width - theme.scale(2)) / 1.75 * animProgress;
            renderer.quadRounded(x + (width - cs) / 2, y + (height - cs) / 2, cs, cs, theme.checkboxColor.get(), theme.roundAmount());
        }
    }
}
