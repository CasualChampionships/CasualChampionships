package net.casual.championships.common.event

import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.championships.common.anticheat.CasualAntiCheat
import net.minecraft.server.level.ServerPlayer

data class PlayerCheatEvent(
    override val player: ServerPlayer,
    val type: CasualAntiCheat.Type
): PlayerEvent