package net.casualuhc.uhcmod.helpers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.casualuhc.uhcmod.utils.ChatColour;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public class SetupHelper {
    public static void GenerateLobby()  {
        MinecraftServer server = UHCMod.UHCServer;
        server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:barrier hollow");
        server.getCommandManager().execute(server.getCommandSource(), "/summon minecraft:armor_stand 0 251 0 {Tags:[lobby,lobbycenter],NoGravity:1b,Small:1b,Invisible:1b,CustomNameVisible:1b,CustomName:\"[{\\\"text\\\":\\\"\\\"},{\\\"text\\\":\\\"UHC\\\",\\\"color\\\":\\\"gold\\\"},{\\\"text\\\":\\\" \\\\u2503 \\\"},{\\\"text\\\":\\\"Lobby\\\",\\\"color\\\":\\\"aqua\\\"}]\"}");
        server.getOverworld().setSpawnPos(new BlockPos(0, 250, 0), 0);
        server.getOverworld().getWorldBorder().setCenter(0, 0);
        for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
            playerEntity.setPos(0, 253, 0);
            playerEntity.sendMessage(new LiteralText(ChatColour.GREEN + "[SETUP] Lobby has finished!"), false);
        }
    }

    public static void SetGamerules() {
        MinecraftServer server = UHCMod.UHCServer;
        server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(true, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        server.setDifficulty(Difficulty.PEACEFUL, true);
        server.getOverworld().setTimeOfDay(6000); // 6000 = noon
        server.getOverworld().setWeather(999999, 0, false, false);
        ((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(0, server);
    }
}
