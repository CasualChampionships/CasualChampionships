package net.casualuhc.uhcmod.managers

import net.casualuhc.arcade.border.ArcadeBorder
import net.casualuhc.arcade.border.BorderState
import net.casualuhc.arcade.border.StillBorderState
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.events.uhc.UHCBorderCompleteEvent
import net.casualuhc.uhcmod.events.uhc.UHCGracePeriodEndEvent
import net.casualuhc.uhcmod.settings.GameSettings

object WorldBorderManager {
    private val global = UHCBorder()
    const val PORTAL_ESCAPE_TIME_SECONDS = 30

    fun startWorldBorders() {
        val size = this.global.size
        var stage = Stage.getStage(size)
        if (stage == null) {
            stage = Stage.FIRST
            moveWorldBorders(Stage.FIRST.startSize)
        }
        GameSettings.WORLD_BORDER_STAGE.setValueQuietly(stage)
        if (stage == Stage.END) {
            GlobalEventHandler.broadcast(UHCBorderCompleteEvent())
            return
        }
        this.moveWorldBorders(stage.endSize, stage.getRemainingTimeAsPercent(size))
    }

    fun moveWorldBorders(newSize: Double, percent: Double = -1.0) {
        val seconds = (percent * GameSettings.WORLD_BORDER_TIME.value).toLong()
        val border = global
        if (seconds > 0) {
            this.global.lerpSizeBetween(border.size, newSize, seconds * 1000L)
            return
        }
        border.size = newSize
    }

    @JvmStatic
    fun getGlobalBorder(): ArcadeBorder {
        return this.global
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerTickEvent> { this.global.update() }
        GlobalEventHandler.register<UHCGracePeriodEndEvent> { this.startWorldBorders() }
    }

    private fun onBorderStageComplete() {
        UHCMod.logger.info("Finished world border stage: ${GameSettings.WORLD_BORDER_STAGE.value}")

        if (!UHCManager.isActivePhase()) {
            return
        }

        val next = GameSettings.WORLD_BORDER_STAGE.value.getNextStage()
        GameSettings.WORLD_BORDER_STAGE.setValueQuietly(next)

        if (next == Stage.END) {
            GlobalEventHandler.broadcast(UHCBorderCompleteEvent())
            return
        }
        UHCManager.schedulePhaseTask(10, Seconds) {
            this.moveWorldBorders(next.endSize, next.getRemainingTimeAsPercent(this.global.size))
        }
    }

    private class UHCBorder: ArcadeBorder() {
        override var state: BorderState = StillBorderState(this, Stage.FIRST.startSize)

        override fun tick() {
            // We don't tick because this would get called by every world
        }

        fun update() {
            val previous = this.state
            super.tick()
            if (this.state !== previous) {
                onBorderStageComplete()
            }
        }
    }

    enum class Stage(
        val startSize: Double,
        val endSize: Double,
        private val weight: Double
    ) {
        FIRST(6128.0, 3064.0, 18.0),
        SECOND(FIRST.endSize, 1532.0, 15.0),
        THIRD(SECOND.endSize, 766.0, 9.0),
        FOURTH(THIRD.endSize, 383.0, 8.0),
        FIFTH(FOURTH.endSize, 180.0, 7.0),
        SIX(FIFTH.endSize, 50.0, 5.0),
        FINAL(SIX.endSize, 20.0, 2.0),
        END(FINAL.endSize, 0.0, 0.0);

        fun getRemainingTimeAsPercent(size: Double): Double {
            val remainingSize = size - this.endSize
            val totalSize = this.startSize - this.endSize
            return (remainingSize / totalSize) * (this.weight / TOTAL_WEIGHT)
        }

        fun getNextStage(): Stage {
            val next = this.ordinal + 1
            val values = values()
            return if (next < values.size) values[next] else END
        }

        companion object {
            val TOTAL_WEIGHT = values().sumOf { it.weight }

            fun getStage(size: Double): Stage? {
                if (size <= FINAL.endSize) {
                    return END
                }
                for (stage in values()) {
                    if (size <= stage.startSize && size > stage.endSize) {
                        return stage
                    }
                }
                return null
            }
        }
    }
}
