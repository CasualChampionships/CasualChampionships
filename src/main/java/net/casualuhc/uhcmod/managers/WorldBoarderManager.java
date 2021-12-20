package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.border.WorldBorder;

public class WorldBoarderManager {

    private static final MinecraftServer server = UHCMod.UHCServer;
    public static float grace = 300000;

    public static void moveWorldBorders(double newSize, long time) {
        server.execute(() ->
            server.getWorlds().forEach(serverWorld -> {
                WorldBorder border = serverWorld.getWorldBorder();
                if (time != 0) {
                    border.interpolateSize(border.getSize(), newSize, time * 1000);
                }
                else {
                    border.setSize(newSize);
                }
            })
        );
    }

    public static void startWorldBorders(ThreadGroup threadGroup, final boolean ignoreGrace) {
        Thread thread = new Thread(threadGroup, () -> {
            try {
                Thread.sleep(200);
                if (!ignoreGrace) {
                    int minutes = (int) ((grace * GameSettings.WORLD_BORDER_SPEED.getValue()) / 60000);
                    PlayerUtils.messageEveryPlayer(new LiteralText("World border will start moving in %d minutes".formatted(minutes)).formatted(Formatting.GREEN));
                    Thread.sleep((long) ((long) grace * GameSettings.WORLD_BORDER_SPEED.getValue()) + 1);
                    PlayerUtils.forEveryPlayer(playerEntity -> {
                        playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0F, 1.0F);
                        playerEntity.sendMessage(new LiteralText("World border will start moving now").formatted(Formatting.RED), false);
                    });
                }
                boolean shouldContinue = true;
                while (shouldContinue) {
                    float size = (float) UHCMod.UHCServer.getOverworld().getWorldBorder().getSize();
                    Stage currentStage = Stage.getStage(size);
                    if (currentStage == null) {
                        break;
                    }
                    long time = (long) (currentStage.getTime(size) * GameSettings.WORLD_BORDER_SPEED.getValue());
                    moveWorldBorders(currentStage.getEndSize(), time);
                    long sleepTime = time * 1000;
                    Thread.sleep((long) (sleepTime + (sleepTime * 0.01)));
                    if (!GameManager.isPhase(Phase.ACTIVE)) {
                        shouldContinue = false;
                    }
                    if (currentStage == Stage.FINAL) {
                        UHCMod.UHCLogger.info("Hit final border");
                        GameManager.worldBorderFinishedPre();
                        Thread.sleep(300000);
                        GameManager.worldBorderFinishedPost();
                        break;
                    }
                }
            }
            catch (InterruptedException ignored) { }
        }, "World Border Thread");
        thread.setDaemon(true);
        thread.start();
    }

    public enum Stage {
        FIRST(6128, 3064, 1500),
        SECOND(3064, 1532),
        THIRD(1532, 766, 1000),
        FOURTH(766, 383, 900),
        FIFTH(383, 180, 800),
        SIX(180, 50, 500),
        FINAL(50, 20, 200),
        ;

        private final float startSize;
        private final float endSize;
        private final float time;

        Stage(float startSize, float endSize, long time) {
            this.startSize = startSize;
            this.endSize = endSize;
            this.time = time;
        }

        Stage(float startSize, float endSize) {
            this(startSize, endSize, (long) (startSize - endSize));
        }

        public float getStartSize() {
            return this.startSize;
        }

        public float getEndSize() {
            return this.endSize;
        }

        public long getTime(float size) {
            float blockPerTime = (this.startSize - this.endSize) / this.time;
            return (long) ((size - this.endSize) / blockPerTime);
        }

        public static Stage getStage(float size) {
            if (size == FINAL.endSize) {
                return FINAL;
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
