package kult.legacy.klient.mixin;

import kult.legacy.klient.systems.modules.Modules;
import kult.legacy.klient.systems.modules.misc.BetterTab;
import kult.legacy.klient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static kult.legacy.klient.KultKlientLegacy.mc;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 0), index = 1)
    private int modifyCount(int count) {
        BetterTab module = Modules.get().get(BetterTab.class);

        return module.isActive() ? module.tabSize.get() : count;
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(PlayerListEntry playerListEntry, CallbackInfoReturnable<Text> info) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);

        if (betterTab.isActive()) info.setReturnValue(betterTab.getPlayerName(playerListEntry));
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 1), index = 0)
    private int modifyWidth(int width) {
        BetterTab module = Modules.get().get(BetterTab.class);

        return module.isActive() && module.accurateLatency.get() ? width + 32 : width;
    }

    @Shadow
    protected void renderLatencyIcon(MatrixStack matrices, int width, int x, int y, PlayerListEntry entry) {}

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;renderLatencyIcon(Lnet/minecraft/client/util/math/MatrixStack;IIILnet/minecraft/client/network/PlayerListEntry;)V"))
    protected void renderLatencyIcon(PlayerListHud self, MatrixStack matrices, int width, int x, int y, PlayerListEntry entry) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);

        if (betterTab.isActive() && betterTab.accurateLatency.get()) {
            TextRenderer textRenderer = mc.textRenderer;

            int latency = Utils.clamp(entry.getLatency(), 0, 99999);
            int color = latency < 150 ? 0x00E970 : latency < 300 ? 0xE7D020 : 0xD74238;
            String text = latency + "ms";
            textRenderer.drawWithShadow(matrices, text, (float) x + width - textRenderer.getWidth(text), (float) y, color);
        } else renderLatencyIcon(matrices, width, x, y, entry);
    }
}
