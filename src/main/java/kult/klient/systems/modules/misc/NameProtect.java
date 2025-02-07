package kult.klient.systems.modules.misc;

import kult.klient.settings.Setting;
import kult.klient.settings.SettingGroup;
import kult.klient.settings.StringSetting;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import net.minecraft.item.Items;

public class NameProtect extends Module {
    private String username = "NULL";

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("Name to be replaced with.")
        .defaultValue("popbob")
        .build()
    );

    public NameProtect() {
        super(Categories.Misc, Items.NAME_TAG, "name-protect", "Hides your username client-side.");
    }

    @Override
    public void onActivate() {
        username = mc.getSession().getUsername();
    }

    public String replaceName(String string) {
        if (string != null && isActive()) return string.replace(username, name.get());

        return string;
    }

    public String getName(String original) {
        if (name.get().length() > 0 && isActive()) return name.get();

        return original;
    }
}
