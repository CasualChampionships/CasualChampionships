package net.casualuhc.uhcmod.managers;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.api.settings.InvalidRuleValueException;
import carpet.api.settings.SettingsManager;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.GoldenHeadRecipe;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.data.PlayerFlag;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.scheduling.Task;
import net.casualuhc.uhcmod.utils.uhc.*;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static net.casualuhc.uhcmod.utils.scheduling.Scheduler.*;

public class GameManager {
	private static final List<Task> currentTasks = new LinkedList<>();
	private static final Set<OneTimeAchievement> claimed = new HashSet<>();

	public static Team teamA;
	public static Team teamB;

	private static Phase phase = Phase.NONE;
	private static StructureTemplate arena;
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

	public static void startCTF(Team first, Team second) {
		if (!isPhase(Phase.ACTIVE)) {
			EventHandler.onActive();
		}

		teamA = first;
		teamB = second;

		regenerateArena();

		PlayerExtension.forEach(p -> p.setFlag(PlayerFlag.IS_PLAYING, false));
		for (Team team : UHCMod.SERVER.getScoreboard().getTeams()) {
			if (team != first && team != second) {
				team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
				team.setFriendlyFireAllowed(false);
			}
		}

		net.minecraft.server.PlayerManager manager = UHCMod.SERVER.getPlayerManager();
		ServerWorld overworld = UHCMod.SERVER.getOverworld();

		first.setFriendlyFireAllowed(true);
		second.setFriendlyFireAllowed(true);

		for (String playerName : first.getPlayerList()) {
			ServerPlayerEntity player = manager.getPlayer(playerName);
			if (player != null) {
				PlayerManager.setPlayerForCTF(player);
				player.setSpawnPoint(World.OVERWORLD, new BlockPos(1037, 53, -6584), 0, true, false);
				player.teleport(overworld, 1098, 53, -6522, 90F, 0F);
			}
		}
		for (String playerName : second.getPlayerList()) {
			ServerPlayerEntity player = manager.getPlayer(playerName);
			if (player != null) {
				PlayerManager.setPlayerForCTF(player);
				player.setSpawnPoint(World.OVERWORLD, new BlockPos(1037, 53, -6450), 180, true, false);
				player.teleport(overworld, 1098, 53, -6512, 90F, 0F);
			}
		}
	}

	public static void resetCTF() {
		PlayerManager.forEveryPlayer(player -> {
			if (PlayerManager.isPlayerPlaying(player)) {
				PlayerManager.clearPlayerInventory(player);
				player.kill();
			}
		});

		ServerWorld overworld = UHCMod.SERVER.getOverworld();

		for (ItemEntity item : overworld.getEntitiesByType(EntityType.ITEM, i -> true)) {
			item.kill();
		}

		// Destroy placed banners
		BlockPos pos = new BlockPos(1037, 53, -6442);
		if (overworld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL)) {
			overworld.getBlockState(pos.down()).scheduledTick(overworld, pos.down(), null);
		}

		pos = new BlockPos(1037, 53, -6594);
		if (overworld.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL)) {
			overworld.getBlockState(pos.down()).scheduledTick(overworld, pos.down(), null);
		}

		// Place back banners
		BlockState blue = Blocks.BLUE_BANNER.getDefaultState().with(BannerBlock.ROTATION, 0);
		pos = new BlockPos(1037, 65, -6578);
		if (overworld.setBlockState(pos, blue, Block.NOTIFY_ALL)) {
			overworld.getBlockState(pos.down()).scheduledTick(overworld, pos.down(), null);
		}

		BlockState red = Blocks.RED_BANNER.getDefaultState().with(BannerBlock.ROTATION, 8);
		pos = new BlockPos(1037, 65, -6458);
		if (overworld.setBlockState(pos, red, Block.NOTIFY_ALL)) {
			overworld.getBlockState(pos.down()).scheduledTick(overworld, pos.down(), null);
		}
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
			public void onServerTick(MinecraftServer server) {
				ticks++;
				if (isPhase(Phase.LOBBY) && bossBar != null && ticks % 100 == 0 && startTime - 30 * 60 * 1000 < System.currentTimeMillis()) {
					long millisLeft = startTime - System.currentTimeMillis();
					float percentLeft = millisLeft / (float) (30 * 60 * 1000);
					bossBar.setPercent(MathHelper.clamp(1 - percentLeft, 0, 1));
					long minutesLeft = millisLeft / (60 * 1000);
					MutableText startTime = minutesLeft <= 0 ? Text.translatable("uhc.lobby.starting.soon") :
						minutesLeft == 1 ? Text.translatable("uhc.lobby.starting.one") : Text.translatable("uhc.lobby.starting.generic", minutesLeft);
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
				generateBossBar();
				regenerateArena();
			}

			@Override
			public void onActive() {
				setPhase(Phase.ACTIVE);
				setStartTime(Long.MAX_VALUE);
				hideBossBar();
				deleteAllBossBars();
				resetTrackers();
				UHCUtils.setCTFGamerules();
			}

			@Override
			public void onEnd() {
				setPhase(Phase.END);
			}
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

	private static void regenerateArena() {
		try {
			ServerWorld world = UHCMod.SERVER.getOverworld();
			for (Entity entity : world.iterateEntities()) {
				if (entity != null && entity.getScoreboardTags().contains("uhc")) {
					entity.kill();
				}
			}
			BlockPos pos = new BlockPos(942, 36, -6672);

			getArena().place(world, pos, pos, new StructurePlacementData().setUpdateNeighbors(false), Random.create(), Block.NOTIFY_LISTENERS);
		} catch (Exception e) {
			UHCMod.LOGGER.error("Failed to generate lobby");
		}
	}

	private static StructureTemplate getArena() {
		if (arena == null) {
			try {
				InputStream inputStream = Files.newInputStream(Config.getConfig("lobbies").resolve("capture_the_flag.nbt"));
				NbtCompound nbtStructure = NbtIo.readCompressed(inputStream);
				arena = UHCMod.SERVER.getStructureTemplateManager().createTemplate(nbtStructure);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to load lobby structure");
			}
		}
		return arena;
	}
}

