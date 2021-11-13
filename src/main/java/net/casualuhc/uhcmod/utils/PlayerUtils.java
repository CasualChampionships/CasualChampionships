package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.function.Consumer;

public class PlayerUtils {
	
	public static void updateActionBar(ServerPlayerEntity playerEntity) {
		// Only non spectators
		if (playerEntity.isSpectator()) {
			return;
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
	
	public static void updateWorldBorderArrow(ServerPlayerEntity playerEntity) {
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
		UHCMod.UHCServer.execute(() -> {
			for (ServerPlayerEntity playerEntity : UHCMod.UHCServer.getPlayerManager().getPlayerList()) {
				consumer.accept(playerEntity);
			}
		});
	}

	public static void messageEveryPlayer(Text text) {
		forEveryPlayer(playerEntity -> playerEntity.sendMessage(text, false));
	}
}
