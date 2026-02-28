package net.casual.championships.common.anticheat.fbp

import me.lucko.fabric.api.permissions.v0.Permissions
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.level.LevelBlockChangedEvent
import net.casual.arcade.events.server.player.PlayerBlockPlacedEvent
import net.casual.arcade.utils.ItemUtils.isOf
import net.casual.arcade.utils.level.isOf
import net.casual.championships.common.anticheat.AntiCheatType
import net.casual.championships.common.anticheat.fbp.WorldBlockTrackerExtension.Companion.blockTracker
import net.casual.championships.common.event.PlayerCheatEvent
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Items
import net.minecraft.world.item.PlaceOnWaterBlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext

object FlexibleBlockPlacementDetector {
    internal fun registerEvents() {
        WorldBlockTrackerExtension.registerEvents()
        GlobalEventHandler.Server.register<LevelBlockChangedEvent> { onBlockChanged(it) }
        GlobalEventHandler.Server.register<PlayerBlockPlacedEvent> { onPlayerBlockPlaced(it) }
    }

    private fun onBlockChanged(event: LevelBlockChangedEvent) {
        if (event.old != event.new)  {
            event.level.blockTracker.track(event.pos, event.old)
        }
    }

    private fun onPlayerBlockPlaced(event: PlayerBlockPlacedEvent) {
        if (this.detectFlexibleBlockPlacement(event.player, event.context)) {
            GlobalEventHandler.Server.broadcast(PlayerCheatEvent(event.player, AntiCheatType.FlexibleBlockPlacement))
            event.cancel()
        }
    }

    private fun detectFlexibleBlockPlacement(player: ServerPlayer, context: BlockPlaceContext): Boolean {
        if (Permissions.check(player, "casual.fbp", PermissionLevel.OWNERS)) {
            return false
        }

        if (context.itemInHand.isOf(Items.SCAFFOLDING)) {
            return false
        }

        val pos = context.clickedPos
        val level = context.level as ServerLevel

        val state = level.getBlockState(pos)

        var couldBePlacedOffset = this.isSolidForPlacement(context, state)
        var couldBePlacedHere = !couldBePlacedOffset
        for (olderState in level.blockTracker.getTrackedStatesInLastNTicks(pos)) {
            val isSolid = this.isSolidForPlacement(context, olderState)
            couldBePlacedOffset = couldBePlacedOffset || isSolid
            couldBePlacedHere = couldBePlacedHere || !isSolid

            if (couldBePlacedOffset && couldBePlacedHere) {
                break
            }
        }
        val possible = ArrayList<BlockPos>(2)

        if (couldBePlacedHere) {
            possible.add(pos)
            // Check for placement inside a block
            val shapeContext = CollisionContext.of(player)
            if (!state.getShape(level, pos, shapeContext).isEmpty) {
                return false
            }
            for (olderState in level.blockTracker.getTrackedStatesInLastNTicks(pos)) {
                if (!olderState.getShape(level, pos, shapeContext).isEmpty) {
                    return false
                }
            }
        }

        if (couldBePlacedOffset) {
            possible.add(pos.relative(context.clickedFace))
        }

        for (placedPos in possible) {
            for (direction in Direction.entries) {
                val neighbor = level.getBlockState(placedPos.relative(direction))
                if (this.isSolidForPlacement(context, neighbor)) {
                    return false
                }
                for (olderNeighbor in level.blockTracker.getTrackedStatesInLastNTicks(placedPos.relative(direction))) {
                    if (this.isSolidForPlacement(context, olderNeighbor)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun isSolidForPlacement(context: BlockPlaceContext, state: BlockState): Boolean {
        if (state.canBeReplaced(context) && !state.isOf(BlockTags.SLABS)) {
            val fluidsSolid = context.itemInHand.item is PlaceOnWaterBlockItem
            return fluidsSolid && state.fluidState.isSource
        }
        return true
    }
}