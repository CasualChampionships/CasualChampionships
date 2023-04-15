package net.casualuhc.uhcmod.uhc.handlers

import net.casualuhc.arcade.math.Location
import net.casualuhc.arcade.utils.PlayerUtils.teleportTo
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Clearable
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import java.util.*

interface LobbyHandler {
    fun getLevel(): ServerLevel

    fun getSpawnLocation(): Location

    fun place()

    fun getBoundingBox(): BoundingBox

    fun remove() {
        val air = Blocks.AIR.defaultBlockState()
        val level = this.getLevel()
        BlockPos.betweenClosedStream(this.getBoundingBox()).forEach { pos ->
            val blockEntity = level.getBlockEntity(pos)
            Clearable.tryClear(blockEntity)
            level.setBlock(pos, air, Block.UPDATE_CLIENTS or Block.UPDATE_SUPPRESS_DROPS, 0)
        }

        val box = this.getPlayerBoundingBox()
        val entities = LinkedList<Entity>()
        for (entity in level.allEntities) {
            if (entity.tags.contains("uhc") || (entity !is Player && box.contains(entity.position()))) {
                entities.add(entity)
            }
        }
        for (entity in entities) {
            entity.kill()
        }
    }

    fun getPlayerBoundingBox(): AABB {
        val lobbyBox = this.getBoundingBox()
        return AABB(
            lobbyBox.minX() - PLAYER_SPACE,
            lobbyBox.minY() - PLAYER_SPACE,
            lobbyBox.minZ() - PLAYER_SPACE,
            lobbyBox.maxX() + PLAYER_SPACE,
            lobbyBox.maxY() + PLAYER_SPACE,
            lobbyBox.maxZ() + PLAYER_SPACE
        )
    }

    fun forceTeleport(player: ServerPlayer) {
        player.teleportTo(this.getSpawnLocation())
    }

    fun tryTeleport(player: ServerPlayer): Boolean {
        if (!this.getPlayerBoundingBox().contains(player.position())) {
            this.forceTeleport(player)
            return true
        }
        return false
    }

    companion object {
        private const val PLAYER_SPACE = 20.0
    }
}