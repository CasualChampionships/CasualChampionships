package net.casual.championships.minigame.lobby

import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.utils.MathUtils
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import java.util.function.Predicate

@Suppress("NonExtendableApiUsage")
class EmptyLobbyArea(override val level: ServerLevel): PlaceableArea {
    override fun getBoundingBox(): BoundingBox {
        return BoundingBox(BlockPos.ZERO)
    }

    override fun getEntityBoundingBox(): AABB {
        return MathUtils.above(0.0)
    }

    override fun place(): Boolean {
        return true
    }

    override fun removeBlocks() {

    }

    override fun removeEntities(predicate: Predicate<Entity>) {

    }
}