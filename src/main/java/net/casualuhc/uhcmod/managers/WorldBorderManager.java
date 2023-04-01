package net.casualuhc.uhcmod.managers;

import net.casualuhc.arcade.border.CustomBorder;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.border.CustomBorderFinishEvent;
import net.casualuhc.arcade.events.server.ServerTickEvent;
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.events.uhc.UHCBorderCompleteEvent;
import net.casualuhc.uhcmod.events.uhc.UHCGracePeriodEndEvent;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderManager {
	private static final MinecraftServer SERVER = UHCMod.SERVER;
	public static final CustomBorder GLOBAL_BORDER = new CustomBorder();
	public static final int PORTAL_ESCAPE_TIME_SECONDS = 30;

	public static void startWorldBorders() {
		double size = UHCMod.SERVER.getOverworld().getWorldBorder().getSize();
		Stage stage = Stage.getStage(size);
		if (stage == null) {
			stage = Stage.FIRST;
			moveWorldBorders(Stage.FIRST.getStartSize(), 0);
		}

		GameSettings.WORLD_BORDER_STAGE.setValueQuietly(stage);
		if (stage == Stage.END) {
			EventHandler.broadcast(new UHCBorderCompleteEvent());
			return;
		}

		moveWorldBorders(stage.getEndSize(), stage.getTime(size));
	}

	public static void moveWorldBorders(double newSize, long time) {
		long modifiedTime = (long) (time * GameSettings.WORLD_BORDER_SPEED.getValue());
		SERVER.getWorlds().forEach(world -> {
			WorldBorder border = world.getWorldBorder();
			if (modifiedTime != 0) {
				border.interpolateSize(border.getSize(), newSize, modifiedTime * 1000);
				return;
			}
			border.setSize(newSize);
		});
	}

	public static void noop() { }

	static {
		EventHandler.register(CustomBorderFinishEvent.class, event -> {
			UHCMod.LOGGER.info("Finished world border: {}", GameSettings.WORLD_BORDER_STAGE.getValue());

			Stage nextStage = GameSettings.WORLD_BORDER_STAGE.getValue().getNextStage();
			if (nextStage == null || !UHCManager.isPhase(Phase.ACTIVE)) {
				return;
			}

			GameSettings.WORLD_BORDER_STAGE.setValueQuietly(nextStage);
			if (nextStage == Stage.END) {
				EventHandler.broadcast(new UHCBorderCompleteEvent());
				return;
			}

			UHCManager.schedulePhaseTask(10, MinecraftTimeUnit.Seconds, () -> {
				double size = UHCMod.SERVER.getOverworld().getWorldBorder().getSize();
				moveWorldBorders(nextStage.getEndSize(), nextStage.getTime(size));
			});
		});
		EventHandler.register(UHCGracePeriodEndEvent.class, event -> {
			startWorldBorders();
		});
		EventHandler.register(ServerTickEvent.class, event -> {
			GLOBAL_BORDER.update();
		});
	}

	public enum Stage {
		FIRST(6128, 3064, 1800),
		SECOND(3064, 1532),
		THIRD(1532, 766, 900),
		FOURTH(766, 383, 800),
		FIFTH(383, 180, 700),
		SIX(180, 50, 500),
		FINAL(50, 20, 200),
		END(20, 0, 0);

		private final long startSize;
		private final long endSize;
		private final long time;

		Stage(long startSize, long endSize, long time) {
			this.startSize = startSize;
			this.endSize = endSize;
			this.time = time;
		}

		Stage(long startSize, long endSize) {
			this(startSize, endSize, startSize - endSize);
		}

		public float getStartSize() {
			return this.startSize;
		}

		public float getEndSize() {
			return this.endSize;
		}

		public long getTime(double size) {
			double blockPerTime = (this.startSize - this.endSize) / (double) this.time;
			return (long) (((long) size - this.endSize) / blockPerTime);
		}

		public Stage getNextStage() {
			int next = this.ordinal() + 1;
			Stage[] values = Stage.values();

			if (next < values.length) {
				return values[next];
			}
			return END;
		}

		public static Stage getStage(double size) {
			if (size <= FINAL.endSize) {
				return END;
			}
			for (Stage stage : Stage.values()) {
				if (size <= stage.startSize && size > stage.endSize) {
					return stage;
				}
			}
			return null;
		}
	}
}
