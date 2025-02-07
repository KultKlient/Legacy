package kult.klient.mixin;

import kult.klient.systems.modules.Modules;
import kult.klient.systems.modules.render.NoRender;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRendererMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/SignBlockEntity;updateSign(ZLjava/util/function/Function;)[Lnet/minecraft/text/OrderedText;"))
    private OrderedText[] updateSignProxy(SignBlockEntity sign, boolean filterText, Function<Text, OrderedText> textOrderingFunction) {
        if (Modules.get().get(NoRender.class).noSignText()) return null;
        return sign.updateSign(filterText, textOrderingFunction);
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 4))
    private int loopTextLengthProxy(int i) {
        if (Modules.get().get(NoRender.class).noSignText()) return 0;
        return i;
    }
}
