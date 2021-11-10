package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

import java.util.List;

public class GameManagerHelper {
    public static void generateLobby()  {
        MinecraftServer server = UHCMod.UHCServer;
        server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:barrier hollow");
        server.getCommandManager().execute(server.getCommandSource(), "/summon minecraft:armor_stand 0 251 0 {Tags:[lobby,lobbycenter],NoGravity:1b,Small:1b,Invisible:1b,CustomNameVisible:1b,CustomName:\"[{\\\"text\\\":\\\"\\\"},{\\\"text\\\":\\\"UHC\\\",\\\"color\\\":\\\"gold\\\"},{\\\"text\\\":\\\" \\\\u2503 \\\"},{\\\"text\\\":\\\"Lobby\\\",\\\"color\\\":\\\"aqua\\\"}]\"}");
        server.getOverworld().setSpawnPos(new BlockPos(0, 250, 0), 0);
        server.getOverworld().getWorldBorder().setCenter(0, 0);
        server.setPvpEnabled(false);
        for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
            playerEntity.setPos(0, 253, 0);
            playerEntity.sendMessage(new LiteralText(ChatColour.GREEN + "[SETUP] Lobby has finished!"), false);
        }
    }

    public static void setGamerules() {
        MinecraftServer server = UHCMod.UHCServer;
        server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(true, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        server.setDifficulty(Difficulty.PEACEFUL, true);
        server.getOverworld().setTimeOfDay(6000); // 6000 = noon
        server.getOverworld().setWeather(999999, 0, false, false);
        ((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(0, server);
    }

    public static void sendReadyMessage() {
        MinecraftServer server = UHCMod.UHCServer;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        Text yesMessage = new LiteralText("YES").formatted(Formatting.BOLD, Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes")));
        Text noMessage = new LiteralText("NO").formatted(Formatting.BOLD, Formatting.RED).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no")));
        Text readyMessage = new LiteralText(
            """
            %s ========================================= %s
            
            Is your team ready?
            
            
            """.formatted(ChatColour.GOLD, ChatColour.RESET)
        ).append(yesMessage).append("           ").append(noMessage).append(
            """
            
            
            %s ========================================= %s
            """.formatted(ChatColour.GOLD, ChatColour.RESET)
        );


        for (ServerPlayerEntity player : players) {
            player.sendMessage(readyMessage, false);
        }
    }
}
