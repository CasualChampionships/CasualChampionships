package net.casualuhc.uhcmod.utils;

import io.netty.buffer.Unpooled;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Event.Events;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
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
	private static final MutableText HEADER = new LiteralText("Casual UHC\n").formatted(Formatting.GOLD, Formatting.BOLD);
	private static final MutableText FOOTER = new LiteralText("\nServer Hosted By KiwiTech").formatted(Formatting.AQUA, Formatting.BOLD);
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.0");

	private static final Map<String, Boolean> isPlayerPlayingMap = new HashMap<>();

	public static final UUID HEALTH_BOOST = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57");
	public static boolean displayTab = true;

	static {
		Events.ON_PLAYER_DEATH.addListener(PlayerUtils::handlePlayerDeath);

		Events.ON_PLAYER_TICK.addListener(player -> {
			updateActionBar(player);
			updateWorldBorderArrow(player);
		});
	}

	public static void forceUpdateGlowing() {
		forEveryPlayer(PlayerUtils::forceUpdateGlowingFlag);
	}

	private static void handlePlayerDeath(ServerPlayerEntity player) {
		if (GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
			UHCDataBase.INSTANCE.updateStats(player);
			player.dropItem(new ItemStack(Items.GOLDEN_APPLE), true, false);
			AbstractTeam team = player.getScoreboardTeam();
			player.interactionManager.changeGameMode(GameMode.SPECTATOR);
			forceUpdateGlowingFlag(player);
			AbstractTeamMixinInterface iTeam = (AbstractTeamMixinInterface) team;
			if (team != null && !TeamUtils.teamHasAlive(team) && !iTeam.isEliminated()) {
				iTeam.setEliminated(true);
				PlayerUtils.forEveryPlayer(playerEntity -> {
					playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 0.5f, 1);
					playerEntity.sendMessage(
						new LiteralText("%s has been ELIMINATED!".formatted(team.getName())).formatted(team.getColor(), Formatting.BOLD), false
					);
				});
			}
			if (TeamUtils.isLastTeam()) {
				Events.ON_END.trigger();
			}
		}
		if (GameManager.INSTANCE.isPhase(Phase.END)) {
			player.interactionManager.changeGameMode(GameMode.SPECTATOR);
		}
	}

	private static void updateActionBar(ServerPlayerEntity playerEntity) {
		if (playerEntity.getWorld().getTime() % 20 == 0 && displayTab) {
			float ticksPerSecond = 1000 / Math.max(50, UHCMod.calculateMSPT());
			Formatting formatting = ticksPerSecond == 20 ? Formatting.DARK_GREEN : ticksPerSecond > 15 ? Formatting.YELLOW : ticksPerSecond > 10 ? Formatting.RED : Formatting.DARK_RED;
			playerEntity.networkHandler.sendPacket(new PlayerListHeaderS2CPacket(
				HEADER,
				new LiteralText("\nServer TPS: ").append(new LiteralText(decimalFormat.format(ticksPerSecond)).formatted(formatting)).append(FOOTER)
			));
		}

		ServerPlayerMixinInterface Iplayer = (ServerPlayerMixinInterface) playerEntity;
		World entityWorld = playerEntity.getEntityWorld();
		WorldBorder border = entityWorld.getWorldBorder();

		// Update location on action bar
		if (Iplayer.getCoordsBoolean()) {
			switch (playerEntity.getMovementDirection().getHorizontal()) {
				case 0 -> Iplayer.setDirection(ServerPlayerMixinInterface.Direction.SOUTH);
				case 1 -> Iplayer.setDirection(ServerPlayerMixinInterface.Direction.WEST);
				case 2 -> Iplayer.setDirection(ServerPlayerMixinInterface.Direction.NORTH);
				case 3 -> Iplayer.setDirection(ServerPlayerMixinInterface.Direction.EAST);
			}
			String locationInfo = "(%d, %d, %d) | Direction: %s | Distance to WB: %d | WB radius: %d".formatted(
				playerEntity.getBlockX(),
				playerEntity.getBlockY(),
				playerEntity.getBlockZ(),
				Iplayer.getDirection(),
				((int) border.getDistanceInsideBorder(playerEntity)),
				((int) border.getSize() / 2)
			);

			playerEntity.sendMessage(new LiteralText(locationInfo), true);
		}
	}

	// By Kman
	private static void updateWorldBorderArrow(ServerPlayerEntity playerEntity) {
		ServerPlayerMixinInterface Iplayer = (ServerPlayerMixinInterface) playerEntity;
		World entityWorld = playerEntity.getEntityWorld();
		WorldBorder Iborder = Iplayer.getWorldBorder();
		WorldBorder border = entityWorld.getWorldBorder();

		double playerX = playerEntity.getX();
		double playerZ = playerEntity.getZ();

		int playerBlockX = playerEntity.getBlockX();
		int playerBlockZ = playerEntity.getBlockZ();

		if (!playerEntity.isSpectator()) {
			// Checking to see if the players is behind the world boarder
			if (border.getDistanceInsideBorder(playerEntity) + 0.2 < 0) {
				// Setting some pre-stuff
				Iplayer.setAlready(true);
				Iplayer.setTime(Iplayer.getTime() + 1);
				int bottomBlockY = 0;
				// Trying to get the first non air block to put the arrow on to make it look nicer if they are jumping
				for (int y = playerEntity.getBlockY(); y > 0; y--) {
					if (!entityWorld.getBlockState(new BlockPos(playerBlockX, y, playerBlockZ)).equals(Blocks.AIR.getDefaultState())) {
						bottomBlockY = y + 1;
						break;
					}
				}
				// Checking to see the players is closer the wb on the x than the z
				if (playerX * playerX >= playerZ * playerZ) {
					// Checking to see if the player is closer to the west world border -- handing particle stuff (arrow)
					if (playerX - border.getBoundWest() > border.getBoundEast() - playerX) {
						Iplayer.getWorldBorder().setCenter(border.getSize(), border.getCenterZ());
						Iplayer.setDirection(ServerPlayerMixinInterface.Direction.WEST);

						for (int x = 0; x < 15; x++) {
							sendParticles(playerEntity, playerBlockX + 0.2 + (float) x / 45, bottomBlockY, playerBlockZ + 0.5 + (float) x / 30);
							sendParticles(playerEntity, playerBlockX + 0.2 + (float) x / 45, bottomBlockY, playerBlockZ + 0.5 - (float) x / 30);
							sendParticles(playerEntity, playerBlockX + 0.2 + (float) x / 20, bottomBlockY, playerBlockZ + 0.5);
						}
						// Checking to see if the player is closer to the east world border -- handing particle stuff (arrow)
					}
					else {
						Iplayer.getWorldBorder().setCenter(-1 * border.getSize(), border.getCenterZ());
						Iplayer.setDirection(ServerPlayerMixinInterface.Direction.EAST);
						for (int x = 0; x < 15; x++) {
							sendParticles(playerEntity, playerBlockX + 0.8 - (float) x / 45, bottomBlockY, playerBlockZ + 0.5 + (float) x / 30);
							sendParticles(playerEntity, playerBlockX + 0.8 - (float) x / 45, bottomBlockY, playerBlockZ + 0.5 - (float) x / 30);
							sendParticles(playerEntity, playerBlockX + 0.8 - (float) x / 20, bottomBlockY, playerBlockZ + 0.5);
						}
					}
				}
				else {
					// Checking to see if the player is closer to the north world border -- handing particle stuff (arrow)
					if (playerZ - border.getBoundNorth() > border.getBoundSouth() - playerZ) {
						Iplayer.getWorldBorder().setCenter(border.getCenterX(), border.getSize());
						Iplayer.setDirection(ServerPlayerMixinInterface.Direction.NORTH);

						for (int x = 0; x < 15; x++) {
							sendParticles(playerEntity, playerBlockX + 0.5 + (float) x / 30, bottomBlockY, playerBlockZ + 0.2 + (float) x / 45);
							sendParticles(playerEntity, playerBlockX + 0.5 - (float) x / 30, bottomBlockY, playerBlockZ + 0.2 + (float) x / 45);
							sendParticles(playerEntity, playerBlockX + 0.5, bottomBlockY, playerBlockZ + 0.2 + (float) x / 20);
						}
						// Checking to see if the player is closer to the south world border -- handing particle stuff (arrow)
					}
					else {
						Iplayer.getWorldBorder().setCenter(border.getCenterX(), -1 * border.getSize());
						Iplayer.setDirection(ServerPlayerMixinInterface.Direction.SOUTH);
						for (int x = 0; x < 15; x++) {
							sendParticles(playerEntity, playerBlockX + 0.5 + (float) x / 30, bottomBlockY, playerBlockZ + 0.8 - (float) x / 45);
							sendParticles(playerEntity, playerBlockX + 0.5 - (float) x / 30, bottomBlockY, playerBlockZ + 0.8 - (float) x / 45);
							sendParticles(playerEntity, playerBlockX + 0.5, bottomBlockY, playerBlockZ + 0.8 - (float) x / 20);
						}
					}
				}
				// Sending fake world border packets to the player
				Iplayer.getWorldBorder().setSize(border.getSize() + 1);

				sendWorldBorderPackets(playerEntity, Iborder);

				// The big title and subtitle msg packets -- sent every 100 game ticks
				if (Iplayer.getTime() % 100 == 0) {
					LiteralText title = new LiteralText("§cYou're outside the border!");
					LiteralText subTitle = new LiteralText("§cTravel " + "§a" + Iplayer.getDirection() + " §cto get to safety!");
					playerEntity.networkHandler.sendPacket(new TitleS2CPacket(title));
					playerEntity.networkHandler.sendPacket(new SubtitleS2CPacket(subTitle));
				}
				// The player is within the world border so we sent the correct packets one time to remove the fake world border
			}
			else if (Iplayer.getAlready()) {
				Iplayer.setAlready(false);
				Iplayer.setTime(99);
				sendWorldBorderPackets(playerEntity, playerEntity.world.getWorldBorder());
			}
			// The edge case where the player dies from the world border and is put into spectator mode. Sends correct world border packet one time
		}
		else if (Iplayer.getAlready()) {
			Iplayer.setAlready(false);
			Iplayer.setTime(99);
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
		UHCMod.UHC_SERVER.execute(() -> {
			for (ServerPlayerEntity playerEntity : UHCMod.UHC_SERVER.getPlayerManager().getPlayerList()) {
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

	public static void forceUpdateGlowingFlag(ServerPlayerEntity player) {
		// Force update the dirty flag in the tracker entry
		player.setGlowing(!player.isGlowing());
		player.setGlowing(!player.isGlowing());
	}

	public static EntityTrackerUpdateS2CPacket handleTrackerUpdatePacketForTeamGlowing(ServerPlayerEntity glowingPlayer, ServerPlayerEntity observingPlayer, EntityTrackerUpdateS2CPacket packet) {
		if (!GameSettings.FRIENDLY_PLAYER_GLOW.getValue()) {
			return packet;
		}
		if (!GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
			return packet;
		}
		if (!PlayerUtils.isPlayerSurvival(glowingPlayer)) {
			return packet;
		}
		if (!((ServerPlayerMixinInterface) observingPlayer).getGlowingBoolean()) {
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
			// need to compare ids because they're not the same instance once re-serialized
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
