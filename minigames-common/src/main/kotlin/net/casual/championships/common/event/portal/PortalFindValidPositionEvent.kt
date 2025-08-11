package net.casual.championships.common.event.portal

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

data class PortalFindValidPositionEvent(
    override val level: ServerLevel,
    val pos: BlockPos,
    var valid: Boolean
): LevelEvent {
    inline fun and(condition: () -> Boolean) {
        this.valid = this.valid && condition.invoke()
    }
}