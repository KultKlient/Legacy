package kult.klient.systems.modules.world;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.Settings;
import baritone.api.pathing.goals.GoalBlock;
import kult.klient.eventbus.EventHandler;
import kult.klient.events.game.GameLeftEvent;
import kult.klient.events.world.TickEvent;
import kult.klient.settings.*;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.utils.Utils;
import kult.klient.utils.player.FindItemResult;
import kult.klient.utils.player.InvUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Predicate;

public class InfinityMiner extends Module {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    private final Settings baritoneSettings = BaritoneAPI.getSettings();

    private final BlockPos.Mutable homePos = new BlockPos.Mutable();

    private boolean repairing;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgExtras = settings.createGroup("Extras");

    // General

    public final Setting<List<Block>> targetBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("target-blocks")
        .description("The target blocks to mine.")
        .defaultValue(
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE
        )
        .filter(this::filter)
        .build()
    );

    public final Setting<List<Block>> repairBlocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("repair-blocks")
        .description("The repair blocks to mine.")
        .defaultValue(
            Blocks.COAL_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.NETHER_QUARTZ_ORE
        )
        .filter(this::filter)
        .build()
    );

    public final Setting<Double> durabilityThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("durability-threshold")
        .description("The durability percentage at which to start repairing the tool.")
        .defaultValue(10)
        .range(1, 99)
        .sliderRange(1, 99)
        .build()
    );

    private final Setting<WhenFull> whenFull = sgGeneral.add(new EnumSetting.Builder<WhenFull>()
        .name("when-full")
        .description("The action to take when your inventory is full.")
        .defaultValue(WhenFull.Disconnect)
        .build()
    );

    // Extras

    public final Setting<Boolean> autoWalkHome = sgExtras.add(new BoolSetting.Builder()
        .name("walk-home")
        .description("Will walk 'home' when your inventory is full.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> autoLogOut = sgExtras.add(new BoolSetting.Builder()
        .name("log-out")
        .description("Logs out when your inventory is full. Will walk home FIRST if walk home is enabled.")
        .defaultValue(false)
        .build()
    );

    public InfinityMiner() {
        super(Categories.World, Items.DIAMOND_PICKAXE, "infinity-miner", "Allows you to essentially mine forever by mining repair blocks when the durability gets low. Needs a mending pickaxe.");
    }

    @Override
    public void onActivate() {
        homePos.set(mc.player.getBlockPos());
    }

    @Override
    public void onDeactivate() {
        baritone.getPathingBehavior().cancelEverything();
        baritoneSettings.mineScanDroppedItems.value = true;
    }

    @EventHandler
    private void onGameDisconnect(GameLeftEvent event) {
        baritone.getPathingBehavior().cancelEverything();
        toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.getInventory().getEmptySlot() == -1) {
            switch (whenFull.get()) {
                case Disconnect -> mc.player.networkHandler.sendPacket(new DisconnectS2CPacket(new LiteralText("[Infinity Miner] Inventory is full.")));
                case Home -> {
                    if (!baritone.getPathingBehavior().isPathing() || !baritone.getPathingBehavior().getGoal().isInGoal(homePos)) {
                        info("Walking home.");
                        baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(homePos));
                    } else if (mc.player.getBlockPos().equals(homePos)) mc.player.networkHandler.sendPacket(new DisconnectS2CPacket(new LiteralText("[Infinity Miner] Inventory is full.")));
                }
            }

            return;
        }

        if (!findBestPick()) {
            error("Could not find a usable mending pickaxe.");
            toggle();
            return;
        }

        if (repairing) {
            if (!needsRepair()) {
                warning("Finished repairing, going back to mining.");
                repairing = false;
                baritone.getPathingBehavior().cancelEverything();
                baritone.getMineProcess().mine(getTargetBlocks());
                return;
            }

            if (baritoneSettings.mineScanDroppedItems.value) baritoneSettings.mineScanDroppedItems.value = false;
            baritone.getMineProcess().mine(getRepairBlocks());
        } else {
            if (needsRepair()) {
                warning("Pickaxe needs repair, beginning repair process");
                repairing = true;
                baritone.getPathingBehavior().cancelEverything();
                baritone.getMineProcess().mine(getRepairBlocks());
                return;
            }

            if (!baritoneSettings.mineScanDroppedItems.value) baritoneSettings.mineScanDroppedItems.value = true;
            baritone.getMineProcess().mine(getTargetBlocks());
        }
    }

    private boolean findBestPick() {
        Predicate<ItemStack> pickaxePredicate = (stack -> stack.getItem() instanceof PickaxeItem && Utils.hasEnchantments(stack, Enchantments.MENDING) && !Utils.hasEnchantments(stack, Enchantments.SILK_TOUCH));
        FindItemResult bestPick = InvUtils.findInHotbar(pickaxePredicate);

        if (bestPick.isOffhand()) InvUtils.quickMove().fromOffhand().toHotbar(mc.player.getInventory().selectedSlot);
        else if (bestPick.isHotbar()) InvUtils.swap(bestPick.slot(), false);

        return InvUtils.findInHotbar(pickaxePredicate).isMainHand();
    }

    private Block[] getTargetBlocks() {
        Block[] array = new Block[targetBlocks.get().size()];
        return targetBlocks.get().toArray(array);
    }

    private Block[] getRepairBlocks() {
        Block[] array = new Block[repairBlocks.get().size()];
        return repairBlocks.get().toArray(array);
    }

    private boolean needsRepair() {
        ItemStack itemStack = mc.player.getMainHandStack();
        return ((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage() <= durabilityThreshold.get();
    }

    private boolean filter(Block block) {
        return block != Blocks.AIR && block.getDefaultState().getHardness(mc.world, null) != -1 && !(block instanceof FluidBlock);
    }

    public enum Mode {
        Target("Target"),
        Repair("Repair");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum WhenFull {
        Home("Home"),
        Disconnect("Disconnect");

        private final String title;

        WhenFull(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
