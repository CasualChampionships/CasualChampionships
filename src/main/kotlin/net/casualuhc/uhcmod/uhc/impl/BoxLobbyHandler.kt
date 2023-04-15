package net.casualuhc.uhcmod.uhc.impl

import net.casualuhc.arcade.math.Location
import net.casualuhc.uhcmod.uhc.handlers.LobbyHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class BoxLobbyHandler(
    private val center: Vec3i,
    private val radius: Int,
    private val height: Int,
    private val level: ServerLevel,
    private val block: Block = Blocks.BARRIER
): LobbyHandler {
    private val spawn = Location(this.level, Vec3.atCenterOf(this.center.above(2)), Vec2(0.0F, 0.0F))

    override fun getLevel(): ServerLevel {
        return this.level
    }

    override fun getSpawnLocation(): Location {
        return this.spawn
    }

    override fun getBoundingBox(): BoundingBox {
        return BoundingBox(
            this.center.x - this.radius,
            this.center.y,
            this.center.z - this.radius,
            this.center.x + this.radius,
            this.center.y + this.height,
            this.center.z + this.radius
        )
    }

    override fun place() {
        val box = this.getBoundingBox()
        val barrier = this.block.defaultBlockState()
        BlockPos.betweenClosedStream(box).filter { p ->
            p.x == box.minX() || p.x == box.maxX() || p.y == box.minY() || p.y == box.maxY() || p.z == box.minZ() || p.z == box.maxZ()
        }.forEach { pos ->
            this.level.setBlock(pos, barrier, 3)
        }
    }
}