package kult.klient.systems.modules.combat;

import baritone.api.BaritoneAPI;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import kult.klient.eventbus.EventHandler;
import kult.klient.events.render.Render3DEvent;
import kult.klient.settings.*;
import kult.klient.systems.friends.Friends;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.utils.entity.EntityUtils;
import kult.klient.utils.entity.SortPriority;
import kult.klient.utils.entity.TargetUtils;
import kult.klient.utils.player.InvUtils;
import kult.klient.utils.player.PlayerUtils;
import kult.klient.utils.player.Rotations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class BowAimbot extends Module {
    private boolean wasPathing;

    private Entity target;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range the entity can be to aim at it.")
        .defaultValue(20)
        .range(0, 100)
        .sliderRange(0, 100)
        .build()
    );

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Determines which entities to attack.")
        .onlyAttackable()
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("What type of entities to target.")
        .defaultValue(SortPriority.Lowest_Health)
        .build()
    );

    private final Setting<Boolean> babies = sgGeneral.add(new BoolSetting.Builder()
        .name("babies")
        .description("Whether or not to attack baby variants of the entity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> nametagged = sgGeneral.add(new BoolSetting.Builder()
        .name("nametagged")
        .description("Whether or not to attack mobs with a name tag.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> pauseOnCombat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-combat")
        .description("Freezes Baritone temporarily until you released the bow.")
        .defaultValue(false)
        .build()
    );

    public BowAimbot() {
        super(Categories.Combat, Items.BOW, "bow-aimbot", "Automatically aims your bow for you.");
    }

    @Override
    public void onDeactivate() {
        target = null;
        wasPathing = false;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!PlayerUtils.isAlive() || !itemInHand()) return;
        if (!InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found()) return;

        target = TargetUtils.get(entity -> {
            if (entity == mc.player || entity == mc.cameraEntity) return false;
            if ((entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) || !entity.isAlive()) return false;
            if (entity.distanceTo(mc.player) > range.get()) return false;
            if (!entities.get().getBoolean(entity.getType())) return false;
            if (!nametagged.get() && entity.hasCustomName()) return false;
            if (!PlayerUtils.canSeeEntity(entity)) return false;
            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity) entity).isCreative()) return false;
                if (!Friends.get().shouldAttack((PlayerEntity) entity)) return false;
            }
            return !(entity instanceof AnimalEntity) || babies.get() || !((AnimalEntity) entity).isBaby();
        }, priority.get());

        if (target == null) {
            if (wasPathing) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
                wasPathing = false;
            }

            return;
        }

        if (mc.options.keyUse.isPressed() && itemInHand()) {
            if (pauseOnCombat.get() && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing() && !wasPathing) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
                wasPathing = true;
            }
            aim(event.tickDelta);
        }
    }

    private boolean itemInHand() {
        return mc.player.getMainHandStack().getItem() instanceof BowItem || mc.player.getMainHandStack().getItem() instanceof CrossbowItem;
    }

    private void aim(double tickDelta) {
        // Velocity based on bow charge.
        float velocity = (mc.player.getItemUseTime() - mc.player.getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        // Positions
        double posX = target.getPos().getX() + (target.getPos().getX() - target.prevX) * tickDelta;
        double posY = target.getPos().getY() + (target.getPos().getY() - target.prevY) * tickDelta;
        double posZ = target.getPos().getZ() + (target.getPos().getZ() - target.prevZ) * tickDelta;

        // Adjusting for hitbox heights
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mc.player.getX();
        double relativeY = posY - mc.player.getY();
        double relativeZ = posZ - mc.player.getZ();

        // Calculate the pitch
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));

        // Set player rotation
        if (Float.isNaN(pitch)) {
            Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target));
        } else {
            Rotations.rotate(Rotations.getYaw(new Vec3d(posX, posY, posZ)), pitch);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }
}
