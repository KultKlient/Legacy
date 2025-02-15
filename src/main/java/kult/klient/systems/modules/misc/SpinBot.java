package kult.klient.systems.modules.misc;

import kult.klient.eventbus.EventHandler;
import kult.klient.events.world.TickEvent;
import kult.klient.settings.IntSetting;
import kult.klient.settings.Setting;
import kult.klient.settings.SettingGroup;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.systems.modules.Modules;
import kult.klient.systems.modules.player.EXPThrower;
import kult.klient.utils.player.Rotations;
import net.minecraft.item.BowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Items;

public class SpinBot extends Module {
    private short count;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
        .name("spin-speed")
        .description("The speed at which you spin.")
        .defaultValue(25)
        .min(0)
        .sliderRange(0, 100)
        .build()
    );

    public SpinBot() {
        super(Categories.Misc, Items.GUNPOWDER, "spin-bot", "Makes you spin like Spin bot in CS:GO.");
        count = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Post post) {
        if (Modules.get().isActive(EXPThrower.class) || mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem || mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem || mc.player.getMainHandStack().getItem() instanceof EnderPearlItem || mc.player.getMainHandStack().getItem() instanceof EnderPearlItem || mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem || mc.player.getMainHandStack().getItem() instanceof ExperienceBottleItem || mc.player.getMainHandStack().getItem() instanceof BowItem || mc.player.getMainHandStack().getItem() instanceof BowItem || mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA) return;
        count = (short)(count + speed.get());
        if (count > 180) count = (short)-180;
        Rotations.rotate(count, 0.0);
    }
}
