package net.casualuhc.uhcmod.utils.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.world.border.WorldBorder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerExtension {
	private static final Map<UUID, PlayerExtension> PLAYERS = new HashMap<>();

	public final WorldBorder worldBorder = new WorldBorder();

	public AbstractTeam trueTeam = null;
	public boolean wasInWorldBorder = false;
	public int damageDealt = 0;
	public int relogs = 0;

	public boolean hasResourcePack = false;
	public boolean displayCoords = false;
	public boolean shouldGlow = true;
	public boolean fullbright = true;

	private PlayerExtension() { }

	public static PlayerExtension get(PlayerEntity player) {
		return PLAYERS.computeIfAbsent(player.getUuid(), u -> new PlayerExtension());
	}

	public static void reset() {
		PLAYERS.values().forEach(p -> {
			p.trueTeam = null;
			p.wasInWorldBorder = false;
			p.damageDealt = 0;
			p.relogs = 0;
		});
	}
}
