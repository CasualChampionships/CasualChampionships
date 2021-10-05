package net.casualuhc.uhcmod.helpers;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SetupHelper {

    public static void GenerateLobby(CommandManager manager, ServerCommandSource source)  {
        manager.execute(source, "/fill 17 250 17 -18 255 -17 minecraft:barrier hollow");
        manager.execute(source, "/setworldspawn 0 253 0");
        manager.execute(source, "/worldborder center 0 0");
        manager.execute(source, "/tellraw @a [LOBBY] Setup has finished!");
    }
}
