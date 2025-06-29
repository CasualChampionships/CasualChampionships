package net.casual.championships.uhc.border

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.LevelBoundary.SizeAndCenter
import net.casual.arcade.boundary.extension.LevelBoundaryExtension.Companion.levelBoundary
import net.casual.arcade.boundary.renderer.AxisAlignedDisplayBoundaryRenderer
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.shape.AxisAlignedBoundaryShape
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.utils.toIdString
import net.casual.championships.uhc.UHCMod
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.minigame.UHCPhase
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

object UHCBoundaryManager {
    fun reset(uhc: UHCMinigame) {
        for (level in uhc.levels) {
            level.levelBoundary = this.createLevelBoundary(uhc, level)
        }
    }

    fun start(uhc: UHCMinigame) {
        uhc.onStartBoundary()
        this.move(uhc, UHCBoundaryPhase.First)
    }

    fun calculateSizeAndCenter(
        uhc: UHCMinigame,
        level: ServerLevel,
        target: SizeAndCenter
    ): SizeAndCenter {
        val scale = 1 / level.dimensionType().coordinateScale
        val size = target.size.scale(uhc.settings.borderSizeMultiplier * scale)
        val center = target.center.multiply(scale, 1.0, scale)
        return SizeAndCenter(this.clampBoundarySize(uhc, level, size), center)
    }

    private fun move(uhc: UHCMinigame, current: UHCBoundaryPhase) {
        uhc.boundaryPhase = current
        val duration = current.getDuration(uhc.settings.borderTime)
        this.move(uhc, current.end, duration)
        uhc.scheduler.schedulePhased(duration, MinigameTask(uhc) { minigame ->
            this.complete(minigame, current)
        })
    }

    private fun complete(uhc: UHCMinigame, current: UHCBoundaryPhase) {
        if (current == UHCBoundaryPhase.entries.last()) {
            uhc.setPhase(UHCPhase.BoundaryFinished)
            return
        }

        uhc.onPauseBoundary()
        val cooldown = current.getCooldown(uhc.settings.borderTime)
        uhc.scheduler.schedulePhased(cooldown, MinigameTask(uhc) { minigame ->
            uhc.onResumeBoundary()
            this.move(minigame, current.getNextStage())
        })
    }

    private fun move(uhc: UHCMinigame, target: SizeAndCenter, duration: MinecraftTimeDuration) {
        for (level in uhc.levels) {
            val boundary = level.levelBoundary
            if (boundary == null) {
                UHCMod.logger.warn("Dimension ${level.dimension().toIdString()} had no boundary to move!")
                continue
            }

            val modified = this.calculateSizeAndCenter(uhc, level, target)
            if (modified.size != boundary.getSize() || modified.center != boundary.getCenter()) {
                boundary.resize(modified.size, duration)
                boundary.recenter(modified.center, duration)
                UHCMod.logger.info("Dimension ${level.dimension().toIdString()} moving to $target")
            }
        }
    }

    private fun createLevelBoundary(uhc: UHCMinigame, level: ServerLevel): LevelBoundary {
        val phase = UHCBoundaryPhase.First
        val start = this.calculateSizeAndCenter(uhc, level, phase.start)
        val shape = AxisAlignedBoundaryShape(start.aabb())
        val renderer = AxisAlignedDisplayBoundaryRenderer(shape, AxisAlignedModelRenderOptions.CUBOID_SHADER)
        return LevelBoundary(shape, renderer)
    }

    private fun clampBoundarySize(uhc: UHCMinigame, level: ServerLevel, size: Vec3): Vec3 {
        if (level == uhc.end) {
            val bounds = UHCBoundaryPhase.Third.end.size
            return MathUtils.max(size, bounds)
        }
        return size
    }
}