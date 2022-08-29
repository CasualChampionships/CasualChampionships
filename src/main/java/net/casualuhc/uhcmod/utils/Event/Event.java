package net.casualuhc.uhcmod.utils.Event;

import net.casualuhc.uhcmod.UHCMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Event<T> {
	private final List<Consumer<T>> callbacks;

	Event() {
		this.callbacks = new ArrayList<>();
	}

	public void trigger() {
		this.trigger(null);
	}

	public void trigger(T eventValue) {
		// Make sure everything is on main thread
		UHCMod.SERVER.execute(() -> {
			this.callbacks.forEach(c -> c.accept(eventValue));
		});
	}

	public void addListener(Consumer<T> consumer) {
		this.callbacks.add(consumer);
	}
}
