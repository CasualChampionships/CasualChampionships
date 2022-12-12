package net.casualuhc.uhcmod.utils.event;

public interface UHCEvents {
	/**
	 * This event is fired when the UHC is set up.
	 */
	default void onSetup() { }

	/**
	 * This event is fired when the UHC lobby is generated.
	 */
	default void onLobby() { }

	/**
	 * This event is fired when players are asked if they are ready.
	 */
	default void onReady() { }

	/**
	 * This event is fired when the UHC starts (countdown and spread players).
	 */
	default void onStart() { }

	/**
	 * This event is fired when the UHC has finished starting (after players have been teleported).
	 */
	default void onActive() { }

	/**
	 * This event is fired when the UHC has ended.
	 */
	default void onEnd() { }

	/**
	 * This event is fired when the grace period ends.
	 */
	default void onGracePeriodEnd() { }

	/**
	 * This event is fired when the world border finishes shrinking.
	 */
	default void onWorldBorderFinishShrinking() { }

	/**
	 * This event is fired when the world border has completely finished shrinking.
	 */
	default void onWorldBorderComplete() { }
}
