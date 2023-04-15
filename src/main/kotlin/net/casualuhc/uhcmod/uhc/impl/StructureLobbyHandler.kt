package net.casualuhc.uhcmod.uhc.impl

import net.casualuhc.arcade.math.Location
import net.casualuhc.uhcmod.uhc.handlers.LobbyHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate

class StructureLobbyHandler(
    private val lobby: StructureTemplate,
    private val spawn: Location,
    private val centre: Vec3i,
    private val level: ServerLevel
): LobbyHandler {
    override fun getLevel(): ServerLevel {
        return this.level
    }

    override fun getSpawnLocation(): Location {
        return this.spawn
    }

    override fun getBoundingBox(): BoundingBox {
        val dimensions = this.lobby.size
        val halfX = dimensions.x / 2 + 1
        val halfY = dimensions.y / 2 + 1
        val halfZ = dimensions.z / 2 + 1
        return BoundingBox(
            this.centre.x - halfX,
            this.centre.y - halfY,
            this.centre.z - halfZ,
            this.centre.x + halfX,
            this.centre.y + halfY,
            this.centre.z + halfZ
        )
    }

    override fun place() {
        val dimensions = this.lobby.size
        val halfX = dimensions.x / 2 + 1
        val halfY = dimensions.y / 2 + 1
        val halfZ = dimensions.z / 2 + 1
        val corner = BlockPos(this.centre.x - halfX, this.centre.y - halfY, this.centre.z - halfZ)
        this.lobby.placeInWorld(this.level, corner, corner, StructurePlaceSettings(), RandomSource.create(), 3)
    }
}