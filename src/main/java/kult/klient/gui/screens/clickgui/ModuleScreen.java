package kult.klient.gui.screens.clickgui;

import kult.klient.gui.widgets.containers.WContainer;
import kult.klient.gui.widgets.containers.WHorizontalList;
import kult.klient.gui.widgets.containers.WSection;
import kult.klient.gui.widgets.pressable.WCheckbox;
import kult.klient.events.kultklient.ModuleBindChangedEvent;
import kult.klient.gui.GuiTheme;
import kult.klient.gui.WindowScreen;
import kult.klient.gui.utils.Cell;
import kult.klient.gui.widgets.WKeyBind;
import kult.klient.gui.widgets.WWidget;
import kult.klient.systems.modules.Module;
import kult.klient.systems.modules.Modules;
import kult.klient.utils.Utils;
import kult.klient.eventbus.EventHandler;
import kult.klient.utils.misc.NbtUtils;
import net.minecraft.nbt.NbtCompound;

public class ModuleScreen extends WindowScreen {
    public final Module module;

    private WContainer settingsContainer;
    private WKeyBind keybind;

    public ModuleScreen(GuiTheme theme, Module module) {
        super(theme, module.title);

        this.module = module;
    }

    @Override
    public void initWidgets() {
        // Description
        add(theme.label(module.description, Utils.getWindowWidth() / 2.0));

        // Settings
        if (module.settings.groups.size() > 0) {
            settingsContainer = add(theme.verticalList()).expandX().widget();
            settingsContainer.add(theme.settings(module.settings)).expandX();
        }

        // Custom widget
        WWidget widget = module.getWidget(theme);

        if (widget != null) {
            add(theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = add(widget);
            if (widget instanceof WContainer) cell.expandX();
        }

        // Bind
        WSection section = add(theme.section("Bind", true)).expandX().widget();
        keybind = section.add(theme.keybind(module.keybind)).expandX().widget();
        keybind.actionOnSet = () -> Modules.get().setModuleToBind(module);

        // Toggle on bind release
        WHorizontalList tobr = section.add(theme.horizontalList()).widget();

        tobr.add(theme.label("Toggle on bind release: "));
        WCheckbox tobrC = tobr.add(theme.checkbox(module.toggleOnBindRelease)).widget();
        tobrC.action = () -> module.toggleOnBindRelease = tobrC.checked;

        // Module
        add(theme.section("Module", true)).expandX().widget();

        // Bottom
        WHorizontalList bottom = add(theme.horizontalList()).expandX().widget();

        // Messages
        bottom.add(theme.label("Toggle message: "));
        WCheckbox messageToggle = bottom.add(theme.checkbox(module.isMessageEnabled())).expandCellX().widget();
        messageToggle.action = () -> {
            if (module.isMessageEnabled() != messageToggle.checked) module.setToggleMessage(messageToggle.checked);
        };

        // Toasts
        bottom.add(theme.label("Toggle toast: "));
        WCheckbox toastToggle = bottom.add(theme.checkbox(module.isToastEnabled())).widget();
        toastToggle.action = () -> {
            if (module.isToastEnabled() != toastToggle.checked) module.setToggleToast(toastToggle.checked);
        };

        // Bottom 2
        WHorizontalList bottom2 = add(theme.horizontalList()).expandX().widget();

        //   Active
        bottom2.add(theme.label("Active: "));
        WCheckbox active = bottom2.add(theme.checkbox(module.isActive())).expandCellX().widget();
        active.action = () -> {
            if (module.isActive() != active.checked) module.toggle();
        };

        //   Visible
        bottom2.add(theme.label("Visible: "));
        WCheckbox visible = bottom2.add(theme.checkbox(module.isVisible())).widget();
        visible.action = () -> {
            if (module.isVisible() != visible.checked) module.setVisible(visible.checked);
        };
    }

    @Override
    public void tick() {
        super.tick();

        module.settings.tick(settingsContainer, theme);
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        keybind.reset();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(module.title, module.toTag());
    }

    @Override
    public boolean fromClipboard() {
        NbtCompound clipboard = NbtUtils.fromClipboard(module.toTag());

        if (clipboard != null) {
            module.fromTag(clipboard);
            return true;
        }

        return false;
    }
}
