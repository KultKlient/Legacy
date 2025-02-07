package kult.klient.gui.tabs.builtin;

import kult.klient.gui.widgets.containers.WTable;
import kult.klient.gui.widgets.input.WTextBox;
import kult.klient.gui.widgets.pressable.WButton;
import kult.klient.gui.widgets.pressable.WMinus;
import kult.klient.gui.widgets.pressable.WPlus;
import kult.klient.eventbus.EventHandler;
import kult.klient.eventbus.EventPriority;
import kult.klient.events.kultklient.KeyEvent;
import kult.klient.events.kultklient.MouseButtonEvent;
import kult.klient.gui.GuiTheme;
import kult.klient.gui.WindowScreen;
import kult.klient.gui.renderer.GuiRenderer;
import kult.klient.gui.tabs.Tab;
import kult.klient.gui.tabs.TabScreen;
import kult.klient.gui.tabs.WindowTabScreen;
import kult.klient.gui.widgets.WKeyBind;
import kult.klient.systems.macros.Macro;
import kult.klient.systems.macros.Macros;
import kult.klient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screen.Screen;

import static kult.klient.KultKlient.mc;

public class MacrosTab extends Tab {
    public MacrosTab() {
        super("Macros");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new MacrosScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof MacrosScreen;
    }

    public static class MacrosScreen extends WindowTabScreen {
        public MacrosScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            // Macros
            if (Macros.get().getAll().size() > 0) {
                WTable table = add(theme.table()).expandX().widget();

                for (Macro macro : Macros.get()) {
                    table.add(theme.label(macro.name + " (" + macro.keybind + ")"));

                    WButton edit = table.add(theme.button(GuiRenderer.EDIT)).expandCellX().right().widget();
                    edit.action = () -> mc.setScreen(new MacroEditorScreen(theme, macro));

                    WMinus remove = table.add(theme.minus()).widget();
                    remove.action = () -> {
                        Macros.get().remove(macro);

                        clear();
                        initWidgets();
                    };

                    table.row();
                }
            }

            // New
            WButton create = add(theme.button("Create")).expandX().widget();
            create.action = () -> mc.setScreen(new MacroEditorScreen(theme, null));
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Macros.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Macros.get());
        }
    }

    public static class MacroEditorScreen extends WindowScreen {
        private final boolean isNew;

        private final Macro macro;

        private WKeyBind keybind;
        private boolean binding;

        public MacroEditorScreen(GuiTheme theme, Macro m) {
            super(theme, m == null ? "Create Macro" : "Edit Macro");

            isNew = m == null;
            macro = isNew ? new Macro() : m;
        }

        @Override
        public void initWidgets() {
            initWidgets(macro);
        }

        private void initWidgets(Macro m) {
            // Name
            WTable t = add(theme.table()).widget();

            t.add(theme.label("Name:"));
            WTextBox name = t.add(theme.textBox(m == null ? "" : macro.name)).minWidth(400).expandX().widget();
            name.setFocused(true);
            name.action = () -> macro.name = name.get().trim();
            t.row();

            // Messages
            t.add(theme.label("Messages:")).padTop(4).top();
            WTable lines = t.add(theme.table()).widget();
            initTable(lines);

            // Bind
            keybind = add(theme.keybind(macro.keybind)).expandX().widget();
            keybind.actionOnSet = () -> binding = true;

            // Apply
            WButton apply = add(theme.button(isNew ? "Add" : "Apply")).expandX().widget();
            apply.action = () -> {
                if (isNew) {
                    if (macro.name != null && !macro.name.isEmpty() && macro.messages.size() > 0 && macro.keybind.isSet()) {
                        Macros.get().add(macro);
                        onClose();
                    }
                } else {
                    Macros.get().save();
                    onClose();
                }
            };

            enterAction = apply.action;
        }

        private void initTable(WTable lines) {
            if (macro.messages.isEmpty()) macro.addMessage("");

            for (int i = 0; i < macro.messages.size(); i++) {
                int ii = i;

                WTextBox line = lines.add(theme.textBox(macro.messages.get(i))).minWidth(400).expandX().widget();
                line.action = () -> macro.messages.set(ii, line.get().trim());

                if (i != macro.messages.size() - 1) {
                    WMinus remove = lines.add(theme.minus()).widget();
                    remove.action = () -> {
                        macro.removeMessage(ii);

                        clear();
                        initWidgets(macro);
                    };
                } else {
                    WPlus add = lines.add(theme.plus()).widget();
                    add.action = () -> {
                        macro.addMessage("");

                        clear();
                        initWidgets(macro);
                    };
                }

                lines.row();
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        private void onKey(KeyEvent event) {
            if (onAction(true, event.key)) event.cancel();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        private void onButton(MouseButtonEvent event) {
            if (onAction(false, event.button)) event.cancel();
        }

        private boolean onAction(boolean isKey, int value) {
            if (binding) {
                keybind.onAction(isKey, value);

                binding = false;
                return true;
            }

            return false;
        }
    }
}
