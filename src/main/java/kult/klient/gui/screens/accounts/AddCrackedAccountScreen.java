package kult.klient.gui.screens.accounts;

import kult.klient.gui.widgets.containers.WTable;
import kult.klient.gui.widgets.input.WTextBox;
import kult.klient.gui.GuiTheme;
import kult.klient.systems.accounts.Accounts;
import kult.klient.systems.accounts.types.CrackedAccount;

public class AddCrackedAccountScreen extends AddAccountScreen {
    public AddCrackedAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Cracked Account", parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Name
        t.add(theme.label("Name: "));
        WTextBox name = t.add(theme.textBox("", (text, c) ->
            // Username can't contain spaces
            c != ' '
        )).minWidth(400).expandX().widget();
        name.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            if (!name.get().isEmpty() && (name.get().length() < 17) && name.get().matches("^[a-zA-Z0-9_]+$")) {
                CrackedAccount account = new CrackedAccount(name.get());
                if (!(Accounts.get().exists(account))) AccountsScreen.addAccount(this, parent, account);
            }
        };

        enterAction = add.action;
    }
}
