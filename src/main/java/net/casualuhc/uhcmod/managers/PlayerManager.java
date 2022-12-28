package net.casualuhc.uhcmod.managers;

import io.netty.buffer.Unpooled;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.data.PlayerFlag;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.scheduling.Scheduler;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.casualuhc.uhcmod.utils.uhc.UHCUtils;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;

import static net.casualuhc.uhcmod.utils.scheduling.Scheduler.secondsToTicks;
import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public class PlayerManager {
	private static final MutableText HEADER = literal("Casual UHC\n").formatted(Formatting.GOLD, Formatting.BOLD);
	private static final MutableText FOOTER = literal("\n").append(translatable("uhc.tab.hosted")).formatted(Formatting.AQUA, Formatting.BOLD);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.0");
	private static final Map<ServerPlayerEntity, List<Runnable>> RESOURCE_RELOADS = new HashMap<>();

	public static final UUID HEALTH_BOOST = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57");
	public static boolean displayTab = true;

	/**
	 * Iterates over all players currently on the server
	 * into a lambda for you to use.
	 *
	 * @param consumer the function to execute for each player.
	 */
	public static void forEveryPlayer(Consumer<ServerPlayerEntity> consumer) {
		UHCMod.SERVER.execute(() -> {
			for (ServerPlayerEntity playerEntity : UHCMod.SERVER.getPlayerManager().getPlayerList()) {
				consumer.accept(playerEntity);
			}
		});
	}

	/**
	 * Stores a runnable to be run when the player next reloads their resource pack.
	 *
	 * @param player the player to wait for the next reload.
	 * @param runnable the runnable to run.
	 */
	public static void waitForResourcePack(ServerPlayerEntity player, Runnable runnable) {
		RESOURCE_RELOADS.computeIfAbsent(player, p -> new LinkedList<>()).add(runnable);
	}

	/**
	 * Messages all players currently on the server.
	 *
	 * @param text the message to send to the players.
	 */
	public static void messageEveryPlayer(Text text) {
		forEveryPlayer(playerEntity -> playerEntity.sendMessage(text, false));
	}

	/**
	 * Marks a player as playing or not in the UHC.
	 *
	 * @param player the player to mark as playing.
	 * @param isPlaying whether the player is playing.
	 */
	public static void setPlayerPlaying(ServerPlayerEntity player, boolean isPlaying) {
		PlayerExtension.get(player).setFlag(PlayerFlag.IS_PLAYING, isPlaying);
	}

	/**
	 * Checks whether the player is playing in the UHC.
	 *
	 * @param player the player to check
	 * @return whether the player is playing.
	 */
	public static boolean isPlayerPlaying(ServerPlayerEntity player) {
		return PlayerExtension.get(player).getFlag(PlayerFlag.IS_PLAYING);
	}

	/**
	 * Checks whether the player is in Survival Mode.
	 *
	 * @param player the player to check.
	 * @return whether the player is in Survival.
	 */
	public static boolean isPlayerSurvival(ServerPlayerEntity player) {
		return player.interactionManager.getGameMode() == GameMode.SURVIVAL;
	}

	/**
	 * Checks whether the player is playing in the UHC and in Survival Mode.
	 *
	 * @param player the player to check.
	 * @return whether the player is playing and is in Survival.
	 */
	public static boolean isPlayerPlayingInSurvival(ServerPlayerEntity player) {
		return isPlayerPlaying(player) && isPlayerSurvival(player);
	}

	/**
	 * Grants a player a certain advancement.
	 *
	 * @param player the player to grant the advancement to.
	 * @param advancement the advancement to grant.
	 */
	public static void grantAdvancement(ServerPlayerEntity player, Advancement advancement) {
		AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
		if (!advancementProgress.isDone()) {
			for (String string : advancementProgress.getUnobtainedCriteria()) {
				player.getAdvancementTracker().grantCriterion(advancement, string);
			}
		}
	}

	/**
	 * Revokes an advancement from a player.
	 *
	 * @param player the player to revoke from.
	 * @param advancement the advancement to revoke.
	 */
	public static void revokeAdvancement(ServerPlayerEntity player, Advancement advancement) {
		AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
		if (advancementProgress.isAnyObtained()) {
			for (String string : advancementProgress.getObtainedCriteria()) {
				player.getAdvancementTracker().revokeCriterion(advancement, string);
			}
		}
	}

	/**
	 * Gets the player ready for UHC. Setting their health, clearing their inventory, etc.
	 *
	 * @param player the player to get ready.
	 */
	public static void setPlayerForCTF(ServerPlayerEntity player) {
		player.dismountVehicle();

		EntityAttributeInstance instance = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (instance != null) {
			instance.removeModifier(PlayerManager.HEALTH_BOOST);
			instance.addPersistentModifier(new EntityAttributeModifier(PlayerManager.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
		}
		player.setHealth(player.getMaxHealth());
		player.getHungerManager().setSaturationLevel(20F);
		player.setExperienceLevel(0);

		if (!TeamManager.shouldIgnoreTeam(player.getScoreboardTeam())) {
			PlayerExtension.apply(player, e -> {
				e.setFlag(PlayerFlag.GLOW_ENABLED, true);
				e.setFlag(PlayerFlag.FULL_BRIGHT_ENABLED, true);
			});
			forceUpdateGlowingFlag(player);
			updateFullBright(player, true);

			clearPlayerInventory(player);
			setPlayerPlaying(player, true);

			player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, secondsToTicks(10), 4, true, false));
			player.changeGameMode(GameMode.SURVIVAL);
			return;
		}

		player.changeGameMode(GameMode.SPECTATOR);
	}

	/**
	 * Completely wipes the player inventory.
	 * This includes their crafting slots and cursor.
	 *
	 * @param player the player clear.
	 */
	public static void clearPlayerInventory(ServerPlayerEntity player) {
		player.getInventory().clear();
		player.playerScreenHandler.clearCraftingSlots();
		player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
	}

	/**
	 * Force updates a player's glowing flag.
	 *
	 * @param player the player to update.
	 */
	public static void forceUpdateGlowingFlag(ServerPlayerEntity player) {
		// Force updates the dirty flag in the tracker entry
		player.setGlowing(!player.isGlowing());
		player.setGlowing(!player.isGlowing());
	}

	/**
	 * Update every player's glowing flag.
	 */
	public static void forceUpdateGlowing() {
		forEveryPlayer(PlayerManager::forceUpdateGlowingFlag);
	}

	/**
	 * Update every player's full bright.
	 */
	public static void forceUpdateFullBright() {
		forEveryPlayer(p -> updateFullBright(p, true));
	}

	/**
	 * Update a player's full bright.
	 *
	 * @param player the player to update.
	 * @param force whether this was force or user updated.
	 */
	public static void updateFullBright(ServerPlayerEntity player, boolean force) {
		PlayerExtension extension = PlayerExtension.get(player);
		if (extension.getFlag(PlayerFlag.FULL_BRIGHT_ENABLED)) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
		} else {
			player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		}
		if (!force) {
			Text message = translatable("uhc.fullbright").append(" ").append(
				extension.getFlag(PlayerFlag.FULL_BRIGHT_ENABLED) ? translatable("uhc.fullbright.enabled").formatted(Formatting.DARK_GREEN) : translatable("uhc.fullbright.disabled").formatted(Formatting.DARK_RED)
			);
			player.sendMessage(message);
		}
	}

	/**
	 * Sends a resource pack to a player.
	 *
	 * @param player the player to send the pack to.
	 * @param properties the properties of the pack.
	 */
	public static void sendResourcePack(ServerPlayerEntity player, MinecraftServer.ServerResourcePackProperties properties) {
		player.sendResourcePackUrl(properties.url(), properties.hash(), true, properties.prompt());
	}

	public static EntityTrackerUpdateS2CPacket handleTrackerUpdatePacketForTeamGlowing(ServerPlayerEntity glowingPlayer, ServerPlayerEntity observingPlayer, EntityTrackerUpdateS2CPacket packet) {
		if (!GameSettings.FRIENDLY_PLAYER_GLOW.getValue()) {
			return packet;
		}
		if (!GameManager.isPhase(Phase.ACTIVE)) {
			return packet;
		}
		if (!PlayerManager.isPlayerSurvival(glowingPlayer)) {
			return packet;
		}
		if (!PlayerExtension.get(observingPlayer).getFlag(PlayerFlag.GLOW_ENABLED)) {
			return packet;
		}

		AbstractTeam glowingTeam = glowingPlayer.getScoreboardTeam();
		AbstractTeam observingTeam = observingPlayer.getScoreboardTeam();
		if (glowingTeam != observingTeam) {
			return packet;
		}

		List<DataTracker.SerializedEntry<?>> trackedValues = packet.trackedValues();
		if (trackedValues == null) {
			return packet;
		}
		if (trackedValues.stream().noneMatch(value -> value.id() == Entity.FLAGS.getId())) {
			return packet;
		}

		// Make a copy of the packet, because other players are sent the same instance of
		// The packet and may not be on the same team
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		packet.write(buf);
		packet = new EntityTrackerUpdateS2CPacket(buf);

		ListIterator<DataTracker.SerializedEntry<?>> iterator = packet.trackedValues().listIterator();
		while (iterator.hasNext()) {
			DataTracker.SerializedEntry<?> trackedValue = iterator.next();
			// Need to compare ids because they're not the same instance once re-serialized
			if (trackedValue.id() == Entity.FLAGS.getId()) {
				@SuppressWarnings("unchecked")
				DataTracker.SerializedEntry<Byte> byteTrackedValue = (DataTracker.SerializedEntry<Byte>) trackedValue;
				byte flags = byteTrackedValue.value();
				flags |= 1 << Entity.GLOWING_FLAG_INDEX;
				iterator.set(DataTracker.SerializedEntry.of(Entity.FLAGS, flags));
			}
		}

		return packet;
	}

	public static void noop() { }

	static {
		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onPlayerJoin(ServerPlayerEntity player) {
				handlePlayerJoin(player);
			}

			@Override
			public void onPlayerTick(ServerPlayerEntity player) {
				updateHUD(player);
			}

			@Override
			public void onResourcePackLoaded(ServerPlayerEntity player) {
				List<Runnable> tasks = RESOURCE_RELOADS.get(player);
				if (tasks != null) {
					for (Runnable task : tasks) {
						task.run();
					}
					tasks.clear();
				}
			}

			@Override
			public void onPlayerLeave(ServerPlayerEntity player) {
				RESOURCE_RELOADS.remove(player);
			}
		});

		EventHandler.register(new UHCEvents() {
			@Override
			public void onSetup() {
				PlayerExtension.reset();
				UHCMod.SERVER.getAdvancementLoader().getAdvancements().forEach(a -> forEveryPlayer(p -> revokeAdvancement(p, a)));
				forEveryPlayer(p -> grantAdvancement(p, UHCAdvancements.ROOT));
			}

			@Override
			public void onLobby() {
				forEveryPlayer(player -> {
					Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
					player.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 90, 0);
				});
			}

			@Override
			public void onActive() {
				forceUpdateGlowing();
				forceUpdateFullBright();
			}

			@Override
			public void onEnd() {
				forceUpdateGlowing();
			}
		});
	}

	private static void handlePlayerJoin(ServerPlayerEntity player) {
		waitForResourcePack(player, () -> {
			player.sendMessage(Text.translatable("uhc.lobby.welcome").append(" Capture The Flag").formatted(Formatting.GOLD), false);
		});

		Scoreboard scoreboard = UHCMod.SERVER.getScoreboard();
		if (!GameManager.isGameActive()) {
			if (!player.hasPermissionLevel(2)) {
				player.changeGameMode(GameMode.ADVENTURE);
				Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
				player.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 90, 0);
			} else {
				if (Config.IS_DEV) {
					player.sendMessage(Text.literal("[INFO] UHC is in dev mode!").formatted(Formatting.RED));
				}
				player.changeGameMode(GameMode.CREATIVE);
				AbstractTeam team = player.getScoreboardTeam();
				if (team == null) {
					Team operator = scoreboard.getTeam("Operator");
					if (operator != null) {
						scoreboard.addPlayerToTeam(player.getEntityName(), operator);
					}
				}
			}
		} else if (!PlayerManager.isPlayerPlaying(player)) {
			Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
			player.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 90, 0);
		}
		if (player.getScoreboardTeam() == null) {
			Team spectator = scoreboard.getTeam("Spectator");
			if (spectator != null) {
				scoreboard.addPlayerToTeam(player.getEntityName(), spectator);
			}
		}

		// idk...
		Scheduler.schedule(20, player::markHealthDirty);
	}

	private static void updateHUD(ServerPlayerEntity playerEntity) {
		if (playerEntity.getWorld().getTime() % 20 == 0 && displayTab) {
			float ticksPerSecond = 1000 / Math.max(50, UHCUtils.calculateMSPT());
			Formatting formatting = ticksPerSecond == 20 ? Formatting.DARK_GREEN : ticksPerSecond > 15 ? Formatting.YELLOW : ticksPerSecond > 10 ? Formatting.RED : Formatting.DARK_RED;
			playerEntity.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(
				HEADER,
				literal("\nServer TPS: ").append(literal(DECIMAL_FORMAT.format(ticksPerSecond)).formatted(formatting)).append(FOOTER)
			));
		}
	}
}
