package net.casualuhc.uhcmod.utils.scheduling;

public class Task {
	private final Runnable runnable;
	private boolean cancelled;

	public Task(Runnable runnable) {
		this.runnable = runnable;
	}

	public void cancel() {
		this.cancelled = true;
	}

	public void run() {
		if (!this.cancelled) {
			this.runnable.run();
		}
	}
}
