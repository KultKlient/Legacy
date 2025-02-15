package kult.klient.systems.modules.render;

import kult.klient.eventbus.EventHandler;
import kult.klient.events.world.ChunkOcclusionEvent;
import kult.klient.settings.*;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.systems.modules.Modules;
import net.minecraft.block.Block;
import net.minecraft.item.Items;

import java.util.List;

public class WallHack extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> opacity = sgGeneral.add(new IntSetting.Builder()
        .name("opacity")
        .description("The opacity for rendered blocks.")
        .defaultValue(1)
        .range(1, 255)
        .sliderMax(255)
        .onChanged(onChanged -> {
            if (isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("What blocks should be targeted for Wall Hack.")
        .defaultValue()
        .onChanged(onChanged -> {
            if (isActive()) mc.worldRenderer.reload();
        })
        .build()
    );

    public final Setting<Boolean> occludeChunks = sgGeneral.add(new BoolSetting.Builder()
        .name("occlude-chunks")
        .description("Whether caves should occlude underground (may look wonky when on).")
        .defaultValue(false)
        .build()
    );

    public WallHack() {
        super(Categories.Render, Items.BARRIER, "wall-hack", "Makes blocks translucent.");
    }

    @Override
    public void onActivate() {
        if (Modules.get().isActive(Xray.class)) {
            error("(highlight)Xray(default) was enabled while enabling (highlight)Wall Hack(default), disabling (highlight)Xray(default)...");
            Modules.get().get(Xray.class).toggle();
        }

        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        mc.worldRenderer.reload();
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        if (!occludeChunks.get()) event.cancel();
    }
}
