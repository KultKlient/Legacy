package kult.klient.systems.hud.modules;

import kult.klient.settings.BoolSetting;
import kult.klient.settings.Setting;
import kult.klient.settings.SettingGroup;
import kult.klient.systems.hud.DoubleTextHudElement;
import kult.klient.systems.hud.HUD;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateHud extends DoubleTextHudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    public final Setting<Boolean> euDate = sgGeneral.add(new BoolSetting.Builder()
        .name("EU-date")
        .description("Changes the date to Europian format.")
        .defaultValue(false)
        .build()
    );

    public DateHud(HUD hud) {
        super(hud, "date", "Displays current date.", true);
    }

    @Override
    protected String getLeft() {
        return "Date: ";
    }

    @Override
    protected String getRight() {
        if (euDate.get()) return getDate() + " EU";
        else return getDate() + " US";
    }

    private String getDate() {
        if (euDate.get()) return new SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().getTime());
        else return new SimpleDateFormat("MM/dd/yy").format(Calendar.getInstance().getTime());
    }
}
