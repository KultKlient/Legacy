package kult.klient.systems.modules.world;

import kult.klient.eventbus.EventHandler;
import kult.klient.events.world.TickEvent;
import kult.klient.settings.*;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.utils.misc.Pool;
import kult.klient.utils.player.FindItemResult;
import kult.klient.utils.player.InvUtils;
import kult.klient.utils.world.BlockIterator;
import kult.klient.utils.world.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SpawnProofer extends Module {
    private final Pool<BlockPos.Mutable> spawnPool = new Pool<>(BlockPos.Mutable::new);
    private final List<BlockPos.Mutable> spawns = new ArrayList<>();

    private int ticksWaited;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Range for block placement and rendering")
        .defaultValue(3)
        .min(0)
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Block to use for spawn proofing")
        .defaultValue(
            Blocks.TORCH,
            Blocks.STONE_BUTTON,
            Blocks.STONE_SLAB
        )
        .filter(this::filterBlocks)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks between placing blocks")
        .defaultValue(0)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the blocks being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which spawn types should be spawn proofed.")
        .defaultValue(Mode.Both)
        .build()
    );

    private final Setting<Boolean> newMobSpawnLightLevel = sgGeneral.add(new BoolSetting.Builder()
        .name("new-mob-spawn-light-level")
        .description("Use the new (1.18+) mob spawn behavior.")
        .defaultValue(true)
        .build()
    );

    public SpawnProofer() {
        super(Categories.World, Items.SPAWNER, "spawn-proofer", "Automatically spawnproofs unlit areas.");
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        // Delay
        if (delay.get() != 0 && ticksWaited < delay.get() - 1) {
            return;
        }

        // Find slot
        FindItemResult block = InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
        if (!block.found()) {
            error("Found none of the chosen blocks in your hotbar, disabling...");
            toggle();
            return;
        }

        // Find spawn locations
        for (BlockPos.Mutable blockPos : spawns) spawnPool.free(blockPos);
        spawns.clear();
        BlockIterator.register(range.get(), range.get(), (blockPos, blockState) -> {
            BlockUtils.MobSpawn spawn = BlockUtils.isValidMobSpawn(blockPos, newMobSpawnLightLevel.get());

            if ((spawn == BlockUtils.MobSpawn.Always && (mode.get() == Mode.Always || mode.get() == Mode.Both)) ||
                    spawn == BlockUtils.MobSpawn.Potential && (mode.get() == Mode.Potential || mode.get() == Mode.Both)) {

                spawns.add(spawnPool.get().set(blockPos));
            }
        });
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        // Delay
        if (delay.get() != 0 && ticksWaited < delay.get() - 1) {
            ticksWaited++;
            return;
        }

        if (spawns.isEmpty()) return;

        // Find slot
        FindItemResult block = InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));

        // Place blocks
        if (delay.get() == 0) for (BlockPos blockPos : spawns) BlockUtils.place(blockPos, block, rotate.get(), -50, false);
        else {

            // Check if light source
            if (isLightSource(Block.getBlockFromItem(mc.player.getInventory().getStack(block.getSlot()).getItem()))) {

                // Find lowest light level
                int lowestLightLevel = 16;
                BlockPos.Mutable selectedBlockPos = spawns.get(0);
                for (BlockPos blockPos : spawns) {
                    int lightLevel = mc.world.getLightLevel(blockPos);
                    if (lightLevel < lowestLightLevel) {
                        lowestLightLevel = lightLevel;
                        selectedBlockPos.set(blockPos);
                    }
                }

                BlockUtils.place(selectedBlockPos, block, rotate.get(), -50, false);

            } else BlockUtils.place(spawns.get(0), block, rotate.get(), -50, false);
        }

        ticksWaited = 0;
    }

    private boolean filterBlocks(Block block) {
        return isNonOpaqueBlock(block) || isLightSource(block);
    }

    private boolean isNonOpaqueBlock(Block block) {
        return block instanceof AbstractButtonBlock ||
            block instanceof SlabBlock ||
            block instanceof AbstractPressurePlateBlock ||
            block instanceof TransparentBlock ||
            block instanceof TripwireBlock;
    }

    private boolean isLightSource(Block block) {
        return block.getDefaultState().getLuminance() > 0;
    }

    public enum Mode {
        Always("Always"),
        Potential("Potential"),
        Both("Both"),
        None("None");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
