package net.casual.championships.sync

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.casual.arcade.minigame.stats.ArcadeStats
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.championships.common.util.CasualStats
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.config.DatabaseLogin
import net.casual.championships.sync.data.SyncableMinigame
import net.casual.championships.sync.data.SyncableParticipants
import net.casual.championships.sync.data.SyncableTeam
import net.casual.championships.uhc.utils.UHCStats
import net.casual.database.*
import net.casual.database.stats.DuelPlayerStats
import net.casual.database.stats.PlayerStats
import net.casual.database.stats.UHCPlayerStats
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.dao.IntEntityClass
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.time.ExperimentalTime
import net.casual.database.Minigame as DatabaseMinigame

class CasualDatabaseSyncService(
    private val current: Event,
    private val database: CasualDatabase
): CasualSyncService {
    override suspend fun getParticipants(): SyncableParticipants = transaction {
        val profiles = buildSet {
            for (player in database.getDiscordPlayers()) {
                if (player.team != null) {
                    add(GameProfile(player.id.value, player.name))
                }
            }
        }
        SyncableParticipants.Strict(profiles)
    }

    override suspend fun getTeams(): List<SyncableTeam> = transaction {
        val teams = this.database.getDiscordTeams()
        buildList {
            for (team in teams) {
                val prefix = Component.literal("[${team.prefix}] ")
                val syncable = SyncableTeam(team.name, prefix, team.color, team.players.map { it.name })
                add(syncable)
            }
        }
    }

    override suspend fun syncUHC(minigame: SyncableMinigame): Boolean = transaction {
        CasualUtils.logger.info("Synchronizing uhc stats for ${minigame.uuid}")

        val databaseMinigame = this.getOrCreateMinigame(minigame)
        for ((profile, team, stats, advancements) in minigame.players) {
            val player = this.getOrCreateMinigamePlayer(profile.id, team, databaseMinigame)

            this.syncPlayerAdvancements(minigame, player, advancements)
            this.getOrCreatePlayerStats(UHCPlayerStats, player) {
                won = stats.getStatValueOrDefault(CasualStats.WON)
                died = stats.getStatValueOrDefault(ArcadeStats.DEATHS) > 0
                kills = stats.getStatValueOrDefault(ArcadeStats.KILLS)
                damageTaken = stats.getStatValueOrDefault(ArcadeStats.DAMAGE_TAKEN)
                damageDealt = stats.getStatValueOrDefault(ArcadeStats.DAMAGE_DEALT)
                damageHealed = stats.getStatValueOrDefault(ArcadeStats.DAMAGE_HEALED)
                headsConsumed = stats.getStatValueOrDefault(UHCStats.HEADS_CONSUMED)
                aliveTime = stats.getStatValueOrDefault(CasualStats.ALIVE_TIME).Ticks.duration
                crouchTime = stats.getStatValueOrDefault(CasualStats.CROUCH_TIME).Ticks.duration
                jumps = stats.getStatValueOrDefault(CasualStats.JUMPS)
                relogs = stats.getStatValueOrDefault(ArcadeStats.RELOGS)
                blocksMined = stats.getStatValueOrDefault(CasualStats.BLOCKS_MINED)
                blocksPlaced = stats.getStatValueOrDefault(CasualStats.BLOCKS_PLACED)
            }
        }
        true
    }

    override suspend fun syncDuel(minigame: SyncableMinigame): Boolean = transaction {
        CasualUtils.logger.info("Synchronizing duel stats for ${minigame.uuid}")
        val databaseMinigame = this.getOrCreateMinigame(minigame)
        for ((profile, team, stats, advancements) in minigame.players) {
            val player = this.getOrCreateMinigamePlayer(profile.id, team, databaseMinigame)

            this.syncPlayerAdvancements(minigame, player, advancements)
            this.getOrCreatePlayerStats(DuelPlayerStats, player) {
                won = stats.getStatValueOrDefault(CasualStats.WON)
                kills = stats.getStatValueOrDefault(ArcadeStats.KILLS)
                damageTaken = stats.getStatValueOrDefault(ArcadeStats.DAMAGE_TAKEN)
                damageDealt = stats.getStatValueOrDefault(ArcadeStats.DAMAGE_DEALT)
                damageHealed = stats.getStatValueOrDefault(ArcadeStats.DAMAGE_HEALED)
            }
        }
        true
    }

    override fun close() {
        this.database.close()
    }

    private suspend fun <T> transaction(statement: (Transaction) -> T) = withContext(Dispatchers.IO) {
        database.transaction(statement)
    }

    private fun syncPlayerAdvancements(
        minigame: SyncableMinigame,
        minigamePlayer: MinigamePlayer,
        advancements: List<AdvancementHolder>
    ) {
        for (holder in advancements) {
            val minigameAdvancement = this.getOrCreateAdvancement(minigame, holder) ?: continue
            MinigameAdvancementAward.new {
                advancement = minigameAdvancement
                player = minigamePlayer
            }
        }
    }

    private fun getOrCreateAdvancement(
        minigame: SyncableMinigame,
        advancement: AdvancementHolder
    ): MinigameAdvancement? {
        val type = minigame.type.toString()
        val id = advancement.id.toString()
        val minigameAdvancement = MinigameAdvancement.find {
            (MinigameAdvancements.advancementId eq id) and (MinigameAdvancements.minigameType eq type)
        }.singleOrNull()
        if (minigameAdvancement != null) {
            return minigameAdvancement
        }

        val display = advancement.value.display.getOrNull() ?: return null
        return MinigameAdvancement.new {
            advancementId = id
            minigameType = type
            displayItem = BuiltInRegistries.ITEM.getKey(display.icon.item).toString()
            title = display.title.string
        }
    }

    private fun <T: PlayerStats> getOrCreatePlayerStats(
        clazz: IntEntityClass<T>,
        player: MinigamePlayer,
        modifier: T.() -> Unit
    ) {
        val stats = clazz.findById(player.id.value)
        if (stats != null) {
            stats.modifier()
            return
        }
        clazz.new(player.id.value, modifier)
    }

    private fun getOrCreateMinigamePlayer(
        playerUUID: UUID,
        playerTeam: SyncableTeam,
        databaseMinigame: DatabaseMinigame
    ): MinigamePlayer {
        val eventPlayer = this.getOrCreateEventPlayer(playerUUID, playerTeam)
        val minigamePlayer = MinigamePlayer.find {
            (MinigamePlayers.player eq eventPlayer.id) and (MinigamePlayers.minigame eq databaseMinigame.id)
        }.singleOrNull()
        if (minigamePlayer != null) {
            return minigamePlayer
        }
        return MinigamePlayer.new {
            player = eventPlayer
            minigame = databaseMinigame
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getOrCreateMinigame(minigame: SyncableMinigame): DatabaseMinigame {
        val databaseMinigame = DatabaseMinigame.findById(minigame.uuid)
        if (databaseMinigame != null) {
            return databaseMinigame
        }
        return DatabaseMinigame.new {
            type = minigame.type.toString()
            startTime = minigame.start
            endTime = minigame.end
            event = current
        }
    }

    private fun getOrCreateEventPlayer(playerUUID: UUID, syncableTeam: SyncableTeam): EventPlayer {
        val eventTeam = this.getOrCreateEventTeam(syncableTeam)
        val player = EventPlayer.find {
            (EventPlayers.uuid eq playerUUID) and (EventPlayers.team eq eventTeam.id)
        }.singleOrNull()
        if (player != null) {
            return player
        }
        return EventPlayer.new {
            uuid = playerUUID
            team = eventTeam
        }
    }

    private fun getOrCreateEventTeam(syncableTeam: SyncableTeam): EventTeam {
        val team = EventTeam.find {
            (EventTeams.name eq syncableTeam.name) and (EventTeams.event eq current.id)
        }.singleOrNull()
        if (team != null) {
            return team
        }
        return EventTeam.new {
            name = syncableTeam.name
            event = current
            color = syncableTeam.color ?: 0xFFFFFF
        }
    }

    companion object {
        fun create(database: CasualDatabase, event: String): CasualDatabaseSyncService {
            return CasualDatabaseSyncService(database.transaction { getOrCreateEvent(event) }, database)
        }

        suspend fun create(login: DatabaseLogin, event: String): CasualDatabaseSyncService = withContext(Dispatchers.IO) {
            val database = CasualDatabase(login.url, login.username, login.password)
            create(database, event)
        }

        private fun getOrCreateEvent(name: String): Event {
            val event = Event.find { Events.name eq name }.singleOrNull()
            return event ?: Event.new { this.name = name }
        }
    }
}