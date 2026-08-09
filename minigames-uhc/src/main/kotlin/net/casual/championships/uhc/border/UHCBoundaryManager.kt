package net.casual.championships.uhc.border

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.LevelBoundary.SizeAndCenter
import net.casual.arcade.boundary.renderer.AxisAlignedDisplayBoundaryRenderer
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.shape.AxisAlignedBoundaryShape
import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.registries.toIdString
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.championships.uhc.CasualUHC
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.routine.BoundaryResumeRoutine
import net.casual.championships.uhc.routine.GlowingCountdownRoutine
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

object UHCBoundaryManager {
    fun reset(uhc: UHCMinigame) {
        for (level in uhc.levels) {
            level.levelBoundary = this.createLevelBoundary(uhc, level)
        }
    }

    fun start(uhc: UHCMinigame) {
        uhc.onStartBoundaryTimer()
        this.move(uhc, UHCBoundaryPhase.First)
    }

    fun calculateSizeAndCenter(
        uhc: UHCMinigame,
        level: ServerLevel,
        target: SizeAndCenter
    ): SizeAndCenter {
        val clamped = this.clampBoundarySize(uhc, level, target.size)
        val scale = 1 / level.dimensionType().coordinateScale
        val sizeScale = uhc.settings.borderSizeMultiplier * scale
        val size = clamped.multiply(sizeScale, 1.0, sizeScale)
        val center = target.center.multiply(scale, 1.0, scale)
        return SizeAndCenter(size, center)
    }

    fun getFinalPhase(uhc: UHCMinigame, level: ServerLevel): UHCBoundaryPhase {
        return when (level) {
            uhc.nether -> UHCBoundaryPhase.Fourth
            uhc.end -> UHCBoundaryPhase.Third
            else -> UHCBoundaryPhase.Sixth
        }
    }

    internal fun move(uhc: UHCMinigame, current: UHCBoundaryPhase) {
        uhc.boundaryPhase = current
        this.move(uhc, MinecraftTimeDuration.ZERO) { current.getStart(it) }
        val duration = current.getDuration(uhc.settings.borderTime)
        this.move(uhc, duration) { current.getEnd(it) }
        uhc.scheduler.schedule(duration, MinigameTask(uhc) { minigame ->
            this.complete(minigame, uhc.boundaryPhase) // current
        })
    }

    private fun complete(uhc: UHCMinigame, current: UHCBoundaryPhase) {
        if (current.getNextStage() == current) {
            CasualUHC.logger.info("Completed boundary moving!")
            return
        }

        CasualUHC.logger.info("Completed boundary moving phase ${current::class.java.simpleName}!")

        val cooldown = current.getCooldown(uhc.settings.borderTime)
        if (current == UHCBoundaryPhase.Fifth) {
            uhc.scheduler.schedule(0.Ticks, GlowingCountdownRoutine(cooldown))
        }

        uhc.onPauseBoundaryTimer()
        uhc.scheduler.schedule(cooldown, BoundaryResumeRoutine(current))
    }

    private fun move(uhc: UHCMinigame, duration: MinecraftTimeDuration, getter: (ServerLevel) -> SizeAndCenter) {
        for (level in uhc.levels) {
            val boundary = level.levelBoundary
            if (boundary == null) {
                CasualUHC.logger.warn("Dimension ${level.dimension().toIdString()} had no boundary to move!")
                continue
            }

            val target = getter.invoke(level)
            val modified = this.calculateSizeAndCenter(uhc, level, target)
            if (modified.size != boundary.getSize() || modified.center != boundary.getCenter()) {
                boundary.resize(modified.size, duration)
                boundary.recenter(modified.center, duration)
            }
        }
    }

    private fun createLevelBoundary(uhc: UHCMinigame, level: ServerLevel): LevelBoundary {
        val phase = UHCBoundaryPhase.First
        val start = this.calculateSizeAndCenter(uhc, level, phase.getStart(level))
        val shape = AxisAlignedBoundaryShape(start.aabb())
        val renderer = AxisAlignedDisplayBoundaryRenderer.Factory(AxisAlignedModelRenderOptions.CUBOID_SHADER)
        return LevelBoundary(level, shape, renderer)
    }

    private fun clampBoundarySize(uhc: UHCMinigame, level: ServerLevel, size: Vec3): Vec3 {
        val final = this.getFinalPhase(uhc, level)
        val bounds = final.getEndSize(level)
        return MathUtils.max(size, bounds)
    }
}