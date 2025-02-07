package kult.klient.systems.modules.chat;

import kult.klient.events.world.TickEvent;
import kult.klient.settings.*;
import kult.klient.systems.modules.Module;
import kult.klient.systems.modules.Categories;
import kult.klient.utils.Utils;
import kult.klient.eventbus.EventHandler;
import net.minecraft.item.Items;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

public class Spam extends Module {
    private int messageI, timer;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAntiSpamBypass = settings.createGroup("Anti Spam Bypass");

    // General

    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("messages")
        .description("Messages to use for spam. Use %player% for a name of a random player.")
        .defaultValue(
            "KultKlient on top!",
            "Matejko06 on top!"
        )
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between specified messages in ticks.")
        .defaultValue(20)
        .min(0)
        .sliderRange(0, 200)
        .build()
    );

    private final Setting<Boolean> random = sgGeneral.add(new BoolSetting.Builder()
        .name("randomise")
        .description("Selects a random message from your spam message list.")
        .defaultValue(false)
        .build()
    );

    // Anti Spam Bypass

    private final Setting<Boolean> randomText = sgAntiSpamBypass.add(new BoolSetting.Builder()
        .name("random-text")
        .description("Adds random text at the bottom of the text.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> randomTextLength = sgAntiSpamBypass.add(new IntSetting.Builder()
        .name("length")
        .description("Text length of anti spam bypass.")
        .defaultValue(16)
        .sliderRange(1, 256)
        .visible(randomText::get)
        .build()
    );

    public Spam() {
        super(Categories.Chat, Items.PAPER, "spam", "Spams specified messages in chat.");
    }

    @Override
    public void onActivate() {
        timer = delay.get();
        messageI = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (messages.get().isEmpty()) return;

        if (timer <= 0) {
            int i;
            if (random.get()) i = Utils.random(0, messages.get().size());
            else {
                if (messageI >= messages.get().size()) messageI = 0;
                i = messageI++;
            }

            String text = messages.get().get(i);
            if (randomText.get()) text += " " + RandomStringUtils.randomAlphabetic(randomTextLength.get()).toLowerCase();

            mc.player.sendChatMessage(text.replace("%player%", Utils.getRandomPlayer()));

            timer = delay.get();
        } else timer--;
    }
}
