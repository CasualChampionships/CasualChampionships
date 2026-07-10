package net.casual.championships.common.event.portal

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

data class EntityPortalEntryPositionEvent(
    override val level: ServerLevel,
    val entity: Entity,
    val pos: Vec3
): CancellableEvent.WithResult<BlockPos>(), LevelEvent