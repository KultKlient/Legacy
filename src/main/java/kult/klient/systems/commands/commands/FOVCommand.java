package kult.klient.systems.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kult.klient.systems.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FOVCommand extends Command {
    public FOVCommand() {
        super("fov", "Changes your FOV.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            info("Currently using (highlight)%s(default).", (int) mc.options.fov);
            return SINGLE_SUCCESS;
        });

        builder.then(literal("set")
            .then(argument("new-fov", IntegerArgumentType.integer(0))
                .executes(context -> {
                    int newFov = context.getArgument("new-fov", Integer.class);
                    mc.options.fov = newFov;
                    info("Set to (highlight)%s(default).", newFov);
                    return SINGLE_SUCCESS;
                })
            )
        );
    }
}
