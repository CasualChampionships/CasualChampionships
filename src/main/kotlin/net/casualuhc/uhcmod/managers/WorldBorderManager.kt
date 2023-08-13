package net.casualuhc.uhcmod.managers

import net.casualuhc.arcade.border.MultiLevelBorderListener
import net.casualuhc.arcade.border.MultiLevelBorderTracker
import net.casualuhc.arcade.border.TrackedBorder
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.casualuhc.arcade.utils.BorderUtils
import net.casualuhc.arcade.utils.LevelUtils
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.events.uhc.UHCBorderCompleteEvent
import net.casualuhc.uhcmod.events.uhc.UHCGracePeriodEndEvent
import net.casualuhc.uhcmod.settings.GameSettings
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

object WorldBorderManager: MultiLevelBorderListener {
    private val tracker = MultiLevelBorderTracker()

    const val PORTAL_ESCAPE_TIME_SECONDS = 30

    var stage: Stage
        get() = GameSettings.WORLD_BORDER_STAGE.value
        private set(value) = GameSettings.WORLD_BORDER_STAGE.setValueQuietly(value)
    val multiplier: Long
        get() = GameSettings.WORLD_BORDER_TIME.value

    fun startWorldBorders() {
        if (this.stage == Stage.END) {
            GlobalEventHandler.broadcast(UHCBorderCompleteEvent())
            return
        }
        this.moveWorldBorders(this.stage)
    }

    fun moveWorldBorders(stage: Stage, size: Size = Size.END, instant: Boolean = false) {
        for ((border, level) in this.tracker.getAllTracking()) {
            this.moveWorldBorder(border, level, stage, size, instant)
        }
    }

    fun pauseWorldBorders() {
        for ((border, _) in this.tracker.getAllTracking()) {
            this.moveWorldBorder(border, border.size)
        }
    }

    override fun onAllBordersComplete(borders: Map<TrackedBorder, ServerLevel>) {
        UHCMod.logger.info("Finished world border stage: ${this.stage}")
        if (!UHCManager.isActivePhase()) {
            return
        }
        val next = this.stage.getNextStage()
        this.stage = next

        if (next == Stage.END) {
            GlobalEventHandler.broadcast(UHCBorderCompleteEvent())
            return
        }
        super.onAllBordersComplete(borders)
    }

    override fun onAllBordersComplete(border: TrackedBorder, level: ServerLevel) {
        // We don't shrink past the fifth stage in the end because
        // otherwise it becomes impossible to enter the end dimension
        if (level.dimension() == Level.END && this.stage > Stage.FIFTH) {
            return
        }

        UHCManager.schedulePhaseTask(10, Seconds) {
            this.moveWorldBorder(border, level, this.stage, Size.END)
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerLoadedEvent> { this.onServerLoadedEvent() }
        GlobalEventHandler.register<UHCGracePeriodEndEvent> { this.startWorldBorders() }
    }

    private fun moveWorldBorder(border: TrackedBorder, level: Level, stage: Stage, size: Size, instant: Boolean = false) {
        val dest = if (size == Size.END) stage.getEndSizeFor(level) else stage.getStartSizeFor(level)
        val time = if (instant) -1.0 else stage.getRemainingTimeAsPercent(border.size, level)
        this.moveWorldBorder(border, dest, time)
    }

    private fun moveWorldBorder(border: TrackedBorder, newSize: Double, percent: Double = -1.0) {
        val seconds = (percent * this.multiplier).toLong()
        if (seconds > 0) {
            border.lerpSizeBetween(border.size, newSize, seconds * 1000L)
            return
        }
        border.size = newSize
    }

    private fun onServerLoadedEvent() {
        this.tracker.addLevelBorder(LevelUtils.overworld())
        this.tracker.addLevelBorder(LevelUtils.nether())
        this.tracker.addLevelBorder(LevelUtils.end())

        BorderUtils.isolateWorldBorders()
    }

    enum class Size {
        START, END
    }

    enum class Stage(
        private val startSize: Double,
        private val endSize: Double,
        private val weight: Double
    ) {
        FIRST(6128.0, 3064.0, 18.0),
        SECOND(FIRST.endSize, 1500.0, 15.0),
        THIRD(SECOND.endSize, 780.0, 9.0),
        FOURTH(THIRD.endSize, 400.0, 8.0),
        FIFTH(FOURTH.endSize, 220.0, 7.0),
        SIX(FIFTH.endSize, 50.0, 5.0),
        FINAL(SIX.endSize, 20.0, 2.0),
        END(FINAL.endSize, 0.0, 0.0);

        fun getRemainingTimeAsPercent(size: Double, level: Level): Double {
            val remainingSize = size - this.getEndSizeFor(level)
            val totalSize = this.getStartSizeFor(level) - this.getEndSizeFor(level)
            return (remainingSize / totalSize) * (this.weight / TOTAL_WEIGHT)
        }

        fun getNextStage(): Stage {
            val next = this.ordinal + 1
            val values = values()
            return if (next < values.size) values[next] else END
        }

        fun getStartSizeFor(level: Level): Double {
            return adjustSize(this.startSize, level)
        }

        fun getEndSizeFor(level: Level): Double {
            return adjustSize(this.endSize, level)
        }

        companion object {
            val TOTAL_WEIGHT = values().sumOf { it.weight }

            fun getStage(size: Double, level: Level): Stage? {
                val adjusted = reverseAdjustSize(size, level)
                if (adjusted <= FINAL.endSize) {
                    return END
                }
                for (stage in values()) {
                    if (adjusted <= stage.startSize && adjusted > stage.endSize) {
                        return stage
                    }
                }
                return null
            }

            fun adjustSize(size: Double, level: Level): Double {
                return size / level.dimensionType().coordinateScale
            }

            fun reverseAdjustSize(size: Double, level: Level): Double {
                return size * level.dimensionType().coordinateScale
            }
        }
    }
}
