package net.casualuhc.uhcmod.utils.scheduling;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
				Queue<Task> queue = TASKS.remove(tickCount++);
				if (queue != null) {
					queue.forEach(Task::run);
					queue.clear();
				}
			}
		});
	}

	private Scheduler() { }

	/**
	 * Converts minutes to ticks.
	 *
	 * @param minutes the number of minutes to convert.
	 * @return the number of ticks
	 */
	public static int minutesToTicks(int minutes) {
		return secondsToTicks(minutes * 60);
	}

	/**
	 * Converts seconds to ticks.
	 *
	 * @param seconds the number of seconds to convert.
	 * @return the number of ticks
	 */
	public static int secondsToTicks(int seconds) {
		return seconds * 20;
	}

	/**
	 * Schedule a task to be run on the Server in a certain number of ticks
	 *
	 * @param ticks the number of ticks to delay
	 * @param runnable the runnable to run
	 * @return a task which is able to be cancelled
	 */
	public static Task schedule(int ticks, Runnable runnable) {
		return schedule(ticks, new Task(runnable));
	}

	/**
	 *
	 * @param ticks the number of ticks to delay
	 * @param task the task to run
	 * @return the task which is able to be cancelled
	 * @see #schedule(int, Runnable)
	 */
	public static Task schedule(int ticks, Task task) {
		if (ticks < 0) {
			throw new IllegalArgumentException("Cannot schedule a task in the past");
		}
		TASKS.computeIfAbsent(tickCount + ticks, k -> new ArrayDeque<>()).add(task);
		return task;
	}

	/**
	 * Schedules a task to be run multiple times determined by
	 * a given interval util a certain time.
	 *
	 * @param delay the amount of ticks to initially delay
	 * @param interval the interval between executions
	 * @param duration the duration, this starts after the delay
	 * @param runnable the runnable to run
	 * @return the task which is able to be cancelled
	 * @see #schedule(int, Runnable)
	 */
	public static Task scheduleInLoop(int delay, int interval, int duration, Runnable runnable) {
		if (delay < 0 || interval <= 0 || duration < 0) {
			throw new IllegalArgumentException("Delay, interval or duration ticks cannot be negative");
		}
		Task task = new Task(runnable);
		for (int tick = delay; tick < duration + delay; tick += interval) {
			schedule(tick, task);
		}
		return task;
	}

	public static void noop() { }
}
