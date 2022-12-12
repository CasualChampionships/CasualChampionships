package net.casualuhc.uhcmod.events;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.scheduling.Scheduler;
import net.casualuhc.uhcmod.utils.uhc.Event;
import net.casualuhc.uhcmod.utils.uhc.ItemUtils;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ChristmasUHC implements Event {
	private static final Vec3d LOBBY_SPAWN = new Vec3d(2, 272, 9);

	private static final ServerResourcePackProperties PROPERTIES = new ServerResourcePackProperties(
		"https://download.mc-packs.net/pack/09b7b5f2cd8e5ff163174d38a08d1919ea51b346.zip",
		"09b7b5f2cd8e5ff163174d38a08d1919ea51b346",
		false, Text.literal("This texture pack provides some festive features!")
	);

	@Override
	public MutableText getBossBarMessage() {
		return Text.literal("Welcome").formatted(Formatting.RED, Formatting.BOLD)
			.append(" ")
			.append(Text.literal("to").formatted(Formatting.WHITE, Formatting.BOLD))
			.append(" ")
			.append(Text.literal("Casual").formatted(Formatting.RED, Formatting.BOLD))
			.append(" ")
			.append(Text.literal("UHC").formatted(Formatting.WHITE, Formatting.BOLD));
	}

	@Override
	public BossBar.Color getBossBarColour() {
		return BossBar.Color.WHITE;
	}

	@Nullable
	@Override
	public ServerResourcePackProperties getResourcePack() {
		return PROPERTIES;
	}

	@Override
	public String getGoldenHeadTexture() {
		return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNhYWQ4NmM3MDhlYjI3NzczYTY0ZjkzNDc5ZTM5ZjA0NDJhNWNlMDg2YjYzMjk2YzdiN2QxY2Y1MTE2MDk1NiJ9fX0=";
	}

	@Override
	public String getLobbyName() {
		return "christmas_lobby";
	}

	@Override
	public Vec3d getLobbySpawnPos() {
		return LOBBY_SPAWN;
	}

	@Override
	public void load() {
		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onResourcePackLoaded(ServerPlayerEntity player) {
				if (!GameManager.isGameActive()) {
					Scheduler.schedule(Scheduler.secondsToTicks(3), () -> PlayerManager.playLobbyMusic(player, Scheduler.secondsToTicks(250)));

					PlayerInventory inventory = player.getInventory();
					if (inventory.isEmpty()) {
						inventory.armor.set(3, ItemUtils.named(Items.CARVED_PUMPKIN, "Santa's Hat"));
					}
				}
			}
		});
	}
}
