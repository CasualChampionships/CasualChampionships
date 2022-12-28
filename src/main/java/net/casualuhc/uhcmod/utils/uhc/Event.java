package net.casualuhc.uhcmod.utils.uhc;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface Event {
	Event DEFAULT = new Event() { };
	Vec3d DEFAULT_SPAWN = new Vec3d(0, 320, 0);

	ServerResourcePackProperties DEFAULT_PACK = new ServerResourcePackProperties(
		"https://download.mc-packs.net/pack/3557274cfde1ef3fb964e70b3df841adbe92c60b.zip",
		"3557274cfde1ef3fb964e70b3df841adbe92c60b",
		true, null
	);

	/**
	 * Returns the message displayed on the lobby boss bar.
	 *
	 * @return the message to display.
	 */
	default MutableText getBossBarMessage() {
		return Text.translatable("uhc.lobby.welcome").append(" Casual UHC").formatted(Formatting.BOLD);
	}

	/**
	 * Returns the colour the lobby boss bar should be.
	 *
	 * @return the boss bar colour.
	 */
	default BossBar.Color getBossBarColour() {
		return BossBar.Color.BLUE;
	}

	/**
	 * Returns the resource pack properties
	 *
	 * @return the pack properties.
	 */
	@Nullable
	default ServerResourcePackProperties getResourcePack() {
		return DEFAULT_PACK;
	}

	/**
	 * Returns the spawning position for the lobby.
	 */
	default Vec3d getLobbySpawnPos() {
		return DEFAULT_SPAWN;
	}

	/**
	 * Invoked after all mangers have been initialised.
	 */
	default void load() { }
}
