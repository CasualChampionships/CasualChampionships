package net.casual.events.player

import net.casual.arcade.events.core.Event
import net.casual.extensions.PlayerFlag
import net.minecraft.server.level.ServerPlayer

class PlayerFlagEvent(
    val player: ServerPlayer,
    val flag: PlayerFlag,
    val value: Boolean
): Event