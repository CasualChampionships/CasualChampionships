package net.casualuhc.uhcmod.utils.data;

import net.casualuhc.uhcmod.utils.stat.PlayerStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.border.WorldBorder;

import java.util.*;
import java.util.function.Consumer;

import static net.casualuhc.uhcmod.utils.data.PlayerFlag.*;

/**
 * This class serves to store extra data about a {@link net.minecraft.server.network.ServerPlayerEntity}.
 * <p>
 * This information is kept until the server is shut down - even if the Player re-logs.
 */
public class PlayerExtension {
	private static final Map<UUID, PlayerExtension> PLAYERS = new HashMap<>();

	private final String name;
	private final PlayerStats stats = new PlayerStats();
	private final WorldBorder fakeBorder = new WorldBorder();
	private final Set<PlayerFlag> flags = EnumSet.noneOf(PlayerFlag.class);

	private Team realTeam = null;

	private PlayerExtension(String name) {
		this.name = name;
		this.resetFlags();
	}

	public String getName() {
		return this.name;
	}

	public WorldBorder getFakeBorder() {
		return this.fakeBorder;
	}

	public void setRealTeam(AbstractTeam realTeam) {
		this.realTeam = (Team) realTeam;
	}

	public Team getRealTeam() {
		return this.realTeam;
	}

	public PlayerStats getStats() {
		return this.stats;
	}

	public boolean getFlag(PlayerFlag flag) {
		return this.flags.contains(flag);
	}

	/**
	 * Sets a specific flag for the player.
	 *
	 * @param flag the flag to set.
	 * @param bool whether it should be removed or added.
	 * @return true if the flag changed, false otherwise.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean setFlag(PlayerFlag flag, boolean bool) {
		return bool ? this.flags.add(flag) : this.flags.remove(flag);
	}

	public void toggleFlag(PlayerFlag flag) {
		if (!this.flags.add(flag)) {
			this.flags.remove(flag);
		}
	}

	public void resetFlags() {
		this.flags.clear();
		this.setFlag(GLOW_ENABLED, true);
		this.setFlag(HUD_ENABLED, true);
		this.setFlag(FULL_BRIGHT_ENABLED, true);
	}

	public static PlayerExtension get(PlayerEntity player) {
		return PLAYERS.computeIfAbsent(player.getUuid(), u -> new PlayerExtension(player.getEntityName()));
	}

	public static void forEach(Consumer<PlayerExtension> consumer) {
		PLAYERS.values().forEach(consumer);
	}

	public static void apply(PlayerEntity player, Consumer<PlayerExtension> consumer) {
		consumer.accept(get(player));
	}

	public static void reset() {
		forEach(p -> {
			p.setRealTeam(null);
			p.resetFlags();
			p.stats.reset();
		});
	}
}
