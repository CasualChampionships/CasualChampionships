package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import java.util.UUID;

public class GameManagerUtils {

    public static final UUID HEALTH_BOOST = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57");

    public static void generateLobby()  {
        MinecraftServer server = UHCMod.UHCServer;
        server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:barrier hollow");
        server.getCommandManager().execute(server.getCommandSource(), "/summon minecraft:armor_stand 0 251 0 {Tags:[lobby,lobbycenter],NoGravity:1b,Small:1b,Invisible:1b,CustomNameVisible:1b,CustomName:\"[{\\\"text\\\":\\\"\\\"},{\\\"text\\\":\\\"UHC\\\",\\\"color\\\":\\\"gold\\\"},{\\\"text\\\":\\\" \\\\u2503 \\\"},{\\\"text\\\":\\\"Lobby\\\",\\\"color\\\":\\\"aqua\\\"}]\"}");
        PlayerUtils.messageEveryPlayer(new LiteralText(ChatColour.GREEN + "[SETUP] Lobby has finished!"));
    }

    public static void setBeforeGamerules() {
        MinecraftServer server = UHCMod.UHCServer;
        server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(true, server);
        server.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        server.setDifficulty(Difficulty.PEACEFUL, true);
        server.getOverworld().setTimeOfDay(6000); // 6000 = noon
        server.getOverworld().setWeather(999999, 0, false, false);
        ((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(0, server);
        setDescriptor(server);
        server.getOverworld().setSpawnPos(new BlockPos(0, 250, 0), 0);
        server.getOverworld().getWorldBorder().setCenter(0, 0);
        server.getWorlds().forEach(serverWorld -> {
            serverWorld.getWorldBorder().setCenter(0, 0);
            serverWorld.getWorldBorder().setSize(6128);
        });
        server.setPvpEnabled(false);
        if (UHCMod.isCarpetInstalled) {
            CommandManager commandManager = server.getCommandManager();
            ServerCommandSource source = server.getCommandSource();
            commandManager.execute(source, "/carpet commandLog ops");
            commandManager.execute(source, "/carpet commandDistance ops");
            commandManager.execute(source, "/carpet commandInfo ops");
            commandManager.execute(source, "/carpet commandPerimeterInfo ops");
            commandManager.execute(source, "/carpet commandProfile ops");
            commandManager.execute(source, "/carpet commandScript ops");
            commandManager.execute(source, "/carpet lightEngineMaxBatchSize 500");
            commandManager.execute(source, "/carpet setDefault commandLog ops");
            commandManager.execute(source, "/carpet setDefault commandDistance ops");
            commandManager.execute(source, "/carpet setDefault commandInfo ops");
            commandManager.execute(source, "/carpet setDefault commandPerimeterInfo ops");
            commandManager.execute(source, "/carpet setDefault commandProfile ops");
            commandManager.execute(source, "/carpet setDefault commandScript ops");
            commandManager.execute(source, "/carpet setDefault lightEngineMaxBatchSize 500");
        }
    }

    private static void setDescriptor(MinecraftServer server) {
        MutableText description = new LiteralText("            %s፠ %sWelcome to Casual UHC! %s፠\n".formatted(ChatColour.GOLD, ChatColour.AQUA, ChatColour.GOLD))
            .append(new LiteralText("     Yes, it's back! Is your team prepared?").formatted(Formatting.DARK_AQUA));
        server.getServerMetadata().setDescription(description);
    }

    public static void setUHCGamerules() {
        MinecraftServer server = UHCMod.UHCServer;
        setDescriptor(server);
        server.getGameRules().get(GameRules.NATURAL_REGENERATION).set(false, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(true, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        server.setDifficulty(Difficulty.HARD, true);
        ((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(3, server);
        if (UHCMod.isCarpetInstalled) {
            CommandManager commandManager = server.getCommandManager();
            ServerCommandSource source = server.getCommandSource();
            commandManager.execute(source, "/spawn mobcaps set 7");
        }
    }

    public static void startCountDown(ThreadGroup threadGroup) {
        // Pushing to a new thread to countdown
        Thread countDownThread = new Thread(threadGroup, () -> {
            for (int i = 10; i > 0; i--) {
                final int countDown = i;
                PlayerUtils.forEveryPlayer(playerEntity -> {
                    playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText(String.valueOf(countDown)).formatted(Formatting.GREEN)));
                    playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0F, 3.0F);
                });

                try { Thread.sleep(1000); }
                catch (InterruptedException ignored) { }

                if (!GameManager.isPhase(Phase.START)) {
                    PlayerUtils.messageEveryPlayer(new LiteralText("Starting has been canceled by an operator!").formatted(Formatting.RED));
                    return;
                }
            }
            PlayerUtils.forEveryPlayer(playerEntity -> {
                playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("Good Luck!").formatted(Formatting.GOLD, Formatting.BOLD)));
                playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1.0F, 1.0F);
                playerEntity.getHungerManager().setSaturationLevel(20F);
                EntityAttributeInstance instance = playerEntity.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (instance != null) {
                    instance.removeModifier(HEALTH_BOOST);
                    instance.addPersistentModifier(new EntityAttributeModifier(HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
                }
                playerEntity.setHealth(playerEntity.getMaxHealth());
            });
            // Pushing back to main thread
            MinecraftServer server = UHCMod.UHCServer;
            PlayerUtils.forEveryPlayer(playerEntity -> {
                if (!TeamUtils.isNonTeam(playerEntity.getScoreboardTeam()) && !playerEntity.isSpectator()) {
                    ((ServerPlayerMixinInterface) playerEntity).setCoordsBoolean(true);
                    playerEntity.changeGameMode(GameMode.SURVIVAL);
                    playerEntity.sendMessage(new LiteralText("You can disable the coordinates above your hotbar by using /coords"), false);
                    playerEntity.getInventory().clear();
                }
                else {
                    playerEntity.changeGameMode(GameMode.SPECTATOR);
                }
            });
            server.execute(() -> {
                server.getCommandManager().execute(server.getCommandSource(), "/fill 17 250 17 -18 255 -17 minecraft:air");
                server.getCommandManager().execute(server.getCommandSource(), "/kill @e[tag=lobby]");
                server.getCommandManager().execute(server.getCommandSource(), "/spreadplayers 0 0 500 2900 true @e[type=player]");
                Phase.ACTIVE.run();
            });

        }, "UHC Countdown Thread");
        countDownThread.setDaemon(true);
        countDownThread.start();
    }

    public static void startPVPCountdown(ThreadGroup threadGroup) {
        Thread gracePeriodThread = new Thread(threadGroup, () -> {
            for (int i = 10; i > 0; i--) {
                switch (i) {
                    case 10, 5, 2, 1 -> {
                        final int minutes = i;
                        PlayerUtils.forEveryPlayer(playerEntity -> {
                            playerEntity.sendMessage(new LiteralText("Grace Period will end in %d minutes".formatted(minutes)).formatted(Formatting.GOLD), false);
                            playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f ,1f);
                        });
                    }
                }
                try { Thread.sleep(60000); }
                catch (InterruptedException ignored) { }
                if (!GameManager.isPhase(Phase.ACTIVE)) {
                    return;
                }
            }
            MinecraftServer server = UHCMod.UHCServer;
            server.setPvpEnabled(true);
            PlayerUtils.forEveryPlayer(playerEntity -> {
                playerEntity.sendMessage(new LiteralText("Grace Period is now over! Good Luck!").formatted(Formatting.RED, Formatting.BOLD), false);
                playerEntity.playSound(SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT, SoundCategory.MASTER, 1f ,1f);
            });
        }, "UHC PVP Timer Thread");
        gracePeriodThread.setDaemon(true);
        gracePeriodThread.start();
    }

    public static void endUHC(ThreadGroup threadGroup) {
        AbstractTeam team = TeamUtils.getLastTeam();
        if (team == null) {
            UHCMod.UHCLogger.error("Last team was null!");
            return;
        }
        Thread thread = new Thread(threadGroup, () -> {
            PlayerUtils.forEveryPlayer(playerEntity -> {
                playerEntity.setGlowing(false);
                playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("%s has won!".formatted(team.getName())).formatted(team.getColor())));
            });
            for (int i = 0; i < 10; i++) {
                try {
                    PlayerUtils.forEveryPlayer(playerEntity -> {
                        playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 0.5f ,1f);
                        playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
                        playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR, SoundCategory.MASTER, 0.5f ,1f);
                    });
                    Thread.sleep(300);
                    PlayerUtils.forEveryPlayer(playerEntity -> {
                        playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.MASTER, 0.5f, 1f);
                        playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
                        playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
                    });
                    Thread.sleep(200);
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }, "UHC win thread");
        thread.setDaemon(true);
        thread.start();
    }

    public static void worldBorderFinishedPre() {

    }

    public static void worldBorderFinishedPost() {
        if (GameSettings.END_GAME_GLOW.getValue()) {
            PlayerUtils.forEveryPlayer(playerEntity -> {
                if (playerEntity.interactionManager.getGameMode() == GameMode.SURVIVAL) {
                    playerEntity.setGlowing(true);
                }
            });
        }
    }
}