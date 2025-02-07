package kult.klient.gui.screens;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kult.klient.gui.widgets.containers.WTable;
import kult.klient.gui.widgets.pressable.WButton;
import kult.klient.utils.misc.ChatUtils;
import kult.klient.utils.network.HTTP;
import kult.klient.utils.network.KultKlientExecutor;
import kult.klient.utils.player.GiveUtils;
import kult.klient.gui.GuiTheme;
import kult.klient.gui.WindowScreen;
import kult.klient.settings.EnumSetting;
import kult.klient.settings.Setting;
import kult.klient.settings.SettingGroup;
import kult.klient.settings.Settings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static kult.klient.KultKlient.mc;

public class HeadScreen extends WindowScreen {
    private static final Type gsonType = new TypeToken<List<Map<String, String>>>() {}.getType();

    private final Settings settings = new Settings();
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private static Categories category = Categories.Decoration;
    private final Setting<Categories> categorySetting = sgGeneral.add(new EnumSetting.Builder<Categories>()
        .name("category")
        .description("Category.")
        .defaultValue(category)
        .onChanged((v) -> loadHeads())
        .build()
    );

    public HeadScreen(GuiTheme theme) {
        super(theme, "Heads");
        loadHeads();
    }

    @Override
    public void initWidgets() {}

    private void set() {
        clear();
        add(theme.settings(settings)).expandX();
        add(theme.horizontalSeparator()).expandX();
    }

    private String getCategory() {
        category = categorySetting.get();
        return category.toString().replace("_", "-");
    }

    private void loadHeads() {
        KultKlientExecutor.execute(() -> {
            List<Map<String, String>> res = HTTP.get("https://minecraft-heads.com/scripts/api.php?cat=" + getCategory()).sendJson(gsonType);
            List<ItemStack> heads = new ArrayList<>();
            res.forEach(a -> {
                try {
                    heads.add(createHeadStack(a.get("uuid"), a.get("value"), a.get("name")));
                } catch (Exception ignored) {}
            });

            WTable t = theme.table();
            for (ItemStack head : heads) {
                t.add(theme.item(head));
                t.add(theme.label(head.getName().asString()));
                WButton give = t.add(theme.button("Give")).widget();
                give.action = () -> {
                    try {
                        GiveUtils.giveItem(head);
                    } catch (CommandSyntaxException e) {
                        ChatUtils.error("Heads", e.getMessage());
                    }
                };
                WButton equip = t.add(theme.button("Equip")).widget();
                equip.tooltip = "Equip client-side.";
                equip.action = () -> mc.player.getInventory().armor.set(3, head);
                t.row();
            }
            set();
            add(t).expandX().minWidth(400).widget();
        });
    }

    private ItemStack createHeadStack(String uuid, String value, String name) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound tag = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        skullOwner.putUuid("Id", UUID.fromString(uuid));
        NbtCompound properties = new NbtCompound();
        NbtList textures = new NbtList();
        NbtCompound Value = new NbtCompound();
        Value.putString("Value", value);
        textures.add(Value);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        tag.put("SkullOwner", skullOwner);
        head.setNbt(tag);
        head.setCustomName(new LiteralText(name));
        return head;
    }

    public enum Categories {
        Alphabet("Alphabet"),
        Animals("Animals"),
        Blocks("Blocks"),
        Decoration("Decoration"),
        Food_Drinks("Food and Drinks"),
        Humanoid("Humanoid"),
        Miscellaneous("Miscellaneous"),
        Monsters("Monsters"),
        Plants("Plants");

        private final String title;

        Categories(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
