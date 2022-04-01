package net.casualuhc.uhcmod.utils.Event;

import net.minecraft.server.network.ServerPlayerEntity;

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
		WORLD_BORDER_COMPLETE = new Event<>()
		;
	public static final Event<ServerPlayerEntity>
		ON_PLAYER_DEATH = new Event<>(),
		ON_PLAYER_TICK = new Event<>()
		;
}
