package net.casualuhc.uhcmod.utils.Event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.LinkedHashSet;
import java.util.Set;

public class EventHandler {
	private static final Set<MinecraftEvents> MINECRAFT_EVENTS = new LinkedHashSet<>();
	private static final Set<UHCEvents> UHC_EVENTS = new LinkedHashSet<>();

	public static void register(MinecraftEvents events) {
		MINECRAFT_EVENTS.add(events);
	}

	public static void register(UHCEvents events) {
		UHC_EVENTS.add(events);
	}

	public static void onPlayerTick(ServerPlayerEntity player) {
		MINECRAFT_EVENTS.forEach(e -> e.onPlayerTick(player));
	}

	public static void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		MINECRAFT_EVENTS.forEach(e -> e.onPlayerDeath(player, source));
	}

	public static void onServerTick(MinecraftServer server) {
		MINECRAFT_EVENTS.forEach(e -> e.onServerTick(server));
	}

	public static void onSetup() {
		UHC_EVENTS.forEach(UHCEvents::onSetup);
	}

	public static void onLobby() {
		UHC_EVENTS.forEach(UHCEvents::onLobby);
	}

	public static void onReady() {
		UHC_EVENTS.forEach(UHCEvents::onReady);
	}

	public static void onStart() {
		UHC_EVENTS.forEach(UHCEvents::onStart);
	}

	public static void onActive() {
		UHC_EVENTS.forEach(UHCEvents::onActive);
	}

	public static void onEnd() {
		UHC_EVENTS.forEach(UHCEvents::onEnd);
	}

	public static void onGracePeriodEnd() {
		UHC_EVENTS.forEach(UHCEvents::onGracePeriodEnd);
	}

	public static void onWorldBorderFinishShrinking() {
		UHC_EVENTS.forEach(UHCEvents::onWorldBorderFinishShrinking);
	}

	public static void onWorldBorderComplete() {
		UHC_EVENTS.forEach(UHCEvents::onWorldBorderComplete);
	}
}
