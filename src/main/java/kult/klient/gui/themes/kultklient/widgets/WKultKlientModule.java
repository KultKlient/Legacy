package kult.klient.gui.themes.kultklient.widgets;

import kult.klient.KultKlient;
import kult.klient.gui.renderer.GuiRenderer;
import kult.klient.gui.themes.kultklient.KultKlientGuiTheme;
import kult.klient.gui.themes.kultklient.KultKlientWidget;
import kult.klient.gui.utils.AlignmentX;
import kult.klient.gui.widgets.pressable.WPressable;
import kult.klient.systems.modules.Module;
import kult.klient.utils.Utils;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WKultKlientModule extends WPressable implements KultKlientWidget {
    private final Module module;

    private double titleWidth;

    private double animationProgress1;

    private double animationProgress2;

    public WKultKlientModule(Module module) {
        this.module = module;

        if (module.isActive()) {
            animationProgress1 = 1;
            animationProgress2 = 1;
        } else {
            animationProgress1 = 0;
            animationProgress2 = 0;
        }
    }

    @Override
    public double pad() {
        return theme.scale(4);
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme.textWidth(module.title);

        width = pad + titleWidth + pad;
        height = pad + theme.textHeight() + pad;
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) KultKlient.mc.setScreen(theme.moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        KultKlientGuiTheme theme = theme();
        double pad = pad();

        animationProgress1 += delta * 4 * ((module.isActive() || mouseOver) ? 1 : -1);
        animationProgress1 = Utils.clamp(animationProgress1, 0, 1);

        animationProgress2 += delta * 6 * (module.isActive() ? 1 : -1);
        animationProgress2 = Utils.clamp(animationProgress2, 0, 1);

        if (animationProgress1 > 0) {
            renderer.quad(x, y, width * animationProgress1, height, theme.moduleBackground.get());
        }
        if (animationProgress2 > 0) {
            renderer.quad(x, y + height * (1 - animationProgress2), theme.scale(2), height * animationProgress2, theme.mainColor.get());
        }

        double x = this.x + pad;
        double w = width - pad * 2;

        if (theme.moduleAlignment.get() == AlignmentX.Center) {
            x += w / 2 - titleWidth / 2;
        } else if (theme.moduleAlignment.get() == AlignmentX.Right) {
            x += w - titleWidth;
        }

        renderer.text(module.title, x, y + pad, theme.textColor.get(), false);
    }
}
