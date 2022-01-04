package kult.legacy.klient.mixin;

import kult.legacy.klient.KultKlientLegacy;
import kult.legacy.klient.events.kultklientlegacy.CharTypedEvent;
import kult.legacy.klient.events.kultklientlegacy.KeyEvent;
import kult.legacy.klient.gui.GuiKeyEvents;
import kult.legacy.klient.gui.WidgetScreen;
import kult.legacy.klient.utils.misc.input.Input;
import kult.legacy.klient.utils.misc.input.KeyAction;
import kult.legacy.klient.utils.Utils;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            if (client.currentScreen instanceof WidgetScreen && action == GLFW.GLFW_REPEAT) ((WidgetScreen) client.currentScreen).keyRepeated(key, modifiers);

            if (GuiKeyEvents.canUseKeys) {
                Input.setKeyState(key, action != GLFW.GLFW_RELEASE);
                if (KultKlientLegacy.EVENT_BUS.post(KeyEvent.get(key, modifiers, KeyAction.get(action))).isCancelled()) info.cancel();
            }
        }
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, int i, int j, CallbackInfo info) {
        if (Utils.canUpdate() && !client.isPaused() && (client.currentScreen == null || client.currentScreen instanceof WidgetScreen)) {
            if (KultKlientLegacy.EVENT_BUS.post(CharTypedEvent.get((char) i)).isCancelled()) info.cancel();
        }
    }
}
