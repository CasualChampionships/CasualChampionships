package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class GameManagerUtils {

    public static void generateLobby()  {
        MinecraftServer server = UHCMod.UHCServer;
        server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:barrier hollow");
        server.getCommandManager().execute(server.getCommandSource(), "/summon minecraft:armor_stand 0 251 0 {Tags:[lobby,lobbycenter],NoGravity:1b,Small:1b,Invisible:1b,CustomNameVisible:1b,CustomName:\"[{\\\"text\\\":\\\"\\\"},{\\\"text\\\":\\\"UHC\\\",\\\"color\\\":\\\"gold\\\"},{\\\"text\\\":\\\" \\\\u2503 \\\"},{\\\"text\\\":\\\"Lobby\\\",\\\"color\\\":\\\"aqua\\\"}]\"}");
        server.getOverworld().setSpawnPos(new BlockPos(0, 250, 0), 0);
        server.getOverworld().getWorldBorder().setCenter(0, 0);
        server.getWorlds().forEach(serverWorld -> {
            serverWorld.getWorldBorder().setCenter(0, 0);
            serverWorld.getWorldBorder().setSize(6128);
        });
        server.setPvpEnabled(false);
        PlayerUtils.messageEveryPlayer(new LiteralText(ChatColour.GREEN + "[SETUP] Lobby has finished!"));
    }

    public static void setBeforeGamerules() {
        MinecraftServer server = UHCMod.UHCServer;
        server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(true, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        server.setDifficulty(Difficulty.PEACEFUL, true);
        server.getOverworld().setTimeOfDay(6000); // 6000 = noon
        server.getOverworld().setWeather(999999, 0, false, false);
        ((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(0, server);
    }

    public static void setUHCGamerules() {
        MinecraftServer server = UHCMod.UHCServer;
        server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        server.setDifficulty(Difficulty.HARD, true);
    }

    public static void startCountDown(ThreadGroup threadGroup) {
        // Pushing to a new thread to countdown
        Thread countDownThread = new Thread(threadGroup, () -> {
            for (int i = 10; i > 0; i--) {
                final int countDown = i;
                PlayerUtils.forEveryPlayer(playerEntity -> playerEntity.networkHandler.sendPacket(
                        new TitleS2CPacket(new LiteralText(String.valueOf(countDown)).formatted(Formatting.GREEN))
                ));

                try { Thread.sleep(1000); }
                catch (InterruptedException ignored) { }

                if (GameManager.currentPhase != GameManager.Phase.START) {
                    PlayerUtils.messageEveryPlayer(new LiteralText("Starting canceled by operator"));
                    return;
                }
            }
            PlayerUtils.forEveryPlayer(playerEntity -> {
                playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("Welcome to Casual UHC").formatted(Formatting.GOLD, Formatting.BOLD)));
                playerEntity.getHungerManager().setSaturationLevel(20F);
            });
            // Pushing back to main thread
            MinecraftServer server = UHCMod.UHCServer;
            PlayerUtils.forEveryPlayer(playerEntity -> {
                if (!TeamUtils.isNonTeam(playerEntity.getScoreboardTeam()) && !playerEntity.isSpectator()) {
                    ((ServerPlayerMixinInterface) playerEntity).setCoordsBoolean(true);
                    playerEntity.changeGameMode(GameMode.SURVIVAL);
                }
            });
            server.execute(() -> {
                server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:air");
                server.getCommandManager().execute(server.getCommandSource(), "/kill @e[tag=lobby]");
                server.getCommandManager().execute(server.getCommandSource(), "/spreadplayers 0 0 500 2900 true @e[type=player]");
                GameManager.Phase.ACTIVE.run();
            });
        }, "UHC Countdown Thread");
        countDownThread.setDaemon(true);
        countDownThread.start();
    }

    public static void startPVPCountdown(ThreadGroup threadGroup) {
        Thread gracePeriodThread = new Thread(threadGroup, () -> {
            for (int i = 10; i > 0; i--) {
                switch (i) {
                    case 10, 5, 2, 1 -> PlayerUtils.messageEveryPlayer(
                        new LiteralText("PVP will begin in %d minutes".formatted(i)).formatted(Formatting.GOLD)
                    );
                }
                try { Thread.sleep(60000); }
                catch (InterruptedException ignored) { }

                if (GameManager.currentPhase != GameManager.Phase.ACTIVE) {
                    return;
                }
            }
            MinecraftServer server = UHCMod.UHCServer;
            server.execute(() -> server.setPvpEnabled(true));
            PlayerUtils.messageEveryPlayer(new LiteralText("PVP is now enabled").formatted(Formatting.RED, Formatting.BOLD));
        }, "UHC PVP Timer Thread");
        gracePeriodThread.setDaemon(true);
        gracePeriodThread.start();
    }
}
