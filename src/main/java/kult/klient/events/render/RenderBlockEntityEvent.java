package kult.klient.events.render;

import kult.klient.events.Cancellable;
import net.minecraft.block.entity.BlockEntity;

public class RenderBlockEntityEvent extends Cancellable {
    private static final RenderBlockEntityEvent INSTANCE = new RenderBlockEntityEvent();

    public BlockEntity blockEntity;

    public static RenderBlockEntityEvent get(BlockEntity blockEntity) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockEntity = blockEntity;
        return INSTANCE;
    }
}
