package kult.klient.systems.modules.movement.elytrafly;

import kult.klient.KultKlient;
import kult.klient.events.entity.player.PlayerMoveEvent;
import kult.klient.events.packets.PacketEvent;
import kult.klient.systems.modules.Modules;
import kult.klient.utils.player.FindItemResult;
import kult.klient.utils.player.InvUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class ElytraFlightMode {
    private final ElytraFlightModes type;

    protected final ElytraFly elytraFly;

    protected Vec3d forward, right;

    protected boolean lastJumpPressed;
    protected boolean incrementJumpTimer;
    protected boolean lastForwardPressed;

    protected double velX, velY, velZ;
    protected double ticksLeft;

    protected int jumpTimer;

    public ElytraFlightMode(ElytraFlightModes type) {
        this.elytraFly = Modules.get().get(ElytraFly.class);
        this.type = type;
    }

    public void onTick() {
        if (elytraFly.autoReplenish.get()) {
            FindItemResult fireworks = InvUtils.find(Items.FIREWORK_ROCKET);
            FindItemResult hotbarFireworks = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);

            if (!hotbarFireworks.found() && fireworks.found()) {
                InvUtils.move().from(fireworks.slot()).toHotbar(elytraFly.replenishSlot.get() - 1);
            }
        }

        if (elytraFly.replace.get()) {
            ItemStack chestStack = KultKlient.mc.player.getInventory().getArmorStack(2);

            if (chestStack.getItem() == Items.ELYTRA) {
                if (chestStack.getMaxDamage() - chestStack.getDamage() <= elytraFly.replaceDurability.get()) {
                    FindItemResult elytra = InvUtils.find(stack -> stack.getMaxDamage() - stack.getDamage() > elytraFly.replaceDurability.get() && stack.getItem() == Items.ELYTRA);

                    InvUtils.move().from(elytra.slot()).toArmor(2);
                }
            }
        }
    }

    public void onPacketSend(PacketEvent.Send event) {}

    public void onPlayerMove() {}

    public void onActivate() {
        lastJumpPressed = false;
        jumpTimer = 0;
        ticksLeft = 0;
    }

    public void onDeactivate() {}

    public void autoTakeoff() {
        if (incrementJumpTimer) jumpTimer++;

        boolean jumpPressed = KultKlient.mc.options.keyJump.isPressed();

        if (elytraFly.autoTakeOff.get() && jumpPressed) {
            if (!lastJumpPressed && !KultKlient.mc.player.isFallFlying()) {
                jumpTimer = 0;
                incrementJumpTimer = true;
            }

            if (jumpTimer >= 8) {
                jumpTimer = 0;
                incrementJumpTimer = false;
                KultKlient.mc.player.setJumping(false);
                KultKlient.mc.player.setSprinting(true);
                KultKlient.mc.player.jump();
                KultKlient.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(KultKlient.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        lastJumpPressed = jumpPressed;
    }

    public void handleAutopilot() {
        if (!KultKlient.mc.player.isFallFlying()) return;

        if (elytraFly.autoPilot.get() && KultKlient.mc.player.getY() > elytraFly.autoPilotMinimumHeight.get()) {
            KultKlient.mc.options.keyForward.setPressed(true);
            lastForwardPressed = true;
        }

        if (elytraFly.useFireworks.get()) {
            if (ticksLeft <= 0) {
                ticksLeft = elytraFly.autoPilotFireworkDelay.get() * 20;

                FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
                if (!itemResult.found()) return;

                if (itemResult.isOffhand()) {
                    KultKlient.mc.interactionManager.interactItem(KultKlient.mc.player, KultKlient.mc.world, Hand.OFF_HAND);
                    KultKlient.mc.player.swingHand(Hand.OFF_HAND);
                } else {
                    InvUtils.swap(itemResult.slot(), true);

                    KultKlient.mc.interactionManager.interactItem(KultKlient.mc.player, KultKlient.mc.world, Hand.MAIN_HAND);
                    KultKlient.mc.player.swingHand(Hand.MAIN_HAND);

                    InvUtils.swapBack();
                }
            }

            ticksLeft--;
        }
    }

    public void handleHorizontalSpeed(PlayerMoveEvent event) {
        boolean a = false;
        boolean b = false;

        if (KultKlient.mc.options.keyForward.isPressed()) {
            velX += forward.x * elytraFly.horizontalSpeed.get() * 10;
            velZ += forward.z * elytraFly.horizontalSpeed.get() * 10;
            a = true;
        } else if (KultKlient.mc.options.keyBack.isPressed()) {
            velX -= forward.x * elytraFly.horizontalSpeed.get() * 10;
            velZ -= forward.z * elytraFly.horizontalSpeed.get() * 10;
            a = true;
        }

        if (KultKlient.mc.options.keyRight.isPressed()) {
            velX += right.x * elytraFly.horizontalSpeed.get() * 10;
            velZ += right.z * elytraFly.horizontalSpeed.get() * 10;
            b = true;
        } else if (KultKlient.mc.options.keyLeft.isPressed()) {
            velX -= right.x * elytraFly.horizontalSpeed.get() * 10;
            velZ -= right.z * elytraFly.horizontalSpeed.get() * 10;
            b = true;
        }

        if (a && b) {
            double diagonal = 1 / Math.sqrt(2);
            velX *= diagonal;
            velZ *= diagonal;
        }
    }

    public void handleVerticalSpeed(PlayerMoveEvent event) {
        if (KultKlient.mc.options.keyJump.isPressed()) velY += 0.5 * elytraFly.verticalSpeed.get();
        else if (KultKlient.mc.options.keySneak.isPressed()) velY -= 0.5 * elytraFly.verticalSpeed.get();
    }

    public void handleFallMultiplier() {
        if (velY < 0) velY *= elytraFly.fallMultiplier.get();
        else if (velY > 0) velY = 0;
    }

    public String getHudString() {
        return type.name();
    }
}
