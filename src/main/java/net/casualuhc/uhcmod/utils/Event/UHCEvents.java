package net.casualuhc.uhcmod.utils.Event;

public interface UHCEvents {
	default void onSetup() { }

	default void onLobby() { }

	default void onReady() { }

	default void onStart() { }

	default void onActive() { }

	default void onEnd() { }

	default void onGracePeriodEnd() { }

	default void onWorldBorderFinishShrinking() { }

	default void onWorldBorderComplete() { }
}
