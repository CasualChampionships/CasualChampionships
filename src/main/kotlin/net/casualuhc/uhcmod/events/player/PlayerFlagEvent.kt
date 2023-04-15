package net.casualuhc.uhcmod.events.player

import net.casualuhc.arcade.events.core.Event
import net.casualuhc.uhcmod.extensions.PlayerFlag
import net.minecraft.server.level.ServerPlayer

class PlayerFlagEvent(
    val player: ServerPlayer,
    val flag: PlayerFlag,
    val value: Boolean
): Event()