package net.casualuhc.uhcmod.helpers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.ChatColour;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetupHelper {

    public static void GenerateLobby()  {
        MinecraftServer server = UHCMod.UHCServer;
        server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:barrier hollow");
        server.getOverworld().setSpawnPos(new BlockPos(0, 253, 0), 0);
        server.getOverworld().getWorldBorder().setCenter(0, 0);
        for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
            playerEntity.sendMessage(new LiteralText(ChatColour.GREEN + "[LOBBY] Setup has finished!"), false);
        }
    }
}
