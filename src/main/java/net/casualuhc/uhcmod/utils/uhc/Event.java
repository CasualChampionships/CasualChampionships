package net.casualuhc.uhcmod.utils.uhc;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface Event {
	Event DEFAULT = new Event() { };
	Vec3d DEFAULT_SPAWN = new Vec3d(0, 320, 0);

	/**
	 * Returns the message displayed on the lobby boss bar.
	 *
	 * @return the message to display.
	 */
	default MutableText getBossBarMessage() {
		return Text.literal("Welcome to Casual UHC!").formatted(Formatting.BOLD);
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
	default MinecraftServer.ServerResourcePackProperties getResourcePack() {
		return null;
	}

	/**
	 * Gets the texture for the Golden Head item.
	 *
	 * @return the texture for the Golden Head item.
	 */
	default String getGoldenHeadTexture() {
		return "ewogICJ0aW1lc3RhbXAiIDogMTY3MDg2MDkyNTE4MywKICAicHJvZmlsZUlkIiA6ICI1N2E4NzA0ZGIzZjQ0YzhmYmVhMDY0Njc1MDExZmU3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQaGFudG9tVHVwYWMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk4Nzg4NTM2NTRjM2JiMjZmZDMzZTgwZjhlZDNjZGYwM2FiMzI0N2Y3MzY3ODQ2NGUwNmRhMTQzZGJkMGMxNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0=\", Signature: \"aspuyfmZ1/6+tDW+3slpVphd+caC2Fzex46aivvvhz24Qr8dWXomtZhibY2wR0QJbIR+6maoKh1nTIuPICjlzuD+Q7JY05dE6V1oTf1QQ+wekFXGKIM/zxlumKrpKv3Bkreu+tn0nU1gV+qNA/7CFgPZezFZ40mAupE79U9JIHXctF+wulxFhAeOgq2kcNTJW71ep9U75bJSUoRbrljITw89HhFWQqEkZdTkzCnc2rTPgQzruwe5NMzaUatYXAquNHlh9oKB7MC7OLUtIK9hRG3bSEujhtoJiSSvoOux2/Wokyy+DOUCIM3CsDmnCoEs5EVgmCiUeP1ATZS3QvYvckFiREf62KMLzz7UIxJ1fGF0oNZT6Rps9Bge6R+lSz+MvBEno3EcqEBKDvxwlaj+0XH+r3EHPFOB6KU83K7itj50Od2r1St2krUAMUrFk+Wd6aSF4xUoJ5Zn2CL97BIBOF3Vn2tKMmCxXtP1hShZHbhtrXCTugnt++53qh6Ilz9yHDw2VwlJDSRfT9oL6GYn917iZK2vLmJENPWhKITFLEpK6RxtJaGtIdk5OFlpKkdYr8JB6KzQ7ycdv+jLa/wfzY6AXhkfRvDoaEk3mE+4FRm2NkUFwlZ8bLWhXZ/BJAHe3EskQQlX+aNnU7dV6YRTSkS+4VA4uNx4zKZiuRf7S3g=";
	}

	/**
	 * Returns the name of the lobby.
	 *
	 * @return the name of the lobby.
	 */
	default String getLobbyName() {
		return "lobby";
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
