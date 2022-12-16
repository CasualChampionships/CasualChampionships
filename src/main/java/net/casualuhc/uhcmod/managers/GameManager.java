package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.GoldenHeadRecipe;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.scheduling.Task;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.utils.uhc.OneTimeAchievement;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.casualuhc.uhcmod.utils.uhc.UHCUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.casualuhc.uhcmod.utils.scheduling.Scheduler.*;

public class GameManager {
	private static final List<Task> currentTasks = new LinkedList<>();
	private static final Set<OneTimeAchievement> claimed = new HashSet<>();

	private static Phase phase = Phase.NONE;
	private static StructureTemplate lobby;
	private static CommandBossBar bossBar;

	private static long startTime = Long.MAX_VALUE;
	private static int ticks;

	private GameManager() { }

	/**
	 * Checks whether the UHC is at a certain Phase.
	 *
	 * @param checkPhase the phase to check against.
	 * @return whether that is the current phase.
	 */
	public static boolean isPhase(Phase checkPhase) {
		return phase.equals(checkPhase);
	}

	/**
	 * Gets the current Phase.
	 *
	 * @return the current Phase.
	 */
	public static Phase getPhase() {
		return phase;
	}

	/**
	 * Sets the current Phase and cancels any tasks that were scheduled for the previous Phase.
	 *
	 * @param newPhase the new Phase.
	 */
	public static void setPhase(Phase newPhase) {
		currentTasks.forEach(Task::cancel);
		currentTasks.clear();
		phase = newPhase;
	}

	/**
	 * Schedules a task that may be cancelled if the Phase changes.
	 *
	 * @param ticks the delay before running the task.
	 * @param runnable the runnable to run.
	 * @see net.casualuhc.uhcmod.utils.scheduling.Scheduler#schedule(int, Runnable)
	 */
	public static void schedulePhaseTask(int ticks, Runnable runnable) {
		currentTasks.add(schedule(ticks, runnable));
	}

	/**
	 * Schedules a task to be run multiple times determined by a given interval util a certain time.
	 * The task may be cancelled if the Phase changes.
	 *
	 * @param delay the amount of ticks to initially delay
	 * @param interval the interval between executions
	 * @param duration the duration, this starts after the delay
	 * @param runnable the runnable to run
	 * @see net.casualuhc.uhcmod.utils.scheduling.Scheduler#scheduleInLoop(int, int, int, Runnable)
	 */
	public static void scheduleInLoopPhaseTask(int delay, int interval, int duration, Runnable runnable) {
		currentTasks.add(scheduleInLoop(delay, interval, duration, runnable));
	}

	/**
	 * Checks whether the Server is ready to let players join the server.
	 *
	 * @return whether players can join.
	 */
	public static boolean isReadyForPlayers() {
		return phase.ordinal() >= 2 && GameSettings.FLOODGATE.getValue();
	}

	/**
	 * Sets the UHC start time (from epoch in milliseconds).
	 *
	 * @param newStartTime the new start time.
	 */
	public static void setStartTime(long newStartTime) {
		startTime = newStartTime;

		if (startTime - 30 * 60 * 1000 >= System.currentTimeMillis()) {
			generateBossBar();
		}
	}

	/**
	 * Whether the UHC is currently active.
	 *
	 * @return whether the UHC is active.
	 */
	public static boolean isGameActive() {
		return phase.ordinal() >= 4;
	}

	/**
	 * Checks if a one time achievement is unclaimed.
	 *
	 * @param achievement the achievement to check.
	 * @return whether the achievement is unclaimed.
	 */
	public static boolean isUnclaimed(OneTimeAchievement achievement) {
		return claimed.add(achievement);
	}

	/**
	 * Gets the amount of time since the UHC game started.
	 *
	 * @return the uptime in ticks.
	 */
	public static int gameUptime() {
		return ticks;
	}

	/**
	 * Resets the uptime tick counter and the claimed achievements.
	 */
	public static void resetTrackers() {
		ticks = 0;
		claimed.clear();
	}

	/**
	 * Gets all the custom recipes for the UHC.
	 * @return the custom recipes.
	 */
	public static Iterable<Recipe<?>> getCustomRecipes() {
		return List.of(new GoldenHeadRecipe());
	}

