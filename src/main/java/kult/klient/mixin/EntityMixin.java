package kult.klient.mixin;

import kult.klient.KultKlient;
import kult.klient.events.entity.LivingEntityMoveEvent;
import kult.klient.events.entity.player.JumpVelocityMultiplierEvent;
import kult.klient.events.entity.player.PlayerMoveEvent;
import kult.klient.systems.modules.Modules;
import kult.klient.utils.render.EntityShaders;
import kult.klient.systems.modules.combat.Hitboxes;
import kult.klient.systems.modules.movement.Moses;
import kult.klient.systems.modules.movement.NoSlow;
import kult.klient.systems.modules.movement.Velocity;
import kult.klient.systems.modules.render.ESP;
import kult.klient.systems.modules.render.NoRender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static kult.klient.KultKlient.mc;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public World world;

    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    protected abstract BlockPos getVelocityAffectingPos();

    @Redirect(method = "updateMovementInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d updateMovementInFluidFluidStateGetVelocity(FluidState state, BlockView world, BlockPos pos) {
        Vec3d vec = state.getVelocity(world, pos);

        Velocity velocity = Modules.get().get(Velocity.class);
        if ((Object) this == mc.player && velocity.isActive() && velocity.liquids.get()) vec = vec.multiply(velocity.getHorizontal(velocity.liquidsHorizontal), velocity.getVertical(velocity.liquidsVertical), velocity.getHorizontal(velocity.liquidsHorizontal));

        return vec;
    }

    @ModifyArgs(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if ((Object) this == mc.player && velocity.isActive() && velocity.entityPush.get()) {
            double multiplier = velocity.entityPushAmount.get();
            args.set(0, (double) args.get(0) * multiplier);
            args.set(2, (double) args.get(2) * multiplier);
        }
    }

    @Inject(method = "getJumpVelocityMultiplier", at = @At("HEAD"), cancellable = true)
    private void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> info) {
        if ((Object) this == mc.player) {
            float f = world.getBlockState(getBlockPos()).getBlock().getJumpVelocityMultiplier();
            float g = world.getBlockState(getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
            float a = f == 1.0D ? g : f;

            JumpVelocityMultiplierEvent event = KultKlient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            info.setReturnValue(a * event.multiplier);
        }
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if ((Object) this == mc.player) KultKlient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
        else if ((Object) this instanceof LivingEntity) KultKlient.EVENT_BUS.post(LivingEntityMoveEvent.get((LivingEntity) (Object) this, movement));
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> info) {
        if (EntityShaders.renderingOutlines) info.setReturnValue(Modules.get().get(ESP.class).getColor((Entity) (Object) this).getPacked());
    }

    @Redirect(method = "getVelocityMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block getVelocityMultiplierGetBlockProxy(BlockState blockState) {
        if ((Object) this != mc.player) return blockState.getBlock();
        if (blockState.getBlock() == Blocks.SOUL_SAND && Modules.get().get(NoSlow.class).soulSand()) return Blocks.STONE;
        return blockState.getBlock();
    }

    @Inject(method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void isInvisibleToCanceller(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(NoRender.class).noInvisibility() || Modules.get().get(ESP.class).shouldDrawOutline((Entity) (Object) this)) info.setReturnValue(false);
    }

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue((Entity) (Object) this);
        if (v != 0) info.setReturnValue((float) v);
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) info.setReturnValue(false);
    }

    @Redirect(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSprinting()Z"))
    private boolean updateSwimmingisSprintingProxy(Entity self) {
        return (!self.isSprinting() || !Modules.get().isActive(Moses.class)) && self.isSprinting();
    }
}
