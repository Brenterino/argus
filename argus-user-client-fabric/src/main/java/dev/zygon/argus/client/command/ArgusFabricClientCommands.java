package dev.zygon.argus.client.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.command.CommandSource;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public class ArgusFabricClientCommands {

    public static void registerCommands() {
        ClientCommandManager.DISPATCHER.register(
                literal("argus")
                        .then(literal("otp")
                                .executes(ArgusFabricClientCommands::generateOneTimePassword)));
    }

    private static <S extends CommandSource> int generateOneTimePassword(CommandContext<S> context) {
        ArgusClientCommandHandler.INSTANCE.generateOneTimePassword();
        return 0;
    }
}
