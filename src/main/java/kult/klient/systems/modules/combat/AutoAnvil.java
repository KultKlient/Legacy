package kult.klient.systems.modules.combat;

import kult.klient.eventbus.EventHandler;
import kult.klient.events.game.OpenScreenEvent;
import kult.klient.events.world.TickEvent;
import kult.klient.settings.*;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.utils.entity.EntityUtils;
import kult.klient.utils.entity.SortPriority;
import kult.klient.utils.entity.TargetUtils;
import kult.klient.utils.player.FindItemResult;
import kult.klient.utils.player.InvUtils;
import kult.klient.utils.world.BlockUtils;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class AutoAnvil extends Module {
    private PlayerEntity target;

    private int timer;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The radius in which players get targeted.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0, 5)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.Lowest_Health)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("The height to place anvils at.")
        .defaultValue(2)
        .range(0, 5)
        .sliderRange(0, 5)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay in between anvil placements.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );

    private final Setting<Boolean> placeButton = sgGeneral.add(new BoolSetting.Builder()
        .name("place-at-feet")
        .description("Automatically places a button or pressure plate at the targets feet to break the anvils.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> multiPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("multi-place")
        .description("Places multiple anvils at once..")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleOnBreak = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-break")
        .description("Toggles when the target's helmet slot is empty.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically rotates towards the position anvils/pressure plates/buttons are placed.")
        .defaultValue(true)
        .build()
    );

    public AutoAnvil() {
        super(Categories.Combat, Items.ANVIL, "auto-anvil", "Automatically places anvils above players to destroy helmets or get out of the hole on servers with patched burrow.");
    }

    @Override
    public void onActivate() {
        timer = 0;
        target = null;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AnvilScreen) event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (toggleOnBreak.get() && target != null && target.getInventory().getArmorStack(3).isEmpty()) {
            error("Target head slot is empty, disabling...");
            toggle();
            return;
        }

        if (TargetUtils.isBadTarget(target, range.get())) target = TargetUtils.getPlayerTarget(range.get(), priority.get());
        if (TargetUtils.isBadTarget(target, range.get())) return;

        if (placeButton.get()) {
            FindItemResult floorBlock = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractButtonBlock);
            BlockUtils.place(target.getBlockPos(), floorBlock, rotate.get(), 0, false);
        }

        if (timer >= delay.get()) {
            timer = 0;

            FindItemResult anvil = InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AnvilBlock);
            if (!anvil.found()) return;

            for (int i = height.get(); i > 1; i--) {
                BlockPos blockPos = target.getBlockPos().up().add(0, i, 0);

                for (int j = 0; j < i; j++) {
                    if (!mc.world.getBlockState(target.getBlockPos().up(j + 1)).getMaterial().isReplaceable()) break;
                }

                if (BlockUtils.place(blockPos, anvil, rotate.get(), 0) && !multiPlace.get()) break;
            }
        } else timer++;
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }
}
