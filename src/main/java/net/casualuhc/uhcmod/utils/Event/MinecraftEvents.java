package net.casualuhc.uhcmod.utils.Event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface MinecraftEvents {
	default void onPlayerTick(ServerPlayerEntity player) { }

	default void onPlayerDeath(ServerPlayerEntity player, DamageSource source) { }

	default void onServerTick(MinecraftServer server) { }
}