	/**
	 * Gets the lobby structure template, loads it if it has not yet been loaded.
	 *
	 * @return the lobby structure.
	 */
	public static StructureTemplate getLobby() {
		if (lobby == null) {
			try {
				InputStream inputStream = Files.newInputStream(Config.getConfig("lobbies").resolve(Config.CURRENT_EVENT.getLobbyName() + ".nbt"));
				NbtCompound nbtStructure = NbtIo.readCompressed(inputStream);
				lobby = UHCMod.SERVER.getStructureTemplateManager().createTemplate(nbtStructure);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to load lobby structure");
			}
		}
		return lobby;
	}

	public static void noop() { }

	// UHC logic

	static {
		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onPlayerJoin(ServerPlayerEntity player) {
				if (isPhase(Phase.LOBBY) && bossBar != null) {
					bossBar.addPlayer(player);
				}
			}

			@Override
			public void onPlayerLeave(ServerPlayerEntity player) {
				UHCDataBase.updateStats(player);
			}

			@Override
			public void onServerTick(MinecraftServer server) {
				ticks++;
				if (isPhase(Phase.LOBBY) && bossBar != null && ticks % 100 == 0 && startTime - 30 * 60 * 1000 < System.currentTimeMillis()) {
					long millisLeft = startTime - System.currentTimeMillis();
					float percentLeft = millisLeft / (float) (30 * 60 * 1000);
					bossBar.setPercent(MathHelper.clamp(1 - percentLeft, 0, 1));
					long minutesLeft = millisLeft / (60 * 1000);
					MutableText startTime = minutesLeft <= 0 ? Text.literal("Starting Soon!") : Text.literal("Starting in %d minute%s!".formatted(minutesLeft, minutesLeft == 1 ? "" : "s"));
					bossBar.setName(Config.CURRENT_EVENT.getBossBarMessage().append(". ").append(startTime.formatted(Formatting.GREEN, Formatting.BOLD)));
				}
			}
		});

		EventHandler.register(new UHCEvents() {
			@Override
			public void onSetup() {
				setPhase(Phase.SETUP);
				deleteAllBossBars();
				UHCUtils.setLobbyGamerules();
			}

			@Override
			public void onLobby() {
				setPhase(Phase.LOBBY);
				generateLobby();
				generateBossBar();
			}

			@Override
			public void onReady() {
				setPhase(Phase.READY);
			}

			@Override
			public void onStart() {
				setPhase(Phase.START);
				hideBossBar();
				startCountDown();
			}

			@Override
			public void onActive() {
				setPhase(Phase.ACTIVE);
				resetTrackers();
				startGracePeriod();
				UHCUtils.setUHCGamerules();
			}

			@Override
			public void onEnd() {
				setPhase(Phase.END);
				endUHC();
			}

			@Override
			public void onGracePeriodEnd() {
				GameSettings.PVP.setValue(true);
			}

			@Override
			public void onWorldBorderComplete() {
				worldBorderComplete();
			}
		});
	}

	private static void startCountDown() {
		AtomicInteger integer = new AtomicInteger(10);
		scheduleInLoopPhaseTask(0, secondsToTicks(1), secondsToTicks(10), () -> {
			int i = integer.getAndDecrement();
			PlayerManager.forEveryPlayer(playerEntity -> {
				playerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(String.valueOf(i)).formatted(Formatting.GREEN)));
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 3.0F);
			});
		});

		schedulePhaseTask(secondsToTicks(10), () -> {
			PlayerManager.forEveryPlayer(PlayerManager::setPlayerForUHC);
			deleteLobby();

			MinecraftServer server = UHCMod.SERVER;
			server.getCommandManager().executeWithPrefix(server.getCommandSource(), "spreadplayers 0 0 500 2900 true @e[type=player]");
			EventHandler.onActive();
		});
	}

	private static void startGracePeriod() {
		PlayerManager.forEveryPlayer(playerEntity -> {
			playerEntity.sendMessage(
				Text.literal("Grace Period will end in 10 minutes, once grace period is over PVP will be enabled and world border will start moving")
					.formatted(Formatting.GOLD),
				false
			);
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 1.0F);
		});

		AtomicInteger integer = new AtomicInteger(10);
		scheduleInLoopPhaseTask(minutesToTicks(2), minutesToTicks(2), minutesToTicks(8), () -> {
			int i = integer.addAndGet(-2);
			PlayerManager.forEveryPlayer(playerEntity -> {
				playerEntity.sendMessage(
					Text.literal("Grace Period will end in %s minutes".formatted(i))
						.formatted(Formatting.GOLD),
					false
				);
				playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 1.0F);
			});
		});

		schedulePhaseTask(minutesToTicks(10), () -> {
			PlayerManager.forEveryPlayer(playerEntity -> {
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

	private static void worldBorderComplete() {
		UHCMod.LOGGER.info("World Border Completed");
		schedulePhaseTask(minutesToTicks(5), () -> {
			if (GameSettings.END_GAME_GLOW.getValue()) {
				UHCMod.LOGGER.info("Players are now glowing");
				PlayerManager.forEveryPlayer(playerEntity -> {
					if (PlayerManager.isPlayerSurvival(playerEntity)) {
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

	private static void endUHC() {
		AbstractTeam team = TeamManager.getAliveTeam();
		if (team == null) {
			UHCMod.LOGGER.error("Last team was null!");
			return;
		}

		PlayerManager.forEveryPlayer(player -> {
			if (player.getScoreboardTeam() == team || PlayerExtension.get(player).trueTeam == team) {
				PlayerManager.grantAdvancement(player, UHCAdvancements.WINNER);
			}
		});

		PlayerManager.forEveryPlayer(playerEntity -> {
			playerEntity.setGlowing(false);
			playerEntity.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("%s has won!".formatted(team.getName())).formatted(team.getColor())));
			UHCDataBase.updateStats(playerEntity);
		});

		UHCDataBase.incrementWinDataBase(team.getName());
		UHCDataBase.updateTotalDataBase();

		scheduleInLoopPhaseTask(0, 4, secondsToTicks(5), () -> {
			PlayerManager.forEveryPlayer(playerEntity -> {
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
				playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
			});
			schedulePhaseTask(6, () -> {
				PlayerManager.forEveryPlayer(playerEntity -> {
					playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.MASTER, 0.5f, 1f);
					playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
					playerEntity.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
				});
			});
		});

		schedulePhaseTask(minutesToTicks(1), () -> {
			PlayerManager.forEveryPlayer(PlayerManager::clearPlayerInventory);
			EventHandler.onSetup();
			EventHandler.onLobby();
		});
	}

	private static void generateBossBar() {
		if (bossBar == null) {
			BossBarManager manager = UHCMod.SERVER.getBossBarManager();
			Identifier id = Identifier.of("uhc", "lobby");
			bossBar = manager.get(id);
			if (bossBar == null) {
				bossBar = manager.add(id, Config.CURRENT_EVENT.getBossBarMessage());
			}
		} else {
			bossBar.setName(Config.CURRENT_EVENT.getBossBarMessage());
		}
		bossBar.setPercent(1);
		bossBar.setColor(Config.CURRENT_EVENT.getBossBarColour());
		PlayerManager.forEveryPlayer(p -> {
			bossBar.addPlayer(p);
		});
	}

	private static void hideBossBar() {
		if (bossBar != null) {
			bossBar.clearPlayers();
		}
	}

	private static void deleteAllBossBars() {
		BossBarManager manager = UHCMod.SERVER.getBossBarManager();
		CommandBossBar[] bossBars = manager.getAll().toArray(CommandBossBar[]::new);
		for (CommandBossBar bar : bossBars) {
			bar.clearPlayers();
			manager.remove(bar);
		}
	}

	private static void generateLobby() {
		try {
			deleteLobby();
			Vec3i dimensions = getLobby().getSize();
			int x = -dimensions.getX() / 2;
			int y = UHCMod.SERVER.getOverworld().getTopY() - dimensions.getY() - 10;
			int z = -dimensions.getZ() / 2;
			BlockPos pos = new BlockPos(x, y ,z);
			getLobby().place(UHCMod.SERVER.getOverworld(), pos, pos, new StructurePlacementData(), Random.create(), 3);
			PlayerManager.forEveryPlayer(playerEntity -> {
				if (!playerEntity.hasPermissionLevel(2)) {
					playerEntity.interactionManager.changeGameMode(GameMode.ADVENTURE);
				} else {
					playerEntity.sendMessage(Text.literal("Successfully generated lobby!").formatted(Formatting.GREEN), false);
				}
				Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
				playerEntity.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
			});
		} catch (Exception e) {
			UHCMod.LOGGER.error("Failed to generate lobby", e);
		}
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

