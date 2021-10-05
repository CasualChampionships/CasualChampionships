package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.GameState;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class OpenLobbyCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("openlobby")
                .executes(context -> {
                    GameManager.LOBBY.getFunction().executeFunction();
                    return 1;
                }));
    }
}
