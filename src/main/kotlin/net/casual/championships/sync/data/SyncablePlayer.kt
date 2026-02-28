package net.casual.championships.sync.data

import net.casual.arcade.minigame.stats.StatTracker
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.players.NameAndId

data class SyncablePlayer(
    val profile: NameAndId,
    val team: SyncableTeam,
    val stats: StatTracker,
    val advancements: List<AdvancementHolder>
)