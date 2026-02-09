package net.casual.championships.common.event

import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState

class LevelFluidTrySpreadEvent(
    override val level: ServerLevel,
    val liquidPos: BlockPos,
    val liquidBlockState: BlockState,
    val direction: Direction,
    val spreadPos: BlockPos,
    val spreadBlockState: BlockState,
    val spreadFluidState: FluidState,
    var canSpread: Boolean
): LocatedLevelEvent {
    override val pos: BlockPos
        get() = this.liquidPos
}