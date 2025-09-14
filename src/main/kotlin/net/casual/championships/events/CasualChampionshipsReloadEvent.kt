package net.casual.championships.events

import net.casual.arcade.events.common.Event
import net.casual.championships.config.CasualConfig
import net.minecraft.server.MinecraftServer

@java.lang.Deprecated
data class CasualChampionshipsReloadEvent(
    val server: MinecraftServer,
    val config: CasualConfig
): Event