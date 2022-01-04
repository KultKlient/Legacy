package kult.legacy.klient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kult.legacy.klient.gui.GuiThemes;
import kult.legacy.klient.settings.Setting;
import kult.legacy.klient.systems.commands.Command;
import kult.legacy.klient.systems.commands.arguments.ModuleArgumentType;
import kult.legacy.klient.systems.modules.Module;
import kult.legacy.klient.systems.modules.Modules;
import kult.legacy.klient.systems.modules.render.hud.HUD;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ResetCommand extends Command {
    public ResetCommand() {
        super("reset", "Resets specified settings.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("settings")
                .then(argument("module", ModuleArgumentType.module()).executes(context -> {
                    Module module = context.getArgument("module", Module.class);
                    module.settings.forEach(group -> group.forEach(Setting::reset));
                    module.info("Reset all settings.");
                    return SINGLE_SUCCESS;
                }))
                .then(literal("all").executes(context -> {
                    Modules.get().getAll().forEach(module -> module.settings.forEach(group -> group.forEach(Setting::reset)));
                    info("Reset all module's settings");
                    return SINGLE_SUCCESS;
                }))
        ).then(literal("gui").executes(context -> {
            GuiThemes.get().clearWindowConfigs();
            info("The Click GUI positioning has been reset.");
            return SINGLE_SUCCESS;
        }).then(literal("scale").executes(context -> {
            GuiThemes.get().resetScale();
            info("The GUI scale has been reset.");
            return SINGLE_SUCCESS;
        }))).then(literal("bind")
                .then(argument("module", ModuleArgumentType.module()).executes(context -> {
                    Module module = context.getArgument("module", Module.class);

                    module.keybind.set(true, -1);
                    module.info("Reset bind.");

                    return SINGLE_SUCCESS;
                }))
                .then(literal("all").executes(context -> {
                    Modules.get().getAll().forEach(module -> module.keybind.set(true, -1));
                    info("Reset all module's binds");
                    return SINGLE_SUCCESS;
                }))
        ).then(literal("hud").executes(context -> {
            Modules.get().get(HUD.class).reset.run();
            Modules.get().get(HUD.class).info("Reset HUD elements.");
            return SINGLE_SUCCESS;
        }));
    }
}
