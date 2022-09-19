package net.casualuhc.uhcmod.utils;

import io.netty.buffer.Unpooled;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerUtils {
	private static final MutableText HEADER = Text.literal("Casual UHC\n").formatted(Formatting.GOLD, Formatting.BOLD);
	private static final MutableText FOOTER = Text.literal("\nServer Hosted By KiwiTech").formatted(Formatting.AQUA, Formatting.BOLD);
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.0");
	private static final ItemStack GOLDEN_HEAD = Util.make(generatePlayerHead("PhantomTupac"), i -> i.setCustomName(Text.literal("Golden Head").formatted(Formatting.GOLD).styled(s -> s.withItalic(false))));

	private static final Map<String, Boolean> isPlayerPlayingMap = new HashMap<>();

	public static final UUID HEALTH_BOOST = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57");
	public static boolean displayTab = true;

	static {
		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onPlayerTick(ServerPlayerEntity player) {
				updateInfo(player);
				updateWorldBorderArrow(player);
			}

			@Override
			public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
				PlayerUtils.handlePlayerDeath(player, source);
			}
		});
		EventHandler.register(new UHCEvents() {
			@Override
			public void onStart() {
				UHCMod.SERVER.getAdvancementLoader().getAdvancements().forEach(a -> forEveryPlayer(p -> revokeAdvancement(p, a)));
				forEveryPlayer(p -> grantAdvancement(p, UHCAdvancements.ROOT));
			}
		});
	}

	public static void forceUpdateGlowing() {
		forEveryPlayer(PlayerUtils::forceUpdateGlowingFlag);
	}

	private static void handlePlayerDeath(ServerPlayerEntity player, DamageSource source) {
		if (GameManager.isPhase(Phase.ACTIVE)) {
			if (GameManager.tryFirstDeath()) {
				grantAdvancement(player, UHCAdvancements.EARLY_EXIT);
			}
			UHCDataBase.updateStats(player);
			if (GameSettings.PLAYER_DROPS_GAPPLE_ON_DEATH.getValue()) {
				player.dropItem(Items.GOLDEN_APPLE.getDefaultStack(), true, false);
			}
			if (GameSettings.PLAYER_DROPS_HEAD_ON_DEATH.getValue()) {
				ItemStack playerHead = generatePlayerHead(player.getEntityName());
				if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
					if (GameManager.tryFirstKill()) {
						grantAdvancement(attacker, UHCAdvancements.FIRST_BLOOD);
					}
					if (!attacker.getInventory().insertStack(playerHead)) {
						player.dropItem(playerHead, true, false);
					}
				} else {
					player.dropItem(playerHead, true, false);
				}
			}
			AbstractTeam team = player.getScoreboardTeam();
			player.interactionManager.changeGameMode(GameMode.SPECTATOR);
			forceUpdateGlowingFlag(player);
			if (team != null && !TeamUtils.teamHasAlive(team) && !TeamUtils.isEliminated(team)) {
				TeamUtils.setEliminated(team, true);
				PlayerUtils.forEveryPlayer(playerEntity -> {
					playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 0.5f, 1);
					playerEntity.sendMessage(
						Text.literal("%s has been ELIMINATED!".formatted(team.getName())).formatted(team.getColor(), Formatting.BOLD), false
					);
				});
			}
			if (TeamUtils.isLastTeam()) {
				EventHandler.onEnd();
			}
		}
		if (GameManager.isPhase(Phase.END)) {
			player.interactionManager.changeGameMode(GameMode.SPECTATOR);
		}
		sendWorldBorderPackets(player, player.world.getWorldBorder());
	}

	public static ItemStack generateGoldenHead() {
		return GOLDEN_HEAD.copy();
	}

	public static ItemStack generatePlayerHead(String playerName) {
		NbtCompound compound = new NbtCompound();
		compound.putString("id", "player_head");
		compound.putByte("Count", (byte) 1);

		NbtCompound playerData = new NbtCompound();
		playerData.putString(SkullItem.SKULL_OWNER_KEY, playerName);

		compound.put("tag", playerData);

		return ItemStack.fromNbt(compound);
	}

	public static void grantAdvancement(ServerPlayerEntity player, Advancement advancement) {
		AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
		if (!advancementProgress.isDone()) {
			for (String string : advancementProgress.getUnobtainedCriteria()) {
				player.getAdvancementTracker().grantCriterion(advancement, string);
			}
		}
	}

	public static void revokeAdvancement(ServerPlayerEntity player, Advancement advancement) {
		AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
		if (advancementProgress.isAnyObtained()) {
			for (String string : advancementProgress.getObtainedCriteria()) {
				player.getAdvancementTracker().revokeCriterion(advancement, string);
			}
		}
	}

	private static void updateInfo(ServerPlayerEntity playerEntity) {
		if (playerEntity.getWorld().getTime() % 20 == 0 && displayTab) {
			float ticksPerSecond = 1000 / Math.max(50, UHCUtil.calculateMSPT());
			Formatting formatting = ticksPerSecond == 20 ? Formatting.DARK_GREEN : ticksPerSecond > 15 ? Formatting.YELLOW : ticksPerSecond > 10 ? Formatting.RED : Formatting.DARK_RED;
			playerEntity.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(
				HEADER,
				Text.literal("\nServer TPS: ").append(Text.literal(decimalFormat.format(ticksPerSecond)).formatted(formatting)).append(FOOTER)
			));
		}

		PlayerExtension extension = PlayerExtension.get(playerEntity);
		World entityWorld = playerEntity.getEntityWorld();
		WorldBorder border = entityWorld.getWorldBorder();

		// Update location on action bar
		if (extension.displayCoords) {
			String locationInfo = "(%d, %d, %d) | Direction: %s | Distance to WB: %d | WB radius: %d".formatted(
				playerEntity.getBlockX(),
				playerEntity.getBlockY(),
				playerEntity.getBlockZ(),
				playerEntity.getHorizontalFacing(),
				((int) border.getDistanceInsideBorder(playerEntity)),
				((int) border.getSize() / 2)
			);

			playerEntity.sendMessage(Text.literal(locationInfo), true);
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
				MutableText title = Text.literal("You're outside the border!").formatted(Formatting.RED);
				MutableText subTitle = Text.literal("§cTravel §a" + direction + " §cto get to safety!");
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
			ParticleTypes.END_ROD,
			false,
			x + 0,
			y + 0.001,
			z + 0,
			0.0f,
			0.0f,
			0.0f,
			0.0f,
			1
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

	public static void forEveryPlayer(Consumer<ServerPlayerEntity> consumer) {
		UHCMod.SERVER.execute(() -> {
			for (ServerPlayerEntity playerEntity : UHCMod.SERVER.getPlayerManager().getPlayerList()) {
				consumer.accept(playerEntity);
			}
		});
	}

	public static void messageEveryPlayer(Text text) {
		forEveryPlayer(playerEntity -> playerEntity.sendMessage(text, false));
	}

	public static void setPlayerPlaying(ServerPlayerEntity player, boolean isPlaying) {
		isPlayerPlayingMap.put(player.getUuidAsString(), isPlaying);
	}

	public static boolean isPlayerPlaying(ServerPlayerEntity player) {
		Boolean isPlaying = isPlayerPlayingMap.get(player.getUuidAsString());
		return isPlaying != null && isPlaying;
	}

	public static boolean isPlayerSurvival(ServerPlayerEntity player) {
		return player.interactionManager.getGameMode() == GameMode.SURVIVAL;
	}

	public static boolean isPlayerPlayingInSurvival(ServerPlayerEntity player) {
		return isPlayerPlaying(player) && isPlayerSurvival(player);
	}

	public static void forceUpdateGlowingFlag(ServerPlayerEntity player) {
		// Force updates the dirty flag in the tracker entry
		player.setGlowing(!player.isGlowing());
		player.setGlowing(!player.isGlowing());
	}

	public static EntityTrackerUpdateS2CPacket handleTrackerUpdatePacketForTeamGlowing(ServerPlayerEntity glowingPlayer, ServerPlayerEntity observingPlayer, EntityTrackerUpdateS2CPacket packet) {
		if (!GameSettings.FRIENDLY_PLAYER_GLOW.getValue()) {
			return packet;
		}
		if (!GameManager.isPhase(Phase.ACTIVE)) {
			return packet;
		}
		if (!PlayerUtils.isPlayerSurvival(glowingPlayer)) {
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

		List<DataTracker.Entry<?>> trackedValues = packet.getTrackedValues();
		if (trackedValues == null) {
			return packet;
		}
		if (trackedValues.stream().noneMatch(value -> value.getData() == Entity.FLAGS)) {
			return packet;
		}

		// Make a copy of the packet, because other players are sent the same instance of
		// The packet and may not be on the same team
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		packet.write(buf);
		packet = new EntityTrackerUpdateS2CPacket(buf);
		trackedValues = packet.getTrackedValues();

		if (trackedValues == null) {
			return null;
		}

		for (DataTracker.Entry<?> trackedValue : trackedValues) {
			// Need to compare ids because they're not the same instance once re-serialized
			if (trackedValue.getData().getId() == Entity.FLAGS.getId()) {
				@SuppressWarnings("unchecked")
				DataTracker.Entry<Byte> byteTrackedValue = (DataTracker.Entry<Byte>) trackedValue;
				byte flags = byteTrackedValue.get();
				flags |= 1 << Entity.GLOWING_FLAG_INDEX;
				byteTrackedValue.set(flags);
			}
		}

		return packet;
	}
}
