package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.Event.Events;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Phase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.WorldBorder;

import java.util.concurrent.TimeUnit;

public class WorldBorderManager {
	private static final MinecraftServer SERVER = UHCMod.UHC_SERVER;
	private static Stage currentStage = Stage.FIRST;

	static {
		Events.GRACE_PERIOD_FINISH.addListener(v -> {
			GameSettings.PVP.setValue(true);
			startWorldBorders();
		});

		Events.WORLD_BORDER_FINISH_SHRINKING.addListener(v -> {
			currentStage = currentStage.getNextStage();
			if (currentStage == null || !GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
				return;
			}

			GameSettings.WORLD_BORDER_STAGE.setValueQuietly(currentStage);
			if (currentStage == Stage.END) {
				Events.WORLD_BORDER_COMPLETE.trigger();
				return;
			}

			GameManager.INSTANCE.execute(() -> {
				UHCMod.UHC_SERVER.execute(() -> {
					double size = UHCMod.UHC_SERVER.getOverworld().getWorldBorder().getSize();
					long time = (long) (currentStage.getTime(size) * GameSettings.WORLD_BORDER_SPEED.getValue());
					moveWorldBorders(currentStage.getEndSize(), time);
				});
			}, 10, TimeUnit.SECONDS);
		});

		Events.WORLD_BORDER_COMPLETE.addListener(v -> {
			GameManager.INSTANCE.worldBorderComplete();
		});
	}

	public static void noop() { }

	public static void startWorldBorders() {
		UHCMod.UHC_SERVER.execute(() -> {
			double size = UHCMod.UHC_SERVER.getOverworld().getWorldBorder().getSize();
			currentStage = Stage.getStage(size);
			if (currentStage == null) {
				currentStage = Stage.FIRST;
				moveWorldBorders(Stage.FIRST.getStartSize(), 0);
			}

			GameSettings.WORLD_BORDER_STAGE.setValueQuietly(currentStage);
			if (currentStage == Stage.END) {
				Events.WORLD_BORDER_COMPLETE.trigger();
				return;
			}

			long time = (long) (currentStage.getTime(size) * GameSettings.WORLD_BORDER_SPEED.getValue());
			moveWorldBorders(currentStage.getEndSize(), time);
		});
	}

	public static void moveWorldBorders(double newSize, long time) {
		SERVER.execute(() ->
			SERVER.getWorlds().forEach(serverWorld -> {
				WorldBorder border = serverWorld.getWorldBorder();
				if (time != 0) {
					border.interpolateSize(border.getSize(), newSize, time * 1000);
					return;
				}
				border.setSize(newSize);
			})
		);
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
