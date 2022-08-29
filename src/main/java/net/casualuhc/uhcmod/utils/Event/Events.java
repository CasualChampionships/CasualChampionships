package net.casualuhc.uhcmod.utils.Event;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

public class Events {
	public static final Event<Void>
		ON_SETUP = new Event<>(),
		ON_LOBBY = new Event<>(),
		ON_READY = new Event<>(),
		ON_START = new Event<>(),
		ON_ACTIVE = new Event<>(),
		ON_END = new Event<>(),

		GRACE_PERIOD_FINISH = new Event<>(),
		WORLD_BORDER_FINISH_SHRINKING = new Event<>(),
		WORLD_BORDER_COMPLETE = new Event<>();

	public static final Event<ServerPlayerEntity>
		ON_PLAYER_TICK = new Event<>();

	public static final Event<Pair<ServerPlayerEntity, DamageSource>>
		ON_PLAYER_DEATH = new Event<>();

	public static final Event<MinecraftServer>
		ON_SERVER_TICK = new Event<>();
}
