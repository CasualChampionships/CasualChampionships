package net.casual.championships.sync.data

import com.mojang.authlib.GameProfile
import net.casual.arcade.minigame.stats.StatTracker
import net.casual.championships.sync.CasualSyncService.SyncableTeam
import net.minecraft.advancements.AdvancementHolder

data class SyncablePlayer(
    val profile: GameProfile,
    val team: SyncableTeam,
    val stats: StatTracker,
    val advancements: List<AdvancementHolder>
)