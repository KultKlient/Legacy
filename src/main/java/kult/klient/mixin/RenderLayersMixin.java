package kult.klient.mixin;

import kult.klient.systems.modules.Modules;
import kult.klient.systems.modules.render.Xray;
import kult.klient.systems.modules.render.WallHack;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class RenderLayersMixin {
    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    private static void onGetBlockLayer(BlockState state, CallbackInfoReturnable<RenderLayer> infor) {
        if(Modules.get() != null) {
            WallHack wallHack = Modules.get().get(WallHack.class);
            Xray xray = Modules.get().get(Xray.class);

            if (wallHack.isActive()) {
                if(wallHack.blocks.get().contains(state.getBlock())) infor.setReturnValue(RenderLayer.getTranslucent());
            } else if(xray.isActive()) infor.setReturnValue(RenderLayer.getTranslucent());
        }
    }
}
