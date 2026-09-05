package net.casual.championships.common.event

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.common.ServerSideEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

data class DoesWaypointIgnoreReceiverEvent(
    val waypoint: LivingEntity,
    val receiver: ServerPlayer
): CancellableEvent.WithResult<Boolean>(), ServerSideEvent
