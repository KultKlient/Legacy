package kult.klient.systems.modules.movement;

import com.google.common.collect.Streams;
import kult.klient.eventbus.EventHandler;
import kult.klient.events.world.TickEvent;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.stream.Stream;

public class Parkour extends Module {
    public Parkour() {
        super(Categories.Movement, Items.DIAMOND_BOOTS, "parkour", "Automatically jumps at the edges of blocks.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!mc.player.isOnGround() || mc.options.keyJump.isPressed()) return;

        if (mc.player.isSneaking() || mc.options.keySneak.isPressed()) return;

        Box box = mc.player.getBoundingBox();
        Box adjustedBox = box.offset(0, -0.5, 0).expand(-0.001, 0, -0.001);

        Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

        if (blockCollisions.findAny().isPresent()) return;

        mc.player.jump();
    }
}
