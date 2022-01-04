package kult.legacy.klient.gui.themes.kultklient;

import kult.legacy.klient.gui.renderer.GuiRenderer;
import kult.legacy.klient.gui.utils.BaseWidget;
import kult.legacy.klient.gui.widgets.WWidget;
import kult.legacy.klient.utils.render.color.Color;

public interface KultKlientWidget extends BaseWidget {
    default KultKlientGuiTheme theme() {
        return (KultKlientGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        KultKlientGuiTheme theme = theme();
        double s = theme.scale(2);

        int r = theme.roundAmount();
        Color outlineColor = theme.outlineColor.get(pressed, mouseOver);
        renderer.quadRounded(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, theme.backgroundColor.get(pressed, mouseOver), r - s);
        renderer.quadOutlineRounded(widget, outlineColor, r, s);
    }
}
