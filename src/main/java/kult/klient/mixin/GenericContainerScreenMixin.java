package kult.klient.mixin;

import kult.klient.systems.modules.Modules;
import kult.klient.utils.render.ContainerButtonWidget;
import kult.klient.systems.modules.misc.InventoryTweaks;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    public GenericContainerScreenMixin(GenericContainerScreenHandler container, PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    @Override
    protected void init() {
        super.init();

        InventoryTweaks invTweaks = Modules.get().get(InventoryTweaks.class);

        if (invTweaks.isActive() && invTweaks.showButtons()) {
            addDrawableChild(new ContainerButtonWidget(
                x + backgroundWidth - 88,
                y + 3,
                40,
                12,
                new LiteralText("Steal"),
                button -> invTweaks.steal(handler))
            );

            addDrawableChild(new ContainerButtonWidget(
                x + backgroundWidth - 46,
                y + 3,
                40,
                12,
                new LiteralText("Dump"),
                button -> invTweaks.dump(handler))
            );
        }

        if (invTweaks.autoSteal()) invTweaks.steal(handler);
        if (invTweaks.autoDump()) invTweaks.dump(handler);
    }
}
