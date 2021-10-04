package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.GameState;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class SetupCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(literal("setup")
            .executes(context -> {
                    GameManager.setGameState(GameState.SETUP);
        return 1;
    }));
    }
}
