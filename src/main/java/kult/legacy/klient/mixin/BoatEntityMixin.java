package kult.legacy.klient.mixin;

import kult.legacy.klient.KultKlientLegacy;
import kult.legacy.klient.events.entity.BoatMoveEvent;
import kult.legacy.klient.systems.modules.movement.BoatFly;
import kult.legacy.klient.systems.modules.Modules;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public class BoatEntityMixin {
    @Shadow private boolean pressingLeft;

    @Shadow private boolean pressingRight;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
    private void onTickInvokeMove(CallbackInfo info) {
        KultKlientLegacy.EVENT_BUS.post(BoatMoveEvent.get((BoatEntity) (Object) this));
    }

    @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/BoatEntity;pressingLeft:Z"))
    private boolean onUpdatePaddlesPressingLeft(BoatEntity boat) {
        if (Modules.get().isActive(BoatFly.class)) return false;
        return pressingLeft;
    }

    @Redirect(method = "updatePaddles", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/vehicle/BoatEntity;pressingRight:Z"))
    private boolean onUpdatePaddlesPressingRight(BoatEntity boat) {
        if (Modules.get().isActive(BoatFly.class)) return false;
        return pressingRight;
    }
}
