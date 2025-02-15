package kult.klient.systems.modules.render.search;

import kult.klient.gui.GuiTheme;
import kult.klient.gui.WidgetScreen;
import kult.klient.gui.utils.IScreenFactory;
import kult.klient.renderer.ShapeMode;
import kult.klient.settings.BlockDataSetting;
import kult.klient.settings.IBlockData;
import kult.klient.utils.misc.IChangeable;
import kult.klient.utils.misc.ICopyable;
import kult.klient.utils.misc.ISerializable;
import kult.klient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;

public class SBlockData implements ICopyable<SBlockData>, ISerializable<SBlockData>, IChangeable, IBlockData<SBlockData>, IScreenFactory {
    public ShapeMode shapeMode;
    public SettingColor lineColor;
    public SettingColor sideColor;

    public boolean tracer;
    public SettingColor tracerColor;

    private boolean changed;

    public SBlockData(ShapeMode shapeMode, SettingColor lineColor, SettingColor sideColor, boolean tracer, SettingColor tracerColor) {
        this.shapeMode = shapeMode;
        this.lineColor = lineColor;
        this.sideColor = sideColor;

        this.tracer = tracer;
        this.tracerColor = tracerColor;
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme, Block block, BlockDataSetting<SBlockData> setting) {
        return new SBlockDataScreen(theme, this, block, setting);
    }

    @Override
    public WidgetScreen createScreen(GuiTheme theme) {
        return new SBlockDataScreen(theme, this, null, null);
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    public void changed() {
        changed = true;
    }

    public void tickRainbow() {
        lineColor.update();
        sideColor.update();
        tracerColor.update();
    }

    @Override
    public SBlockData set(SBlockData value) {
        shapeMode = value.shapeMode;
        lineColor.set(value.lineColor);
        sideColor.set(value.sideColor);

        tracer = value.tracer;
        tracerColor.set(value.tracerColor);

        changed = value.changed;

        return this;
    }

    @Override
    public SBlockData copy() {
        return new SBlockData(shapeMode, new SettingColor(lineColor), new SettingColor(sideColor), tracer, new SettingColor(tracerColor));
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("shapeMode", shapeMode.name());
        tag.put("lineColor", lineColor.toTag());
        tag.put("sideColor", sideColor.toTag());

        tag.putBoolean("tracer", tracer);
        tag.put("tracerColor", tracerColor.toTag());

        tag.putBoolean("changed", changed);

        return tag;
    }

    @Override
    public SBlockData fromTag(NbtCompound tag) {
        shapeMode = ShapeMode.valueOf(tag.getString("shapeMode"));
        lineColor.fromTag(tag.getCompound("lineColor"));
        sideColor.fromTag(tag.getCompound("sideColor"));

        tracer = tag.getBoolean("tracer");
        tracerColor.fromTag(tag.getCompound("tracerColor"));

        changed = tag.getBoolean("changed");

        return this;
    }
}
