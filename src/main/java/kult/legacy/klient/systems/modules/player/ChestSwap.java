package kult.legacy.klient.systems.modules.player;

import kult.legacy.klient.settings.BoolSetting;
import kult.legacy.klient.settings.EnumSetting;
import kult.legacy.klient.settings.Setting;
import kult.legacy.klient.settings.SettingGroup;
import kult.legacy.klient.systems.config.Config;
import kult.legacy.klient.systems.modules.Categories;
import kult.legacy.klient.systems.modules.Module;
import kult.legacy.klient.utils.player.InvUtils;
import kult.legacy.klient.utils.render.ToastSystem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

public class ChestSwap extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Chestplate> chestplate = sgGeneral.add(new EnumSetting.Builder<Chestplate>()
        .name("chestplate")
        .description("Which type of chestplate to swap to.")
        .defaultValue(Chestplate.Prefer_Netherite)
        .build()
    );

    private final Setting<Boolean> stayOn = sgGeneral.add(new BoolSetting.Builder()
        .name("stay-on")
        .description("Stays on and activates when you turn it off.")
        .defaultValue(false)
        .build()
    );

    public ChestSwap() {
        super(Categories.Player, Items.DIAMOND_CHESTPLATE, "chest-swap", "Automatically swaps between a chestplate and an elytra");
    }

    @Override
    public void onActivate() {
        swap();

        if (!stayOn.get()) toggle();
    }

    @Override
    public void onDeactivate() {
        if (stayOn.get()) swap();
    }

    public void swap() {
        Item currentItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (currentItem == Items.ELYTRA) equipChestplate();
        else if (currentItem instanceof ArmorItem && ((ArmorItem) currentItem).getSlotType() == EquipmentSlot.CHEST) equipElytra();
        else if (!equipChestplate()) equipElytra();
    }

    private boolean equipChestplate() {
        int bestSlot = -1;
        boolean breakLoop = false;

        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            switch (chestplate.get()) {
                case Diamond:
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                    break;
                case Netherite:
                    if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                    break;
                case Prefer_Diamond:
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    } else if (item == Items.NETHERITE_CHESTPLATE) bestSlot = i;
                    break;
                case Prefer_Netherite:
                    if (item == Items.DIAMOND_CHESTPLATE) bestSlot = i;
                    else if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                    break;
            }

            if (breakLoop) break;
        }

        if (bestSlot != -1) equip(bestSlot);
        return bestSlot != -1;
    }

    private void equipElytra() {
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            if (item == Items.ELYTRA) {
                equip(i);
                break;
            }
        }
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);
    }

    @Override
    public void sendToggledMsg(String name, Module module) {
        if (stayOn.get()) super.sendToggledMsg(name, module);
        else if (Config.get().chatFeedback) info("Triggered (highlight)%s(default).", title);
    }

    @Override
    public void sendToggledToast(String name, Module module) {
        if (stayOn.get()) super.sendToggledToast(name, module);
        else if (Config.get().toastFeedback) mc.getToastManager().add(new ToastSystem(module.icon, module.category.color, title, null, Formatting.GRAY + "Has been triggered.", Config.get().toastDuration));
    }

    public enum Chestplate {
        Diamond,
        Netherite,
        Prefer_Diamond,
        Prefer_Netherite;

        @Override
        public String toString() {
            return super.toString().replace("_", " ");
        }
    }
}
