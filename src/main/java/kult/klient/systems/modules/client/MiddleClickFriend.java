package kult.klient.systems.modules.client;

import kult.klient.eventbus.EventHandler;
import kult.klient.events.kultklient.MouseButtonEvent;
import kult.klient.settings.BoolSetting;
import kult.klient.settings.Setting;
import kult.klient.settings.SettingGroup;
import kult.klient.settings.StringSetting;
import kult.klient.systems.config.Config;
import kult.klient.systems.enemies.Enemies;
import kult.klient.systems.friends.Friend;
import kult.klient.systems.friends.Friends;
import kult.klient.systems.modules.Categories;
import kult.klient.systems.modules.Module;
import kult.klient.utils.misc.ChatUtils;
import kult.klient.utils.misc.Placeholders;
import kult.klient.utils.misc.input.KeyAction;
import kult.klient.utils.render.ToastSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

public class MiddleClickFriend extends Module {
    private final SettingGroup sgAdd = settings.createGroup("Add");
    private final SettingGroup sgRemove = settings.createGroup("Remove");

    // Add

    private final Setting<Boolean> friendAddMessage = sgAdd.add(new BoolSetting.Builder()
        .name("friend-add-message")
        .description("Sends a message to the player when you add them as a friend.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> friendAddMessageText = sgAdd.add(new StringSetting.Builder()
        .name("friend-add-message-text")
        .description("The message sent to the player after friending him.")
        .defaultValue("I just friended you on KultKlient!")
        .visible(friendAddMessage::get)
        .build()
    );

    // Remove

    private final Setting<Boolean> friendRemoveMessage = sgRemove.add(new BoolSetting.Builder()
        .name("friend-remove-message")
        .description("Sends a message to the player when you add them as a friend.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> friendRemoveMessageText = sgRemove.add(new StringSetting.Builder()
        .name("friend-remove-message-text")
        .description("The message sent to the player after unfriending him.")
        .defaultValue("I just unfriended you on KultKlient!")
        .visible(friendRemoveMessage::get)
        .build()
    );

    public MiddleClickFriend() {
        super(Categories.Client, Items.STONE_BUTTON, "middle-click-friend", "Adds or removes a player as a friend using middle click.");
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && event.button == GLFW_MOUSE_BUTTON_MIDDLE && mc.currentScreen == null && mc.targetedEntity != null && mc.targetedEntity instanceof PlayerEntity) {
            if (mc.targetedEntity.getUuidAsString().equals(mc.getSession().getUuid())) return;
            if (!Friends.get().isFriend((PlayerEntity) mc.targetedEntity)) {
                if (Enemies.get().isEnemy((PlayerEntity) mc.targetedEntity)) {
                    if (Config.get().chatFeedback.get()) ChatUtils.error("Friends", "Could not add to friends because this person is on your Enemy list.");
                    if (Config.get().toastFeedback.get()) mc.getToastManager().add(new ToastSystem(Items.EMERALD_BLOCK, Friends.get().color.getPacked(), "Friends " + Formatting.GRAY + "[" + Formatting.WHITE + mc.targetedEntity.getEntityName() + Formatting.GRAY + "]", null, Formatting.RED + "Person is enemy.", Config.get().toastDuration.get()));
                } else {
                    Friends.get().add(new Friend((PlayerEntity) mc.targetedEntity));
                    if (friendAddMessage.get()) mc.player.sendChatMessage("/msg " + mc.targetedEntity.getEntityName() + " " + Placeholders.apply(friendAddMessageText.toString()));
                    if (Config.get().chatFeedback.get()) ChatUtils.info("Friends", "Added (highlight)%s (default)to friends.", mc.targetedEntity.getEntityName());
                    if (Config.get().toastFeedback.get()) mc.getToastManager().add(new ToastSystem(Items.EMERALD_BLOCK, Friends.get().color.getPacked(), "Friends " + Formatting.GRAY + "[" + Formatting.WHITE + mc.targetedEntity.getEntityName() + Formatting.GRAY + "]", null, Formatting.GRAY + "Added to friends.", Config.get().toastDuration.get()));
                }
            } else {
                Friends.get().remove(Friends.get().get((PlayerEntity) mc.targetedEntity));
                if (friendRemoveMessage.get()) mc.player.sendChatMessage("/msg " + mc.targetedEntity.getEntityName() + " " + Placeholders.apply(friendRemoveMessageText.toString()));
                if (Config.get().chatFeedback.get()) ChatUtils.info("Friends", "Removed (highlight)%s (default)from friends.", mc.targetedEntity.getEntityName());
                if (Config.get().toastFeedback.get()) mc.getToastManager().add(new ToastSystem(Items.EMERALD_BLOCK, Friends.get().color.getPacked(), "Friends " + Formatting.GRAY + "[" + Formatting.WHITE + mc.targetedEntity.getEntityName() + Formatting.GRAY + "]", null, Formatting.GRAY + "Removed from friends.", Config.get().toastDuration.get()));
            }
        }
    }
}
