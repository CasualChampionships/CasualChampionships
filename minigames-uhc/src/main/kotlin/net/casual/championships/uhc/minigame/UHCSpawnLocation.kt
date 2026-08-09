package net.casual.championships.uhc.minigame

import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.utils.MathUtils.rotationAnglesTowards
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.asLocation
import net.casual.championships.uhc.utils.UHCSpreadTeleporter
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class UHCSpawnLocation(
    private val uhc: UHCMinigame
): MinigameLevelManager.SpawnLocation {
    override val overridesPlayerSpawnPoint: Boolean = true

    override fun get(player: ServerPlayer): LocationWithLevel<ServerLevel> {
        val location = this.getValidRespawnPos(player) ?:
        this.uhc.overworld.asLocation(this.uhc.overworld.levelBoundary?.getCenter() ?: Vec3.ZERO)
        val level = location.level
        val pos = location.position

        val valid = UHCSpreadTeleporter.searchForValidPosition(BlockPos.containing(pos), level)
        if (valid != null) {
            val centerPos = Vec3.atBottomCenterOf(valid)
            val boundary = level.levelBoundary
            val rotation = if (boundary != null) player.eyePosition.rotationAnglesTowards(boundary.getCenter()) else Vec2.ZERO
            return level.asLocation(centerPos, rotation)
        }

        val blockPos = BlockPos.containing(pos)
        // Force load the chunk so we can load the heightmap
        level.getChunk(blockPos)
        val y = level.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.x, blockPos.z)
        return level.asLocation(pos.with(Direction.Axis.Y, y.toDouble()))
    }

    private fun getValidRespawnPos(player: ServerPlayer): LocationWithLevel<ServerLevel>? {
        val respawnData = player.respawnConfig?.respawnData ?: return null
        val level = this.uhc.server.getLevel(respawnData.dimension()) ?: return null
        if (!this.uhc.levels.has(level)) {
            return null
        }

        val respawnPos = Vec3.atBottomCenterOf(respawnData.pos())
        val boundary = level.levelBoundary ?: return level.asLocation(respawnPos)
        if (boundary.contains(respawnPos)) {
            return level.asLocation(respawnPos)
        }

        val factor = 0.99 // nudge the player further inside the border
        val inBorderPos = boundary.shape.getDirectionFrom(respawnPos)
            .add(respawnPos).scale(factor)
        return level.asLocation(inBorderPos)
    }
}