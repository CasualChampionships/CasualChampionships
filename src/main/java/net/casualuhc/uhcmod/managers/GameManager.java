package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.*;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.casualuhc.uhcmod.utils.Scheduler.*;

public class GameManager {
	private static final List<Task> currentTasks = new LinkedList<>();
	private static Phase phase = Phase.NONE;
	private static StructureTemplate lobby;

	private static int ticks;
	private static boolean firstKill;
	private static boolean firstDeath;
	private static boolean firstCraft;

	static {
		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onServerTick(MinecraftServer server) {
				ticks++;
			}
		});
	}

	private GameManager() { }

	public static boolean isReadyForPlayers() {
		return phase.ordinal() >= 2 && GameSettings.FLOODGATE.getValue();
	}

	public static boolean isGameActive() {
		return phase.ordinal() >= 4;
	}

	public static void setPhase(Phase newPhase) {
		currentTasks.forEach(Task::cancel);
		currentTasks.clear();
		phase = newPhase;
	}

	public static Phase getPhase() {
		return phase;
	}

	public static boolean isPhase(Phase checkPhase) {
		return phase.equals(checkPhase);
	}

	public static void resetTrackers() {
		ticks = 0;
		firstKill = true;
		firstDeath = true;
		firstCraft = true;
	}

	public static boolean tryFirstKill() {
		if (firstKill) {
			firstKill = false;
			return true;
		}
		return false;
	}

	public static boolean tryFirstDeath() {
		if (firstDeath) {
			firstDeath = false;
			return true;
		}
		return false;
	}

	public static boolean tryFirstCraft() {
		if (firstCraft) {
			firstCraft = false;
			return true;
		}
		return false;
	}

	public static int ticksSinceStart() {
		return ticks;
	}

	public static void startCountDown() {
		PlayerUtils.messageEveryPlayer(
			Text.literal("Please stand still during the countdown so you don't fall when you get teleported!").formatted(Formatting.GOLD)
		);

		AtomicInteger integer = new AtomicInteger(10);
		scheduleInLoopPhaseTask(0, secondsToTicks(1), secondsToTicks(10), () -> {
			int i = integer.getAndDecrement();
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(String.valueOf(i)).formatted(Formatting.GREEN)));
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 3.0F);
			});
		});

		schedulePhaseTask(secondsToTicks(10), () -> {
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Good Luck!").formatted(Formatting.GOLD, Formatting.BOLD)));
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.MASTER, 1.0F, 1.0F);
				playerEntity.getHungerManager().setSaturationLevel(20F);
				EntityAttributeInstance instance = playerEntity.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
				if (instance != null) {
					instance.removeModifier(PlayerUtils.HEALTH_BOOST);
					instance.addPersistentModifier(new EntityAttributeModifier(PlayerUtils.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
				}
				playerEntity.setHealth(playerEntity.getMaxHealth());

				if (!TeamUtils.shouldIgnoreTeam(playerEntity.getScoreboardTeam()) && !playerEntity.isSpectator()) {
					if (playerEntity.getScoreboardTeam().getPlayerList().size() == 1) {
						PlayerUtils.grantAdvancement(playerEntity, UHCAdvancements.SOLOIST);
					}
					PlayerExtension.get(playerEntity).displayCoords = true;
					playerEntity.changeGameMode(GameMode.SURVIVAL);
					playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, secondsToTicks(5), 4, true, false));
					playerEntity.sendMessage(Text.literal("You can disable the coordinates above your hotbar by using /coords"), false);
					playerEntity.getInventory().clear();
					playerEntity.playerScreenHandler.clearCraftingSlots();
					playerEntity.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
					playerEntity.setExperienceLevel(0);
					PlayerUtils.setPlayerPlaying(playerEntity, true);
				} else {
					playerEntity.changeGameMode(GameMode.SPECTATOR);
				}

				playerEntity.dismountVehicle();
			});

			deleteLobby();
			MinecraftServer server = UHCMod.SERVER;
			server.getCommandManager().executeWithPrefix(server.getCommandSource(), "spreadplayers 0 0 500 2900 true @e[type=player]");
			EventHandler.onActive();
		});
	}

	public static void startGracePeriod() {
		PlayerUtils.forEveryPlayer(playerEntity -> {
			playerEntity.sendMessage(
				Text.literal("Grace Period will end in 10 minutes, once grace period is over PVP will be enabled and world border will start moving")
					.formatted(Formatting.GOLD),
				false
			);
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 1.0F);
		});

		AtomicInteger integer = new AtomicInteger(10);
		scheduleInLoopPhaseTask(minutesToTicks(2), minutesToTicks(2), minutesToTicks(10), () -> {
			int i = integer.addAndGet(-2);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.sendMessage(
					Text.literal("Grace Period will end in %s minutes".formatted(i))
						.formatted(Formatting.GOLD),
					false
				);
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 1.0F);
			});
		});

		schedulePhaseTask(minutesToTicks(10), () -> {
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.sendMessage(
					Text.literal("Grace Period is now over, PVP is enabled and world border has started! Good Luck!")
						.formatted(Formatting.RED, Formatting.BOLD),
					false
				);
				playerEntity.playSound(SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT, SoundCategory.MASTER, 1.0F, 1.0F);
			});

			EventHandler.onGracePeriodEnd();
		});
	}

	public static void worldBorderComplete() {
		UHCMod.LOGGER.info("World Border Completed");
		schedulePhaseTask(minutesToTicks(5), () -> {
			if (GameSettings.END_GAME_GLOW.getValue()) {
				UHCMod.LOGGER.info("Players are now glowing");
				PlayerUtils.forEveryPlayer(playerEntity -> {
					if (PlayerUtils.isPlayerSurvival(playerEntity)) {
						playerEntity.setGlowing(true);
					}
				});
			}
			if (GameSettings.GENERATE_PORTAL.getValue()) {
				ServerWorld overworld = UHCMod.SERVER.getOverworld();
				overworld.getPortalForcer().createPortal(BlockPos.ORIGIN, Direction.Axis.X);
				ServerWorld nether = UHCMod.SERVER.getWorld(World.NETHER);
				if (nether != null) {
					nether.getPortalForcer().createPortal(BlockPos.ORIGIN, Direction.Axis.X);
				}
			}
		});
	}

	public static void endUHC() {
		AbstractTeam team = TeamUtils.getLastTeam();
		if (team == null) {
			UHCMod.LOGGER.error("Last team was null!");
			return;
		}

		PlayerUtils.forEveryPlayer(player -> {
			if (player.getScoreboardTeam() == team || PlayerExtension.get(player).trueTeam == team) {
				PlayerUtils.grantAdvancement(player, UHCAdvancements.WINNER);
			}
		});

		UHCDataBase.incrementWinDataBase(team.getName());
		PlayerUtils.forEveryPlayer(playerEntity -> {
			playerEntity.setGlowing(false);
			playerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("%s has won!".formatted(team.getName())).formatted(team.getColor())));
			UHCDataBase.updateStats(playerEntity);
		});
		UHCDataBase.updateTotalDataBase();

		scheduleInLoopPhaseTask(0, 4, secondsToTicks(5), () -> {
			PlayerUtils.forEveryPlayer(playerEntity -> {
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
			});
			schedulePhaseTask(6, () -> {
				PlayerUtils.forEveryPlayer(playerEntity -> {
					playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.MASTER, 0.5f, 1f);
					playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
					playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
				});
			});
		});

		schedule(minutesToTicks(1), () -> {
			EventHandler.onSetup();
			EventHandler.onLobby();
		});
	}

	public static void generateLobby() {
		try {
			deleteLobby();
			Vec3i dimensions = getLobby().getSize();
			int x = -dimensions.getX() / 2;
			int y = UHCMod.SERVER.getOverworld().getTopY() - dimensions.getY() - 10;
			int z = -dimensions.getZ() / 2;
			BlockPos pos = new BlockPos(x, y ,z);
			getLobby().place(UHCMod.SERVER.getOverworld(), pos, pos, new StructurePlacementData(), Random.create(), 3);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				if (!playerEntity.hasPermissionLevel(2)) {
					playerEntity.interactionManager.changeGameMode(GameMode.ADVENTURE);
				} else {
					playerEntity.sendMessage(Text.literal(ChatColour.GREEN + "Successfully generated lobby!"), false);
				}
				playerEntity.teleport(
					UHCMod.SERVER.getOverworld(),
					Config.LOBBY_SPAWN.getX(),
					Config.LOBBY_SPAWN.getY(),
					Config.LOBBY_SPAWN.getZ(),
					0, 0
				);
			});
		} catch (Exception e) {
			UHCMod.LOGGER.error("Failed to generate lobby", e);
		}
	}

	public static void setBeforeGamerules() {
		MinecraftServer server = UHCMod.SERVER;
		GameRules gameRules = server.getGameRules();
		gameRules.get(GameRules.NATURAL_REGENERATION).set(true, server);
		gameRules.get(GameRules.DO_INSOMNIA).set(false, server);
		gameRules.get(GameRules.DO_FIRE_TICK).set(false, server);
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
		gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
		gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
		gameRules.get(GameRules.DROWNING_DAMAGE).set(false, server);
		gameRules.get(GameRules.DO_ENTITY_DROPS).set(false, server);
		gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
		gameRules.get(GameRules.SNOW_ACCUMULATION_HEIGHT).set(0, server);
		gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, server);
		server.setDifficulty(Difficulty.PEACEFUL, true);
		server.getOverworld().setTimeOfDay(6000); // 6000 = noon
		server.getOverworld().setWeather(999999, 0, false, false);
		server.getOverworld().setSpawnPos(new BlockPos(0, 250, 0), 0);
		server.getOverworld().getWorldBorder().setCenter(0, 0);
		server.getWorlds().forEach(serverWorld -> {
			serverWorld.getWorldBorder().setCenter(0, 0);
			serverWorld.getWorldBorder().setSize(6128);
		});
		GameSettings.PVP.setValue(false);
		if (UHCMod.HAS_CARPET) {
			CommandManager commandManager = server.getCommandManager();
			ServerCommandSource source = server.getCommandSource();
			commandManager.executeWithPrefix(source, "carpet commandLog ops");
			commandManager.executeWithPrefix(source, "carpet commandDistance ops");
			commandManager.executeWithPrefix(source, "carpet commandInfo ops");
			commandManager.executeWithPrefix(source, "carpet commandPerimeterInfo ops");
			commandManager.executeWithPrefix(source, "carpet commandProfile ops");
			commandManager.executeWithPrefix(source, "carpet commandScript ops");
			commandManager.executeWithPrefix(source, "carpet lightEngineMaxBatchSize 500");
			commandManager.executeWithPrefix(source, "carpet structureBlockLimit 256");
			commandManager.executeWithPrefix(source, "carpet fillLimit 1000000");
			commandManager.executeWithPrefix(source, "carpet fillUpdates false");
			commandManager.executeWithPrefix(source, "carpet setDefault commandLog ops");
			commandManager.executeWithPrefix(source, "carpet setDefault commandDistance ops");
			commandManager.executeWithPrefix(source, "carpet setDefault commandInfo ops");
			commandManager.executeWithPrefix(source, "carpet setDefault commandPerimeterInfo ops");
			commandManager.executeWithPrefix(source, "carpet setDefault commandProfile ops");
			commandManager.executeWithPrefix(source, "carpet setDefault commandScript ops");
			commandManager.executeWithPrefix(source, "carpet setDefault lightEngineMaxBatchSize 500");
			commandManager.executeWithPrefix(source, "carpet setDefault structureBlockLimit 256");
		}
	}

	public static void setDescriptor(MinecraftServer server) {
		MutableText description = Text.literal("            %s፠ %sWelcome to Casual UHC! %s፠\n".formatted(ChatColour.GOLD, ChatColour.AQUA, ChatColour.GOLD))
			.append(Text.literal("     Yes, it's back! Is your team prepared?").formatted(Formatting.DARK_AQUA));
		server.getServerMetadata().setDescription(description);
	}

	public static void setUHCGamerules() {
		MinecraftServer server = UHCMod.SERVER;
		GameRules gameRules = server.getGameRules();
		gameRules.get(GameRules.NATURAL_REGENERATION).set(false, server);
		gameRules.get(GameRules.DO_FIRE_TICK).set(true, server);
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
		gameRules.get(GameRules.FALL_DAMAGE).set(true, server);
		gameRules.get(GameRules.DROWNING_DAMAGE).set(true, server);
		gameRules.get(GameRules.DO_ENTITY_DROPS).set(true, server);
		gameRules.get(GameRules.DO_WEATHER_CYCLE).set(true, server);
		gameRules.get(GameRules.SNOW_ACCUMULATION_HEIGHT).set(1, server);
		gameRules.get(GameRules.RANDOM_TICK_SPEED).set(3, server);
		server.setDifficulty(Difficulty.HARD, true);
		server.getOverworld().setTimeOfDay(0);
		if (UHCMod.HAS_CARPET) {
			CommandManager commandManager = server.getCommandManager();
			ServerCommandSource source = server.getCommandSource();
			commandManager.executeWithPrefix(source, "spawn mobcaps set 7");
		}
	}

	public static StructureTemplate getLobby() {
		if (lobby == null) {
			try {
				InputStream inputStream = Files.newInputStream(Config.getConfig("lobby.nbt"));
				NbtCompound nbtStructure = NbtIo.readCompressed(inputStream);
				lobby = UHCMod.SERVER.getStructureTemplateManager().createTemplate(nbtStructure);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to load lobby.nbt");
			}
		}
		return lobby;
	}

	public static void schedulePhaseTask(int ticks, Runnable runnable) {
		currentTasks.add(schedule(ticks, runnable));
	}

	public static void scheduleInLoopPhaseTask(int delay, int interval, int until, Runnable runnable) {
		currentTasks.add(scheduleInLoop(delay, interval, until, runnable));
	}

	private static void deleteLobby() {
		ServerWorld world = UHCMod.SERVER.getOverworld();
		Vec3i dimensions = getLobby().getSize();
		int startY = world.getTopY() - dimensions.getY() - 11;
		int endY = world.getTopY();
		int startX = -(dimensions.getX() / 2 + 1);
		int startZ = -(dimensions.getZ() / 2 + 1);
		for (BlockPos pos : BlockPos.iterate(startX, startY, startZ, -startX, endY, -startZ)) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			Clearable.clear(blockEntity);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.SKIP_DROPS, 0);
		}
		for (Entity entity : world.iterateEntities()) {
			if (entity != null && entity.getScoreboardTags().contains("uhc")) {
				entity.kill();
			}
		}
	}
}

