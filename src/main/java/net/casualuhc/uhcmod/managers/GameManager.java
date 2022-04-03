package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.utils.*;
import net.casualuhc.uhcmod.utils.Event.Events;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {
	public static final GameManager INSTANCE = new GameManager();

	private final ScheduledExecutorService EXECUTOR;
	private final List<ScheduledFuture<?>> FUTURES;

	private Phase currentPhase = Phase.NONE;
	private boolean floodgateOpen = false;

	private GameManager() {
        this.EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> new Thread(new ThreadGroup("UHC Thread Group"), r));
        this.FUTURES = new ArrayList<>();
	}

	public void setCurrentPhase(Phase phase) {
        this.FUTURES.forEach(scheduledFuture -> scheduledFuture.cancel(true));
		this.FUTURES.clear();
        this.currentPhase = phase;
	}

	public void setFloodgates(boolean bool) {
        this.floodgateOpen = bool;
	}

	public Phase getCurrentPhase() {
		return this.currentPhase;
	}

	public boolean isReadyForPlayers() {
		return this.currentPhase.ordinal() >= 2 && this.floodgateOpen;
	}

	public boolean isGameActive() {
		return this.currentPhase.ordinal() >= 4;
	}

	public boolean isPhase(Phase phase) {
		return this.currentPhase.equals(phase);
	}

    public void shutdown() {
        this.EXECUTOR.shutdown();
		this.FUTURES.forEach(scheduledFuture -> scheduledFuture.cancel(true));
		this.FUTURES.clear();
    }

	public void startCountDown() {
		PlayerUtils.messageEveryPlayer(
			new LiteralText("Please stand still during the countdown so you don't fall when you get teleported!").formatted(Formatting.GOLD)
		);

		// We repeat the code with interval 1 second
		AtomicInteger integer = new AtomicInteger(10);
		ScheduledFuture<?> future = this.EXECUTOR.scheduleAtFixedRate(() -> {
			int i = integer.getAndDecrement();
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText(String.valueOf(i)).formatted(Formatting.GREEN)));
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0F, 3.0F);
			});
		}, 0, 1, TimeUnit.SECONDS);
        this.FUTURES.add(future);

        this.FUTURES.add(this.EXECUTOR.schedule(() -> {
			future.cancel(true);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("Good Luck!").formatted(Formatting.GOLD, Formatting.BOLD)));
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1.0F, 1.0F);
				playerEntity.getHungerManager().setSaturationLevel(20F);
				EntityAttributeInstance instance = playerEntity.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
				if (instance != null) {
					instance.removeModifier(PlayerUtils.HEALTH_BOOST);
					instance.addPersistentModifier(new EntityAttributeModifier(PlayerUtils.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
				}
				playerEntity.setHealth(playerEntity.getMaxHealth());
			});
			// Pushing back to main thread
			MinecraftServer server = UHCMod.UHC_SERVER;
			PlayerUtils.forEveryPlayer(playerEntity -> {
				if (!TeamUtils.isNonTeam(playerEntity.getScoreboardTeam()) && !playerEntity.isSpectator()) {
					((ServerPlayerMixinInterface) playerEntity).setCoordsBoolean(true);
					playerEntity.changeGameMode(GameMode.SURVIVAL);
					playerEntity.sendMessage(new LiteralText("You can disable the coordinates above your hotbar by using /coords"), false);
					playerEntity.getInventory().clear();
					PlayerUtils.setPlayerPlaying(playerEntity, true);
				}
				else {
					playerEntity.changeGameMode(GameMode.SPECTATOR);
				}
			});

			server.execute(() -> {
				server.getCommandManager().execute(server.getCommandSource(), "/fill 24 250 24 -25 289 -25 air");
				server.getCommandManager().execute(server.getCommandSource(), "/spreadplayers 0 0 500 2900 true @e[type=player]");
				Events.ON_ACTIVE.trigger();
			});
		}, 10, TimeUnit.SECONDS));
	}

	public void startGracePeriod() {
		PlayerUtils.forEveryPlayer(playerEntity -> {
			playerEntity.sendMessage(
				new LiteralText("Grace Period will end in 10 minutes, once grace period is over PVP will be enabled and world border will start moving")
					.formatted(Formatting.GOLD),
				false
			);
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
		});

		AtomicInteger integer = new AtomicInteger(10);
		ScheduledFuture<?> future = this.EXECUTOR.scheduleAtFixedRate(() -> {
			int i = integer.addAndGet(-2);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.sendMessage(
					new LiteralText("Grace Period will end in %s minutes".formatted(i))
						.formatted(Formatting.GOLD),
					false
				);
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
			});
		}, 2, 2, TimeUnit.MINUTES);
		this.FUTURES.add(future);

        this.FUTURES.add(this.EXECUTOR.schedule(() -> {
			future.cancel(true);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.sendMessage(
					new LiteralText("Grace Period is now over, PVP is enabled and world border has started! Good Luck!")
						.formatted(Formatting.RED, Formatting.BOLD),
					false
				);
				playerEntity.playSound(SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT, SoundCategory.MASTER, 1f, 1f);
			});

			Events.GRACE_PERIOD_FINISH.trigger();
		}, 10, TimeUnit.MINUTES));
	}

	public void worldBorderComplete() {
		UHCMod.UHCLogger.info("World Border Completed");
        this.EXECUTOR.schedule(() -> {
			if (GameSettings.END_GAME_GLOW.getValue()) {
				UHCMod.UHCLogger.info("Players are now glowing");
				PlayerUtils.forEveryPlayer(playerEntity -> {
					if (PlayerUtils.isPlayerSurvival(playerEntity)) {
						playerEntity.setGlowing(true);
					}
				});
			}
		}, 5, TimeUnit.MINUTES);
	}

	public void endUHC() {
		AbstractTeam team = TeamUtils.getLastTeam();
		if (team == null) {
			UHCMod.UHCLogger.error("Last team was null!");
			return;
		}
		UHCDataBase.INSTANCE.incrementWinDataBase(team.getName());
		PlayerUtils.forEveryPlayer(playerEntity -> {
			playerEntity.setGlowing(false);
			playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("%s has won!".formatted(team.getName())).formatted(team.getColor())));
			UHCDataBase.INSTANCE.updateStats(playerEntity);
		});
		UHCDataBase.INSTANCE.updateTotalDataBase();

		ScheduledFuture<?> future = this.EXECUTOR.scheduleWithFixedDelay(() -> {
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
			});
			ExceptionUtils.runSafe(() -> Thread.sleep(300));

			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
			});
		}, 0, 200, TimeUnit.MILLISECONDS);
        this.FUTURES.add(future);

        this.FUTURES.add(this.EXECUTOR.schedule(() -> future.cancel(true), 5, TimeUnit.SECONDS));
	}

	public void generateLobby() {
		ExceptionUtils.runSafe(() -> {
			InputStream inputStream = GameManager.class.getClassLoader().getResourceAsStream("assets/uhcmod/structures/lobby.nbt");
			if (inputStream == null) {
				throw new IOException("Could not find lobby structure");
			}
			NbtCompound nbtStructure = NbtIo.readCompressed(inputStream);
			Structure lobbyStructure = UHCMod.UHC_SERVER.getStructureManager().createStructure(nbtStructure);
			BlockPos pos = new BlockPos(-25, 250, -25);
			lobbyStructure.place(UHCMod.UHC_SERVER.getOverworld(), pos, pos, new StructurePlacementData(), new Random(), 3);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.teleport(UHCMod.UHC_SERVER.getOverworld(), 0, 253, 0, 0, 0);
				playerEntity.sendMessage(new LiteralText(ChatColour.GREEN + "Successfully generated lobby!"), false);
			});
		});
	}

	public void setBeforeGamerules() {
		MinecraftServer server = UHCMod.UHC_SERVER;
		GameRules gameRules = server.getGameRules();
		gameRules.get(GameRules.NATURAL_REGENERATION).set(true, server);
		gameRules.get(GameRules.DO_INSOMNIA).set(false, server);
		gameRules.get(GameRules.DO_FIRE_TICK).set(false, server);
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
		gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
		gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
		server.setDifficulty(Difficulty.PEACEFUL, true);
		server.getOverworld().setTimeOfDay(6000); // 6000 = noon
		server.getOverworld().setWeather(999999, 0, false, false);
		((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(0, server);
		server.getOverworld().setSpawnPos(new BlockPos(0, 250, 0), 0);
		server.getOverworld().getWorldBorder().setCenter(0, 0);
		server.getWorlds().forEach(serverWorld -> {
			serverWorld.getWorldBorder().setCenter(0, 0);
			serverWorld.getWorldBorder().setSize(6128);
		});
		GameSettings.PVP.setValue(false);
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
			commandManager.execute(source, "/carpet structureBlockLimit 256");
			commandManager.execute(source, "/carpet fillLimit 1000000");
			commandManager.execute(source, "/carpet fillUpdates false");
			commandManager.execute(source, "/carpet setDefault commandLog ops");
			commandManager.execute(source, "/carpet setDefault commandDistance ops");
			commandManager.execute(source, "/carpet setDefault commandInfo ops");
			commandManager.execute(source, "/carpet setDefault commandPerimeterInfo ops");
			commandManager.execute(source, "/carpet setDefault commandProfile ops");
			commandManager.execute(source, "/carpet setDefault commandScript ops");
			commandManager.execute(source, "/carpet setDefault lightEngineMaxBatchSize 500");
			commandManager.execute(source, "/carpet setDefault structureBlockLimit 256");
		}
	}

	public void setDescriptor(MinecraftServer server) {
		MutableText description = new LiteralText("            %s፠ %sWelcome to Casual UHC! %s፠\n".formatted(ChatColour.GOLD, ChatColour.AQUA, ChatColour.GOLD))
			.append(new LiteralText("     Yes, it's back! Is your team prepared?").formatted(Formatting.DARK_AQUA));
		server.getServerMetadata().setDescription(description);
	}

	public void setUHCGamerules() {
		MinecraftServer server = UHCMod.UHC_SERVER;
		GameRules gameRules = server.getGameRules();
		gameRules.get(GameRules.NATURAL_REGENERATION).set(false, server);
		gameRules.get(GameRules.DO_FIRE_TICK).set(true, server);
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
		gameRules.get(GameRules.FALL_DAMAGE).set(true, server);
		server.setDifficulty(Difficulty.HARD, true);
		((IntRuleMixinInterface) server.getGameRules().get(GameRules.RANDOM_TICK_SPEED)).setIntegerValue(3, server);
		if (UHCMod.isCarpetInstalled) {
			CommandManager commandManager = server.getCommandManager();
			ServerCommandSource source = server.getCommandSource();
			commandManager.execute(source, "/spawn mobcaps set 7");
		}
	}
}

