package net.casualuhc.uhcmod.utils.event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface MinecraftEvents {
	/**
	 * This event is fired when a player joins the server.
	 *
	 * @param player the player that joined.
	 */
	default void onPlayerJoin(ServerPlayerEntity player) { }

	/**
	 * This event is fired when a player is ticked (before player tick logic).
	 *
	 * @param player the player that is being ticked.
	 */
	default void onPlayerTick(ServerPlayerEntity player) { }

	/**
	 * This event is fired when a player dies (after death logic).
	 *
	 * @param player the player that died.
	 * @param source the damage source that caused death.
	 */
	default void onPlayerDeath(ServerPlayerEntity player, DamageSource source) { }

	/**
	 * This event is fired when a player leaves.
	 *
	 * @param player the player that left.
	 */
	default void onPlayerLeave(ServerPlayerEntity player) { }

	/**
	 * This event is fired when a player successfully loaded the server resource pack.
	 *
	 * @param player the player that loaded the pack.
	 */
	default void onResourcePackLoaded(ServerPlayerEntity player) { }

	/**
	 * This event fires when after the server has ticked.
	 *
	 * @param server the Minecraft Server.
	 */
	default void onServerTick(MinecraftServer server) { }
}
