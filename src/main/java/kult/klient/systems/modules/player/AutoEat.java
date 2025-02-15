package kult.klient.systems.modules.player;

import baritone.api.BaritoneAPI;
import kult.klient.eventbus.EventHandler;
import kult.klient.eventbus.EventPriority;
import kult.klient.events.entity.player.ItemUseCrosshairTargetEvent;
import kult.klient.events.world.TickEvent;
import kult.klient.settings.*;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.systems.modules.Modules;
import kult.klient.systems.modules.combat.CrystalAura;
import kult.klient.systems.modules.combat.KillAura;
import kult.klient.utils.Utils;
import kult.klient.utils.player.InvUtils;
import kult.klient.systems.modules.combat.BedAura;
import kult.klient.systems.modules.combat.AnchorAura;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class AutoEat extends Module {
    public boolean eating;
    private int slot, prevSlot;

    private final List<Class<? extends Module>> wasAura = new ArrayList<>();
    private boolean wasBaritone;

    private static final Class<? extends Module>[] AURAS = new Class[] { KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class };

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgHunger = settings.createGroup("Hunger");

    // General

    private final Setting<List<Item>> blacklist = sgGeneral.add(new ItemListSetting.Builder()
        .name("blacklist")
        .description("Which items to not eat.")
        .defaultValue(
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.GOLDEN_APPLE,
            Items.CHORUS_FRUIT,
            Items.POISONOUS_POTATO,
            Items.PUFFERFISH,
            Items.CHICKEN,
            Items.ROTTEN_FLESH,
            Items.SPIDER_EYE,
            Items.SUSPICIOUS_STEW
        )
        .filter(Item::isFood)
        .build()
    );

    private final Setting<Boolean> pauseAuras = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-auras")
        .description("Pauses all auras when eating.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseBaritone = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-baritone")
        .description("Pause baritone when eating.")
        .defaultValue(true)
        .build()
    );

    // Hunger

    private final Setting<Integer> hungerThreshold = sgHunger.add(new IntSetting.Builder()
        .name("hunger-threshold")
        .description("The level of hunger you eat at.")
        .defaultValue(16)
        .range(1, 19)
        .sliderRange(1, 19)
        .build()
    );

    public AutoEat() {
        super(Categories.Player, Items.APPLE, "auto-eat", "Automatically eats food.");
    }

    @Override
    public void onDeactivate() {
        if (eating) stopEating();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onTick(TickEvent.Pre event) {
        if (Modules.get().get(AutoGap.class).isEating()) return;

        if (eating) {
            if (shouldEat()) {
                if (!mc.player.getInventory().getStack(slot).isFood()) {
                    int slot = findSlot();

                    if (slot == -1) {
                        stopEating();
                        return;
                    } else changeSlot(slot);
                }

                eat();
            } else stopEating();
        } else {
            if (shouldEat()) {
                slot = findSlot();

                if (slot != -1) startEating();
            }
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (eating) event.target = null;
    }

    private void startEating() {
        prevSlot = mc.player.getInventory().selectedSlot;
        eat();

        wasAura.clear();
        if (pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);

                if (module.isActive()) {
                    wasAura.add(klass);
                    module.toggle();
                }
            }
        }

        wasBaritone = false;
        if (pauseBaritone.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            wasBaritone = true;
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
        }
    }

    private void eat() {
        changeSlot(slot);
        setPressed(true);
        if (!mc.player.isUsingItem()) Utils.rightClick();

        eating = true;
    }

    private void stopEating() {
        changeSlot(prevSlot);
        setPressed(false);

        eating = false;

        if (pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);

                if (wasAura.contains(klass) && !module.isActive()) {
                    module.toggle();
                }
            }
        }

        if (pauseBaritone.get() && wasBaritone) BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
    }

    private void setPressed(boolean pressed) {
        mc.options.keyUse.setPressed(pressed);
    }

    private void changeSlot(int slot) {
        InvUtils.swap(slot, false);
        this.slot = slot;
    }

    private boolean shouldEat() {
        return mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.get();
    }

    private int findSlot() {
        int slot = -1;
        int bestHunger = -1;

        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (!item.isFood()) continue;

            int hunger = item.getFoodComponent().getHunger();
            if (hunger > bestHunger) {
                if (blacklist.get().contains(item)) continue;

                slot = i;
                bestHunger = hunger;
            }
        }

        return slot;
    }
}
