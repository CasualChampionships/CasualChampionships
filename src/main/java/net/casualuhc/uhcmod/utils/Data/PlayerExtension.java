package net.casualuhc.uhcmod.utils.Data;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerExtension {
	private static final Map<UUID, PlayerExtension> PLAYERS = new HashMap<>();

	public final WorldBorder worldBorder = new WorldBorder();

	public boolean displayCoords = false;
	public boolean shouldGlow = true;

	private PlayerExtension() { }

	public static PlayerExtension get(ServerPlayerEntity player) {
		return PLAYERS.computeIfAbsent(player.getUuid(), u -> new PlayerExtension());
	}
}
