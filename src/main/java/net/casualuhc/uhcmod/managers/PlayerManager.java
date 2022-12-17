package net.casualuhc.uhcmod.managers;

import io.netty.buffer.Unpooled;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.scheduling.Scheduler;
import net.casualuhc.uhcmod.utils.uhc.*;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.text.DecimalFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

import static net.casualuhc.uhcmod.utils.scheduling.Scheduler.secondsToTicks;
import static net.minecraft.text.Text.literal;
import static net.minecraft.text.Text.translatable;

public class PlayerManager {
	private static final MutableText HEADER = literal("Casual UHC\n").formatted(Formatting.GOLD, Formatting.BOLD);
	private static final MutableText FOOTER = literal("\n").append(translatable("uhc.tab.hosted")).formatted(Formatting.AQUA, Formatting.BOLD);
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.0");

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
		PlayerExtension.get(player).isPlaying = isPlaying;
	}

	/**
	 * Checks whether the player is playing in the UHC.
	 *
	 * @param player the player to check
	 * @return whether the player is playing.
	 */
	public static boolean isPlayerPlaying(ServerPlayerEntity player) {
		return PlayerExtension.get(player).isPlaying;
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
	 * Drops the player's head, or inserts it into another player's inventory, if possible.
	 *
	 * @param player the player to drop their head.
	 * @param entity the possible player to give the head item to.
	 */
	public static void dropPlayerHead(ServerPlayerEntity player, Entity entity) {
		ItemStack playerHead = ItemUtils.generatePlayerHead(player.getEntityName());
		if (entity instanceof ServerPlayerEntity attacker) {
			if (!attacker.getInventory().insertStack(playerHead)) {
				player.dropItem(playerHead, true, false);
			}
			return;
		}
		player.dropItem(playerHead, true, false);
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
	 * Play the lobby music in a loop for a certain player.
	 * The player must have the resource pack.
	 *
	 * @param player the player to play the music to.
	 */
	public static void playLobbyMusic(ServerPlayerEntity player, int tickInterval) {
		if (!GameManager.isGameActive()) {
			player.playSound(SoundEvents.MUSIC_DISC_5, SoundCategory.RECORDS, 1.0f, 1.0f);
			Scheduler.schedule(tickInterval, () -> {
				playLobbyMusic(player, tickInterval);
			});
		}
	}

	/**
	 * Stop the lobby music for all players.
	 */
	public static void stopLobbyMusic() {
		resendResourcePack();
	}

	/**
	 * Gets the player ready for UHC. Setting their health, clearing their inventory, etc.
	 *
	 * @param player the player to get ready.
	 */
	public static void setPlayerForUHC(ServerPlayerEntity player) {
		player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("uhc.lobby.goodLuck").formatted(Formatting.GOLD, Formatting.BOLD)));
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.MASTER, 1.0F, 1.0F);

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
			if (player.getScoreboardTeam().getPlayerList().size() == 1) {
				PlayerManager.grantAdvancement(player, UHCAdvancements.SOLOIST);
			}

			PlayerExtension.get(player).displayCoords = true;

			clearPlayerInventory(player);
			setPlayerPlaying(player, true);

			player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, secondsToTicks(5), 4, true, false));
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
		if (extension.fullbright) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
		} else {
			player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		}
		if (!force) {
			Text message = translatable("uhc.tips.fullbright").append(" ").append(
				extension.fullbright ? translatable("uhc.tips.enabled").formatted(Formatting.DARK_GREEN) : translatable("uhc.tips.disabled").formatted(Formatting.DARK_RED)
			);
			player.sendMessage(message);
		}
	}

	/**
	 * Resends resource pack to all players.
	 */
	public static void resendResourcePack() {
		MinecraftServer.ServerResourcePackProperties properties = Config.CURRENT_EVENT.getResourcePack();
		if (properties != null) {
			forEveryPlayer(player -> {
				sendResourcePack(player, properties);
			});
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
		if (!PlayerExtension.get(observingPlayer).shouldGlow) {
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
				updateWorldBorderArrow(player);
			}

			@Override
			public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
				handlePlayerDeath(player, source);
			}

			@Override
			public void onResourcePackLoaded(ServerPlayerEntity player) {
				player.sendMessage(Text.translatable("uhc.lobby.welcome").append(" Casual UHC").formatted(Formatting.GOLD), false);
			}
		});

		EventHandler.register(new UHCEvents() {
			@Override
			public void onLobby() {
				PlayerExtension.reset();
			}

			@Override
			public void onSetup() {
				UHCMod.SERVER.getAdvancementLoader().getAdvancements().forEach(a -> forEveryPlayer(p -> revokeAdvancement(p, a)));
				forEveryPlayer(p -> grantAdvancement(p, UHCAdvancements.ROOT));
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
		if (GameSettings.TESTING.getValue()) {
			player.changeGameMode(GameMode.SURVIVAL);
			player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Welcome to UHC Test!").formatted(Formatting.GOLD)));
			player.sendMessage(Text.literal("Use /kit to get some items"));
			player.sendMessage(Text.literal("You may also use /kill"));
			Scheduler.schedule(20, player::markHealthDirty);
			return;
		}

		Scoreboard scoreboard = UHCMod.SERVER.getScoreboard();
		if (!GameManager.isGameActive()) {
			if (!player.hasPermissionLevel(2)) {
				player.changeGameMode(GameMode.ADVENTURE);
				Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
				player.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
			} else {
				player.changeGameMode(GameMode.CREATIVE);
				AbstractTeam team = player.getScoreboardTeam();
				if (team == null) {
					Team operator = scoreboard.getTeam("Operator");
					if (operator != null) {
						scoreboard.addPlayerToTeam(player.getEntityName(), operator);
					}
				}
			}
		} else if (player.getScoreboardTeam() == null || !PlayerManager.isPlayerPlaying(player)){
			player.changeGameMode(GameMode.SPECTATOR);
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

		PlayerExtension extension = PlayerExtension.get(playerEntity);
		World entityWorld = playerEntity.getEntityWorld();
		WorldBorder border = entityWorld.getWorldBorder();

		// Update location on action bar
		if (extension.displayCoords) {
			MutableText coords = literal("(" + playerEntity.getBlockX() + ", " + playerEntity.getBlockY() + ", " + playerEntity.getBlockZ() + ")");
			Text direction = translatable("uhc.game.direction").append(": " + playerEntity.getHorizontalFacing().asString().toUpperCase(Locale.ROOT));
			int borderRadius = ((int) border.getSize() / 2);
			Text radius = translatable("uhc.game.radius").append(": " + borderRadius);
			int distanceToBorder = ((int) border.getDistanceInsideBorder(playerEntity));
			double distancePercent = distanceToBorder / (double) borderRadius;
			Formatting formatting = distancePercent > 0.4 ? Formatting.DARK_GREEN : distancePercent > 0.2 ? Formatting.YELLOW : distancePercent > 0.1 ? Formatting.RED : Formatting.DARK_RED;
			Text distanceToWb = translatable("uhc.game.distance").append(": ").append(literal(String.valueOf(distanceToBorder)).formatted(formatting));

			playerEntity.sendMessage(coords.append(" | ").append(direction).append(" | ").append(distanceToWb).append(" | ").append(radius), true);
		}
	}

	private static void handlePlayerDeath(ServerPlayerEntity player, DamageSource source) {
		sendWorldBorderPackets(player, player.world.getWorldBorder());

		if (GameManager.isPhase(Phase.END)) {
			player.interactionManager.changeGameMode(GameMode.SPECTATOR);
		}

		if (GameManager.isPhase(Phase.ACTIVE)) {
			player.setSpawnPoint(player.world.getRegistryKey(), player.getBlockPos(), player.getSpawnAngle(), true, false);
			player.interactionManager.changeGameMode(GameMode.SPECTATOR);

			UHCDataBase.updateStats(player);
			forceUpdateGlowingFlag(player);

			if (GameManager.isUnclaimed(OneTimeAchievement.DEATH)) {
				grantAdvancement(player, UHCAdvancements.EARLY_EXIT);
			}

			if (GameSettings.PLAYER_DROPS_GAPPLE_ON_DEATH.getValue()) {
				player.dropItem(Items.GOLDEN_APPLE.getDefaultStack(), true, false);
			}

			if (GameSettings.PLAYER_DROPS_HEAD_ON_DEATH.getValue()) {
				dropPlayerHead(player, source.getAttacker());
			}

			if (source.getAttacker() instanceof ServerPlayerEntity attacker && GameManager.isUnclaimed(OneTimeAchievement.KILL)) {
				grantAdvancement(attacker, UHCAdvancements.FIRST_BLOOD);
			}

			AbstractTeam team = player.getScoreboardTeam();
			PlayerExtension.get(player).trueTeam = team;
			ServerScoreboard scoreboard = player.getWorld().getServer().getScoreboard();
			scoreboard.addPlayerToTeam(player.getEntityName(), scoreboard.getTeam("Spectator"));
			if (team != null && !TeamManager.teamHasAlive(team) && !TeamManager.isEliminated(team)) {
				TeamManager.setEliminated(team, true);
				PlayerManager.forEveryPlayer(playerEntity -> {
					playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 0.5f, 1);
					playerEntity.sendMessage(
						translatable("uhc.game.eliminated", team.getName()).formatted(team.getColor(), Formatting.BOLD), false
					);
				});
			}
			if (TeamManager.isLastTeam()) {
				EventHandler.onEnd();
			}
		}
	}

	// By Kman
	private static void updateWorldBorderArrow(ServerPlayerEntity playerEntity) {
		PlayerExtension playerExtension = PlayerExtension.get(playerEntity);
		World entityWorld = playerEntity.getEntityWorld();
		WorldBorder border = entityWorld.getWorldBorder();

		double playerX = playerEntity.getX();
		double playerZ = playerEntity.getZ();

		int playerBlockX = playerEntity.getBlockX();
		int playerBlockZ = playerEntity.getBlockZ();

		if (playerEntity.isSpectator()) {
			return;
		}

		// Checking to see if the players is behind the world boarder
		if (border.getDistanceInsideBorder(playerEntity) + 0.2 < 0) {
			int bottomBlockY = 0;
			// Trying to get the first non-air block to put the arrow on to make it look nicer if they are jumping
			for (int y = playerEntity.getBlockY(); y > 0; y--) {
				if (!entityWorld.getBlockState(new BlockPos(playerBlockX, y, playerBlockZ)).equals(Blocks.AIR.getDefaultState())) {
					bottomBlockY = y + 1;
					break;
				}
			}
			Direction direction;
			// Checking to see the players is closer the wb on the x than the z
			if (playerX * playerX >= playerZ * playerZ) {
				// Checking to see if the player is closer to the west world border -- handing particle stuff (arrow)
				if (playerX - border.getBoundWest() > border.getBoundEast() - playerX) {
					playerExtension.worldBorder.setCenter(border.getSize(), border.getCenterZ());
					direction = Direction.WEST;

					for (int x = 0; x < 15; x++) {
						sendParticles(playerEntity, playerBlockX + 0.2 + (float) x / 45, bottomBlockY, playerBlockZ + 0.5 + (float) x / 30);
						sendParticles(playerEntity, playerBlockX + 0.2 + (float) x / 45, bottomBlockY, playerBlockZ + 0.5 - (float) x / 30);
						sendParticles(playerEntity, playerBlockX + 0.2 + (float) x / 20, bottomBlockY, playerBlockZ + 0.5);
					}
					// Checking to see if the player is closer to the east world border -- handing particle stuff (arrow)
				} else {
					playerExtension.worldBorder.setCenter(-1 * border.getSize(), border.getCenterZ());
					direction = Direction.EAST;
					for (int x = 0; x < 15; x++) {
						sendParticles(playerEntity, playerBlockX + 0.8 - (float) x / 45, bottomBlockY, playerBlockZ + 0.5 + (float) x / 30);
						sendParticles(playerEntity, playerBlockX + 0.8 - (float) x / 45, bottomBlockY, playerBlockZ + 0.5 - (float) x / 30);
						sendParticles(playerEntity, playerBlockX + 0.8 - (float) x / 20, bottomBlockY, playerBlockZ + 0.5);
					}
				}
			} else {
				// Checking to see if the player is closer to the north world border -- handing particle stuff (arrow)
				if (playerZ - border.getBoundNorth() > border.getBoundSouth() - playerZ) {
					playerExtension.worldBorder.setCenter(border.getCenterX(), border.getSize());
					direction = Direction.NORTH;

					for (int x = 0; x < 15; x++) {
						sendParticles(playerEntity, playerBlockX + 0.5 + (float) x / 30, bottomBlockY, playerBlockZ + 0.2 + (float) x / 45);
						sendParticles(playerEntity, playerBlockX + 0.5 - (float) x / 30, bottomBlockY, playerBlockZ + 0.2 + (float) x / 45);
						sendParticles(playerEntity, playerBlockX + 0.5, bottomBlockY, playerBlockZ + 0.2 + (float) x / 20);
					}
					// Checking to see if the player is closer to the south world border -- handing particle stuff (arrow)
				} else {
					playerExtension.worldBorder.setCenter(border.getCenterX(), -1 * border.getSize());
					direction = Direction.SOUTH;
					for (int x = 0; x < 15; x++) {
						sendParticles(playerEntity, playerBlockX + 0.5 + (float) x / 30, bottomBlockY, playerBlockZ + 0.8 - (float) x / 45);
						sendParticles(playerEntity, playerBlockX + 0.5 - (float) x / 30, bottomBlockY, playerBlockZ + 0.8 - (float) x / 45);
						sendParticles(playerEntity, playerBlockX + 0.5, bottomBlockY, playerBlockZ + 0.8 - (float) x / 20);
					}
				}
			}
			// Sending fake world border packets to the player
			playerExtension.worldBorder.setSize(border.getSize() + 1);
			sendWorldBorderPackets(playerEntity, playerExtension.worldBorder);

			// The big title and subtitle msg packets -- sent every 100 game ticks
			if (UHCMod.SERVER.getTicks() % 100 == 0) {
				MutableText title = translatable("uhc.game.outsideBorder").formatted(Formatting.RED);
				MutableText subTitle = translatable("uhc.game.outsideBorder.sub", direction.name());
				playerEntity.networkHandler.sendPacket(new TitleS2CPacket(title));
				playerEntity.networkHandler.sendPacket(new SubtitleS2CPacket(subTitle));
			}

			playerExtension.wasInWorldBorder = true;
		} else if (playerExtension.wasInWorldBorder) {
			playerExtension.wasInWorldBorder = false;
			sendWorldBorderPackets(playerEntity, border);
		}
	}

	private static void sendParticles(ServerPlayerEntity playerEntity, double x, int y, double z) {
		playerEntity.networkHandler.sendPacket(new ParticleS2CPacket(
			ParticleTypes.END_ROD, false, x + 0, y + 0.001, z + 0, 0.0f, 0.0f, 0.0f, 0.0f, 1
		));
	}

	private static void sendWorldBorderPackets(ServerPlayerEntity playerEntity, WorldBorder border) {
		playerEntity.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(border));
		playerEntity.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(border));
		playerEntity.networkHandler.sendPacket(new WorldBorderWarningTimeChangedS2CPacket(border));
		playerEntity.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(border));
		playerEntity.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(border));
		playerEntity.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(border));
	}
}
