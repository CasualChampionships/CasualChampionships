package net.casualuhc.uhcmod.util

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.level.LevelBlockChangedEvent
import net.casualuhc.arcade.events.level.LevelCreatedEvent
import net.casualuhc.arcade.events.level.LevelTickEvent
import net.casualuhc.arcade.events.player.PlayerBlockPlacedEvent
import net.casualuhc.arcade.utils.LevelUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.grantAdvancement
import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.extensions.WorldBlockTrackerExtension
import net.casualuhc.uhcmod.extensions.WorldBlockTrackerExtension.Companion.blockTracker
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.PlaceOnWaterBlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext

object AntiCheat {
    internal fun registerEvents() {
        EventHandler.register<LevelCreatedEvent> { it.level.addExtension(WorldBlockTrackerExtension()) }
        EventHandler.register<LevelTickEvent> { it.level.blockTracker.tick() }
        EventHandler.register<LevelBlockChangedEvent> { this.onBlockChanged(it) }
        EventHandler.register<PlayerBlockPlacedEvent> { this.onPlayerBlockPlaced(it) }
    }

    private fun onBlockChanged(event: LevelBlockChangedEvent) {
        if (Arcade.server.isSameThread && event.old != event.new)  {
            event.level.blockTracker.track(event.pos, event.old)
        }
    }

    private fun onPlayerBlockPlaced(event: PlayerBlockPlacedEvent) {
        if (this.detectFlexibleBlockPlacement(event.player, event.context)) {
            event.player.grantAdvancement(UHCAdvancements.BUSTED)
            val message = Component.literal("Player ").append(event.player.displayName).append(" used fbp")
            PlayerUtils.broadcastToOps(message)
            // Might create ghost blocks?
            event.cancel()
        }
    }

    private fun detectFlexibleBlockPlacement(player: ServerPlayer, context: BlockPlaceContext): Boolean {
        if (player.hasPermissions(4)) {
            return false
        }

        val pos = context.clickedPos
        val level = context.level as ServerLevel

        val state = level.getBlockState(pos)

        var couldBePlacedOffset = isSolidForPlacement(context, state)
        var couldBePlacedHere = !couldBePlacedOffset
        for (olderState in level.blockTracker.getTrackedStatesInLastNTicks(pos, 20)) {
            val isSolid = isSolidForPlacement(context, olderState)
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
            for (olderState in level.blockTracker.getTrackedStatesInLastNTicks(pos, 20)) {
                if (!olderState.getShape(level, pos, shapeContext).isEmpty) {
                    return false
                }
            }
        }

        if (couldBePlacedOffset) {
            possible.add(pos.relative(context.clickedFace))
        }

        for (placedPos in possible) {
            for (direction in Direction.values()) {
                val neighbor: BlockState = level.getBlockState(placedPos.relative(direction))
                if (isSolidForPlacement(context, neighbor)) {
                    return false
                }
                for (olderNeighbor in level.blockTracker.getTrackedStatesInLastNTicks(placedPos.relative(direction), 20)) {
                    if (isSolidForPlacement(context, olderNeighbor)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun isSolidForPlacement(context: BlockPlaceContext, state: BlockState): Boolean {
        if (state.canBeReplaced(context)) {
            val fluidsSolid = context.itemInHand.item is PlaceOnWaterBlockItem
            return fluidsSolid && state.fluidState.isSource
        }
        return true
    }
}