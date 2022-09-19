package net.casualuhc.uhcmod.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.Queue;

public class Scheduler {
	private static final Int2ObjectOpenHashMap<Queue<Task>> TASKS = new Int2ObjectOpenHashMap<>();
	private static int tickCount = 0;

	static {
		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onServerTick(MinecraftServer server) {
				// UHCMod.LOGGER.info("{}", tickCount);
				Queue<Task> queue = TASKS.remove(tickCount++);
				if (queue != null) {
					queue.forEach(Task::run);
					queue.clear();
				}
			}
		});
	}

	private Scheduler() { }

	public static int minutesToTicks(int minutes) {
		return secondsToTicks(minutes * 60);
	}

	public static int secondsToTicks(int seconds) {
		return seconds * 20;
	}

	public static Task schedule(int ticks, Runnable runnable) {
		return schedule(ticks, new Task(runnable));
	}

	public static Task schedule(int ticks, Task task) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Cannot schedule a task in the past");
		}
		TASKS.computeIfAbsent(tickCount + ticks, k -> new ArrayDeque<>()).add(task);
		return task;
	}

	public static Task scheduleInLoop(int delay, int interval, int until, Runnable runnable) {
		if (delay < 0 || interval <= 0 || until < 0) {
			throw new IllegalArgumentException("Delay, interval or until ticks cannot be negative");
		}
		Task task = new Task(runnable);
		for (int tick = delay; tick < until; tick += interval) {
			schedule(tick, task);
		}
		return task;
	}
}
