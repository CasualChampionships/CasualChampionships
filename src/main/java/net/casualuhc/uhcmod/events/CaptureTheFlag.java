package net.casualuhc.uhcmod.events;

import net.casualuhc.uhcmod.utils.uhc.Event;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class CaptureTheFlag implements Event {
	private static final ServerResourcePackProperties DEFAULT_PACK = new ServerResourcePackProperties(
		"https://download.mc-packs.net/pack/2758f9e41b6e4fc5c6e2be0a807ca1ff83c8086c.zip",
		"2758f9e41b6e4fc5c6e2be0a807ca1ff83c8086c",
		true, null
	);

	@Override
	public MutableText getBossBarMessage() {
		return Text.translatable("uhc.lobby.welcome").append(" Capture The Flag").formatted(Formatting.BOLD, Formatting.WHITE);
	}

	@Override
	public BossBar.Color getBossBarColour() {
		return Event.super.getBossBarColour();
	}

	@Nullable
	@Override
	public ServerResourcePackProperties getResourcePack() {
		return DEFAULT_PACK;
	}

	@Override
	public Vec3d getLobbySpawnPos() {
		return new Vec3d(1117.48, 80.00, -6518.45);
	}

	@Override
	public void load() {
		Event.super.load();
	}
}
