package net.casualuhc.uhc.events.border

import net.casualuhc.arcade.events.core.CancellableEvent
import net.casualuhc.arcade.events.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.Vec3

data class BorderEntityPortalEntryPointEvent(
    val border: WorldBorder,
    override val level: ServerLevel,
    val entity: Entity,
    val pos: Vec3
): CancellableEvent.Typed<BlockPos>(), LevelEvent