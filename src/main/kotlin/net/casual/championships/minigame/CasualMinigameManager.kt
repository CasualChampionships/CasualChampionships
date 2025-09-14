package net.casual.championships.minigame

import com.mojang.authlib.GameProfile
import com.mojang.serialization.DataResult
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.player.PlayerChatEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.server.player.PlayerRequestLoginEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.MinigameCloseEvent
import net.casual.arcade.minigame.events.MinigameCompleteEvent
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.minigame.utils.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.scheduler.coroutine.launch
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.PlayerUtils.getChatUsername
import net.casual.arcade.utils.TeamUtils.getOrCreateTeam
import net.casual.arcade.utils.TeamUtils.setHexColor
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.red
import net.casual.championships.CasualChampionships
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.PerformanceUtils
import net.casual.championships.duel.minigame.DuelMinigame
import net.casual.championships.minigame.event.EventConfiguration
import net.casual.championships.minigame.event.EventState
import net.casual.championships.minigame.lobby_v2.CasualLobbyMinigame
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.sync.CasualSyncService
import net.casual.championships.sync.data.SyncableParticipants
import net.casual.championships.sync.data.SyncableTeam
import net.casual.championships.sync.syncMinigame
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.players.UserWhiteListEntry
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KProperty0

class CasualMinigameManager(
    sync: KProperty0<CasualSyncService>,
    private val path: Path
) {
    private val winners = LinkedHashSet<String>()

    private val sync by sync

    private lateinit var config: EventConfiguration

    private lateinit var lobby: Minigame
    private var minigame: Minigame? = null

    /**
     * Whether non-operator players are able to join.
     */
    var floodgates = false

    /**
     * The name of the current event.
     */
    val event: String
        get() = this.config.name

    /**
     * The current minigame being played.
     * This may be the [lobby] minigame.
     */
    val current: Minigame
        get() = this.getCurrentMinigame()

    fun returnToLobby() {
        val current = this.current
        if (current != this.lobby) {
            current.players.transferTo(this.lobby)
            current.close()
        }
    }

    fun reloadResources() {
        this.current.resources.sendTo(this.current.players)
    }

    internal fun registerEvents(registry: ListenerRegistry) {
        registry.register<PlayerRequestLoginEvent>(::onPlayerRequestLogin)
        registry.register<MinigameInitializeEvent>(::onMinigameInitialize)
        registry.register<PlayerJoinEvent>(phase = PlayerJoinEvent.PHASE_INITIALIZED, listener = ::onPlayerJoinEarly)
        registry.register<PlayerJoinEvent>(::onPlayerJoin)
        registry.register<PlayerChatEvent>(::onPlayerChat)
        registry.register<ServerSaveEvent>(::onServerSave)
    }

    // This should be called *after* minigames have been loaded
    // so the state can reload minigames if necessary
    internal fun load(server: MinecraftServer) {
        // TODO: - We need to set whitelist
        //       - Set locator bar gamerule
        //       - Set MOTD
        server.launch { createTeams(server) }

        this.reloadConfiguration()
        this.reloadState()

        this.lobby = this.createLobby(server)
    }

    internal fun reload(server: MinecraftServer) {
        this.reloadPlayers(server)
        this.reloadConfiguration()
        this.reloadLobby(server)
    }

    private fun reloadPlayers(server: MinecraftServer) {
        server.launch { reloadTeams(server) }
        server.launch { reloadWhitelist(server) }
    }

    private fun reloadConfiguration() {
        this.config = this.readEventConfig()
    }

    private fun reloadState() {
        val state = this.readEventState() ?: return
        val minigameUUID = state.minigameUUID.getOrNull() ?: return
        val minigame = Minigames.get(minigameUUID)
        if (minigame == null) {
            ArcadeUtils.logger.error("Failed to reload minigame manager state!")
            ArcadeUtils.logger.error("Minigame $minigameUUID doesn't exist!")
            return
        }
        this.minigame = minigame
    }

    private fun reloadLobby(server: MinecraftServer) {
        val previous = this.lobby
        this.lobby = this.createLobby(server)
        previous.players.transferTo(this.lobby)
        previous.close()
    }

    private fun createLobby(server: MinecraftServer): CasualLobbyMinigame {
        return CasualLobbyMinigame.create(this.config.lobby, this::minigame, MinigameCreationContext(server))
    }

    private suspend fun createTeams(server: MinecraftServer) {
        val teams = this.sync.getTeams()
        val scoreboard = server.scoreboard
        for (team in scoreboard.playerTeams.toList()) {
            scoreboard.removePlayerTeam(team)
        }

        // TODO: - Set admin/spectator teams
        //       - Update spectating admin status for players

        val updated = ArrayList<PlayerTeam>(teams.size)
        for (team in teams) {
            val playerTeam = scoreboard.getOrCreateTeam(team.name)
            this.syncTeamPlayers(scoreboard, playerTeam, team)
            playerTeam.playerPrefix = team.prefix
            playerTeam.setHexColor(team.color)
            playerTeam.isAllowFriendlyFire = false
            playerTeam.collisionRule = Team.CollisionRule.ALWAYS
            updated.add(playerTeam)
        }

        if (CasualResourcePackHost.loadTeamColors(updated)) {
            this.reloadResources()
        }
    }

    private suspend fun reloadTeams(server: MinecraftServer) {
        val teams = this.sync.getTeams()
        val scoreboard = server.scoreboard
        for (team in teams) {
            val playerTeam = scoreboard.getPlayerTeam(team.name) ?: continue
            this.syncTeamPlayers(scoreboard, playerTeam, team)
        }
    }

    private fun syncTeamPlayers(
        scoreboard: Scoreboard,
        playerTeam: PlayerTeam,
        syncableTeam: SyncableTeam
    ) {
        for (member in playerTeam.players.toList()) {
            scoreboard.removePlayerFromTeam(member, playerTeam)
        }
        for (player in syncableTeam.members) {
            scoreboard.addPlayerToTeam(player, playerTeam)
        }
    }

    private suspend fun reloadWhitelist(server: MinecraftServer) {
        val participants = this.sync.getParticipants()
        val whitelist = server.playerList.whiteList
        if (participants is SyncableParticipants.Strict) {
            for (entry in whitelist.entries.toList()) {
                server.playerList.whiteList.remove(entry)
            }
        }

        val previous = whitelist.userList.toSet()
        val added = HashSet<String>()
        val removed = previous - participants.profiles.map(GameProfile::getName).toSet()
        for (profile in participants.profiles) {
            whitelist.add(UserWhiteListEntry(profile))
            if (!previous.contains(profile.name)) {
                added.add(profile.name)
            }
        }
        if (added.isEmpty() && removed.isEmpty()) {
            return
        }
        val component = Component {
            val builder = literal("Reloaded whitelist.")
            if (added.isNotEmpty()) {
                builder + nl + literal("Added: ").green() + added.joinToString()
            }
            if (removed.isNotEmpty()) {
                builder + nl + literal("Removed: ").red() + removed.joinToString()
            }
            builder.withMiniFont()
        }
        this.current.chat.broadcastInfo(component, this.current.players.admins)
    }

    private fun onPlayerRequestLogin(event: PlayerRequestLoginEvent) {
        val profile = event.profile
        if (event.isAccepted && !this.floodgates && !event.server.playerList.isOp(profile)) {
            event.deny(Component.literal("CasualChampionships isn't quite ready yet..."))
        }
        if (!event.isAccepted) {
            val notification = Component.literal("${profile.name} tried to join, but was denied because: ") + event.reason!!
            this.current.chat.broadcastInfo(notification, players = this.current.players.admins)
        }
    }

    private fun onMinigameInitialize(event: MinigameInitializeEvent) {
        when (val minigame = event.minigame) {
            is UHCMinigame -> this.modifyUHCMinigame(minigame)
            is DuelMinigame -> this.modifyDuelMinigame(minigame)
        }
    }

    private fun onPlayerJoinEarly(event: PlayerJoinEvent) {
        event.joinMessageModification = PlayerJoinEvent.JoinMessageModification.Delay
    }

    private fun onPlayerJoin(event: PlayerJoinEvent) {
        this.current.players.add(event.player)
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        event.format { it.copy(username = event.player.getChatUsername(false)) }
    }

    @Suppress("unused_parameter")
    private fun onServerSave(event: ServerSaveEvent) {
        this.writeEventState()
    }

    private fun modifyUHCMinigame(minigame: UHCMinigame) {
        PerformanceUtils.reduceMinigameMobcap(minigame)
        PerformanceUtils.disableEntityAI(minigame)
        CasualGuiUtils.setMinigameUI(minigame)

        minigame.resources.add(
            MinigameResources.of(CasualResourcePackHost.uhc.toPackInfo(!CasualChampionships.config.dev))
        )
        minigame.resources.add(
            MinigameResources.of(CasualResourcePackHost.boundary.toPackInfo(!CasualChampionships.config.dev))
        )

        this.registerSyncMinigameStats(minigame)
        minigame.events.register<MinigameCloseEvent> {
            this.returnToLobby()
        }
        minigame.events.register<MinigameCompleteEvent> {
            this.winners.clear()
            this.winners.addAll(minigame.winners)
        }

        minigame.settings.replay = !CasualChampionships.config.dev
    }

    private fun modifyDuelMinigame(minigame: DuelMinigame) {
        this.registerSyncMinigameStats(minigame)
        // TODO: This should be handled by the lobby
        // minigame.events.register<MinigameCloseEvent> {
        //     minigame.players.transferTo(this.minigame)
        // }
        CasualGuiUtils.setMinigameUI(minigame)
        minigame.ui.setPlayerListDisplay(CasualGuiUtils.createSimpleTabDisplay(minigame))

        minigame.resources.add(CasualResourcePackHost.createResourcesFromPacks {
            minigame.duelArena.data.packs
        })
    }

    private fun registerSyncMinigameStats(minigame: Minigame) {
        minigame.events.register<MinigameCompleteEvent> {
            minigame.server.launch { sync.syncMinigame(minigame) }
        }
    }

    private fun getCurrentMinigame(): Minigame {
        val minigame = this.minigame ?: return this.lobby
        if (minigame.started && !minigame.closed) {
            return minigame
        }
        return this.lobby
    }

    private fun readEventConfig(): EventConfiguration {
        return when (val result = JsonUtils.decodeWith(EventConfiguration.CODEC, this.path.resolve("config.json"))) {
            is DataResult.Success -> result.value
            is DataResult.Error -> {
                ArcadeUtils.logger.error("Failed to read event config: ${result.message()}")
                result.partialValue.orElseGet(::EventConfiguration)
            }
        }
    }

    private fun readEventState(): EventState? {
        return when (val result = JsonUtils.decodeWith(EventState.CODEC, this.path.resolve("state.json"))) {
            is DataResult.Success -> result.value
            is DataResult.Error -> {
                ArcadeUtils.logger.error("Failed to read event state: ${result.message()}")
                result.partialValue.getOrNull()
            }
        }
    }

    private fun writeEventState() {
        val state = EventState(Optional.ofNullable(this.minigame?.uuid))
        JsonUtils.encodeWith(state, EventState.CODEC, this.path.resolve("state.json"))
    }
}