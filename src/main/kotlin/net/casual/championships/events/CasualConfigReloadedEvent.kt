package net.casual.championships.events

import net.casual.arcade.events.common.Event
import net.casual.championships.util.CasualConfig
import net.minecraft.server.MinecraftServer

data class CasualConfigReloadedEvent(
    val server: MinecraftServer,
    val config: CasualConfig
): Event