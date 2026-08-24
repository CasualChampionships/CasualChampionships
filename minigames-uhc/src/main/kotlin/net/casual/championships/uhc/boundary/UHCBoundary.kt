package net.casual.championships.uhc.boundary

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.LevelBoundary.SizeAndCenter
import net.casual.arcade.boundary.renderer.AxisAlignedDisplayBoundaryRenderer
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.shape.AxisAlignedBoundaryShape
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.MathUtils.isAbove
import net.casual.arcade.utils.MathUtils.isBelow
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.player.sendTitle
import net.casual.arcade.utils.registries.toIdString
import net.casual.arcade.utils.shapes.ShapePoints.Companion.drawAsParticlesFor
import net.casual.arcade.utils.shapes.impl.ArrowShape
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.championships.common.event.portal.EntityPortalEntryPositionEvent
import net.casual.championships.common.event.portal.PortalCreateValidPositionEvent
import net.casual.championships.common.event.portal.PortalFindValidPositionEvent
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.common.util.CasualSounds
import net.casual.championships.uhc.CasualUHC
import net.casual.championships.uhc.minigame.GAME_OVER_ID
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.routine.GlowingCountdownRoutine
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.HitResult
import kotlin.math.atan2

class UHCBoundary(
    private val uhc: UHCMinigame
): MinigameEventListener {
    var phase: UHCBoundaryPhase = UHCBoundaryPhase.First
        private set

    private var lastPhaseChange = 0.Ticks

    fun reset() {
        for (level in this.uhc.levels) {
            level.levelBoundary = this.createBoundary(level)
        }
    }

    fun start() {
        this.startAfter(MinecraftTimeDuration.ZERO)
    }

    fun startAfter(delay: MinecraftTimeDuration) {
        this.resetTimer()
        this.uhc.scheduler.schedule(0.Ticks, UHCBoundaryRoutine(delay))
    }

    fun getFinalPhase(level: ServerLevel): UHCBoundaryPhase {
        return when (level) {
            this.uhc.nether -> UHCBoundaryPhase.Fourth
            this.uhc.end -> UHCBoundaryPhase.Third
            else -> UHCBoundaryPhase.Sixth
        }
    }

    fun isFinal(level: ServerLevel): Boolean {
        return this.getFinalPhase(level) <= this.phase
    }

    fun getShrinkSpeed(level: ServerLevel): Double {
        return this.phase.getSpeedInBlocksPerTick(level)
    }

    fun getSizeAndCenter(level: ServerLevel, target: SizeAndCenter): SizeAndCenter {
        val clamped = MathUtils.max(target.size, this.getFinalPhase(level).getEndSize(level))
        val scale = 1 / level.dimensionType().coordinateScale
        val sizeScale = this.uhc.settings.borderSizeMultiplier * scale
        val size = clamped.multiply(sizeScale, 1.0, sizeScale)
        val center = target.center.multiply(scale, 1.0, scale)
        return SizeAndCenter(size, center)
    }

    fun getTimeUntilPause(): MinecraftTimeDuration {
        return this.phase.getDuration(this.uhc.settings.borderTime) - this.elapsed()
    }

    fun getTimeUntilMove(): MinecraftTimeDuration {
        val cooldown = when {
            this.uhc.uptime.Ticks <= this.uhc.settings.borderStartDelay -> this.uhc.settings.borderStartDelay
            else -> this.phase.getCooldown(this.uhc.settings.borderTime)
        }
        return cooldown - this.elapsed()
    }

    fun serialize(output: ValueOutput) {
        output.store("phase", UHCBoundaryPhase.CODEC, this.phase)
        output.store("last_phase_change", MinecraftTimeDuration.CODEC, this.lastPhaseChange)
    }

    fun deserialize(input: ValueInput) {
        this.phase = input.read("phase", UHCBoundaryPhase.CODEC).orElse(this.phase)!!
        this.lastPhaseChange = input.read("last_phase_change", MinecraftTimeDuration.CODEC).orElse(this.lastPhaseChange)!!
    }

    internal fun onStarted() {
        this.resetTimer()
        this.uhc.chat.broadcastGame(component = CasualComponents.BORDER_STARTED.withMiniFont().red())
    }

    internal fun moveTo(phase: UHCBoundaryPhase) {
        this.phase = phase
        this.move(MinecraftTimeDuration.ZERO, phase::getStart)
        this.move(phase.getDuration(this.uhc.settings.borderTime), phase::getEnd)
    }

    internal fun onPaused(phase: UHCBoundaryPhase) {
        if (phase == UHCBoundaryPhase.Fifth) {
            val cooldown = phase.getCooldown(this.uhc.settings.borderTime)
            this.uhc.scheduler.schedule(0.Ticks, GlowingCountdownRoutine(cooldown))
        }

        this.resetTimer()
        this.uhc.chat.broadcastGame(component = CasualComponents.BORDER_PAUSED.withMiniFont().red())
    }

    internal fun onResumed() {
        this.resetTimer()
        this.uhc.chat.broadcastGame(
            component = CasualComponents.BORDER_RESUMED.withMiniFont().red(),
            sound = Sound(CasualSounds.GAME_BORDER_MOVING)
        )
    }

    internal fun onCompleted() {
        CasualUHC.logger.info("Completed boundary moving!")
    }

    @Listener(during = During(before = GAME_OVER_ID))
    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event
        if (this.uhc.players.isSpectating(player)) {
            return
        }

        val level = player.level()
        val boundary = level.levelBoundary ?: return

        val position = player.position()
        if (boundary.contains(position)) {
            return
        }

        val box = boundary.getAABB()
        when {
            box.isAbove(position) -> this.showReturnDirection(player, CasualComponents.direction(Direction.DOWN))
            box.isBelow(position) -> this.showReturnDirection(player, CasualComponents.direction(Direction.UP))
            else -> this.showReturnPath(player, level, boundary)
        }
    }

    private fun showReturnDirection(player: ServerPlayer, direction: MutableComponent) {
        if (this.uhc.uptime % 200 == 0) {
            player.sendTitle(
                Component.empty(),
                CasualComponents.INSIDE_BORDER.generate(direction.lime()).withMiniFont()
            )
        }
    }

    private fun showReturnPath(player: ServerPlayer, level: ServerLevel, boundary: LevelBoundary) {
        val vector = boundary.getDirectionFrom(player.eyePosition)

        val start = player.eyePosition.add(0.0, 4.0, 0.0)
        val end = start.add(vector.normalize())

        for (i in 1..2) {
            val top = start.lerp(end, 1.5 * i)
            val bottom = top.subtract(0.0, 10.0, 0.0)
            val hit = level.clip(ClipContext(top, bottom, ClipContext.Block.VISUAL, ClipContext.Fluid.SOURCE_ONLY, player))

            if (hit.type != HitResult.Type.MISS) {
                val position = hit.blockPos
                val rotation = atan2(vector.x, vector.z)

                val arrow = ArrowShape.createHorizontalCentred(
                    position.x, hit.location.y + 0.1, position.z, 1.0, rotation
                )
                arrow.drawAsParticlesFor(player, pointsPerUnit = 10.0)
            }
        }

        this.showReturnDirection(player, CasualComponents.direction(MathUtils.getDirection8(vector)))
    }

    @Listener
    private fun onEntityPortalEntryPosition(event: EntityPortalEntryPositionEvent) {
        val (level, _, pos) = event
        val boundary = level.levelBoundary ?: return

        val margin = this.getPortalMargin(level, boundary)
        if (margin == null) {
            val (x, y, z) = boundary.getCenter()
            // The boundary would reach size 0 before the player could escape it
            event.cancel(BlockPos.containing(x, y, z))
            return
        }

        val box = boundary.getAABB()
        event.cancel(BlockPos.containing(
            Mth.clamp(pos.x, box.minX + margin, box.maxX - margin),
            Mth.clamp(pos.y, box.minY + margin, box.maxY - margin),
            Mth.clamp(pos.z, box.minZ + margin, box.maxZ - margin)
        ))
    }

    @Listener
    private fun onPortalCreateValidPosition(event: PortalCreateValidPositionEvent) {
        val (level, position) = event
        val boundary = level.levelBoundary ?: return
        event.and { this.isValidPortalPosition(level, position, boundary) }
    }

    @Listener
    private fun onPortalFindValidPosition(event: PortalFindValidPositionEvent) {
        val (level, position) = event
        val boundary = level.levelBoundary ?: return
        event.and { this.isValidPortalPosition(level, position, boundary) }
    }

    private fun getPortalMargin(level: ServerLevel, boundary: LevelBoundary): Double? {
        val speed = this.getShrinkSpeed(level)
        if (speed <= 0) {
            return 0.0
        }
        val margin = speed * this.uhc.settings.portalEscapeTime.ticks
        if (margin >= boundary.getSize().x * 0.5) {
            return null
        }
        return margin
    }

    private fun isValidPortalPosition(level: ServerLevel, position: BlockPos, boundary: LevelBoundary): Boolean {
        val speed = this.getShrinkSpeed(level)
        if (speed <= 0) {
            // The boundary is static or expanding
            return boundary.contains(position) == BoundaryShape.Containment.Full
        }

        val margin = speed * this.uhc.settings.portalEscapeTime.ticks
        val size = boundary.getSize()
        val box = boundary.getAABB()
        val x = margin.coerceAtMost(size.x * 0.5 - 1)
        val y = margin.coerceAtMost(size.y * 0.5 - 1)
        val z = margin.coerceAtMost(size.z * 0.5 - 1)
        return position.x >= box.minX + x
            && position.x + 1 <= box.maxX - x
            && position.y >= box.minY + y
            && position.y + 1 <= box.maxY - y
            && position.z >= box.minZ + z
            && position.z + 1 <= box.maxZ - z
    }

    private fun resetTimer() {
        this.lastPhaseChange = this.uhc.uptime.Ticks
    }

    private fun elapsed(): MinecraftTimeDuration {
        return this.uhc.uptime.Ticks - this.lastPhaseChange
    }

    private fun move(duration: MinecraftTimeDuration, getter: (ServerLevel) -> SizeAndCenter) {
        for (level in this.uhc.levels) {
            val boundary = level.levelBoundary
            if (boundary == null) {
                CasualUHC.logger.warn("Dimension ${level.dimension().toIdString()} had no boundary to move!")
                continue
            }

            val target = this.getSizeAndCenter(level, getter.invoke(level))
            if (target.size != boundary.getSize() || target.center != boundary.getCenter()) {
                boundary.resize(target.size, duration)
                boundary.recenter(target.center, duration)
            }
        }
    }

    private fun createBoundary(level: ServerLevel): LevelBoundary {
        val start = this.getSizeAndCenter(level, UHCBoundaryPhase.First.getStart(level))
        val shape = AxisAlignedBoundaryShape(start.aabb())
        val renderer = AxisAlignedDisplayBoundaryRenderer.Factory(AxisAlignedModelRenderOptions.CUBOID_SHADER)
        return LevelBoundary(level, shape, renderer)
    }
}
