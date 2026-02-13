package net.casual.championships.minigame

import com.mojang.authlib.GameProfile
import com.mojang.serialization.DataResult
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerChatEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.server.player.PlayerRequestLoginEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.MinigameAddAdminEvent
import net.casual.arcade.minigame.events.MinigameCloseEvent
import net.casual.arcade.minigame.events.MinigameCompleteEvent
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.minigame.utils.MinigameResources.Companion.sendTo
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.PlayerUtils.getChatUsername
import net.casual.arcade.utils.PlayerUtils.username
import net.casual.arcade.utils.TeamUtils.getOrCreateTeam
import net.casual.arcade.utils.TeamUtils.setHexColor
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.coroutine.launch
import net.casual.championships.CasualChampionships
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.CasualTags
import net.casual.championships.common.util.PerformanceUtils
import net.casual.championships.duel.minigame.DuelMinigame
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.minigame.event.EventConfiguration
import net.casual.championships.minigame.event.EventState
import net.casual.championships.minigame.lobby.LobbyMinigames
import net.casual.championships.minigame.lobby.LobbySidebar
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.sync.CasualNoopSyncService
import net.casual.championships.sync.data.SyncableParticipants
import net.casual.championships.sync.data.SyncableTeam
import net.casual.championships.sync.syncMinigame
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.util.CasualTeamUtils.getOrCreateAdminTeam
import net.casual.championships.util.CasualTeamUtils.getOrCreateSpectatorTeam
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.players.NameAndId
import net.minecraft.server.players.UserWhiteListEntry
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.io.path.notExists
import kotlin.jvm.optionals.getOrNull

class CasualMinigameManager(
    private val championships: CasualChampionships,
    private val path: Path
) {
    private val packs = ArrayList<PackInfo>()

    private lateinit var config: EventConfiguration

    private lateinit var lobby: LobbyMinigame
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

    /**
     * Whether the current minigame is the lobby.
     *
     * @return Whether the current minigame is the lobby.
     */
    fun isInLobby(): Boolean {
        return this.current == this.lobby
    }

    /**
     * Returns everyone back to the lobby and
     * closes the current minigame.
     *
     * This will have no effect if the current
     * minigame is the lobby.
     */
    fun returnToLobby() {
        val current = this.current
        if (current != this.lobby) {
            current.players.transferTo(this.lobby)
            current.close()

            // We need to do this later, because the first minigame
            // may not have fully closed yet...
            GlobalTickedScheduler.later {
                this.reloadMinigame(this.lobby.server)
            }
        }
    }

    /**
     * Resends all resources to players.
     */
    fun reloadPlayerResources() {
        for (player in this.current.players) {
            this.current.resources.sendTo(player)
            this.packs.forEach { pack -> player.sendResourcePack(pack) }
        }
    }

    /**
     * Reloads the next minigame.
     */
    fun reloadMinigame() {
        this.reloadMinigame(this.current.server)
    }

    /**
     * Reloads the current teams, syncing from the sync service.
     */
    suspend fun reloadTeams() {
        this.reloadTeams(this.current.server)
    }

    /**
     * Re-creates all teams, syncing from the sync service.
     */
    suspend fun createTeams() {
        this.createTeams(this.current.server)
    }

    internal fun registerEvents(registry: ListenerRegistry) {
        registry.register<PlayerRequestLoginEvent>(::onPlayerRequestLogin)
        registry.register<MinigameInitializeEvent>(::onMinigameInitialize)
        registry.register<PlayerJoinEvent>(phase = PlayerJoinEvent.PHASE_INITIALIZED, listener = ::onPlayerJoinEarly)
        registry.register<PlayerJoinEvent>(::onPlayerJoin)
        registry.register<PlayerChatEvent>(::onPlayerChat)
        registry.register<ServerSaveEvent>(::onServerSave)
        registry.register<ServerTickEvent>(::onServerTick)
    }

    // This should be called *after* minigames have been loaded
    // so the state can reload minigames if necessary
    internal fun load(server: MinecraftServer) {
        server.isUsingWhitelist = true

        this.reloadConfiguration()
        this.reloadState()
        if (this.minigame == null) {
            this.reloadMinigame(server)
        }
        this.reloadResourcePacks()

        this.lobby = this.createLobby(server)
    }

    internal fun reload(server: MinecraftServer) {
        this.reloadPlayers(server)
        this.reloadConfiguration()
        this.reloadLobby(server)
        this.reloadMinigame(server)
        this.reloadResourcePacks()
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

    private fun reloadMinigame(server: MinecraftServer) {
        this.minigame?.close()
        val minigame = this.config.minigame.create(MinigameCreationContext(server))
        minigame.tryInitialize()
        this.minigame = minigame
    }

    private fun reloadResourcePacks() {
        this.packs.clear()
        // This is kinda yucky
        CasualResourcePackHost.getCommonPacks().mapTo(this.packs) { hosted -> hosted.toPackInfo() }
        this.packs.addAll(CasualResourcePackHost.createResourcesFromPacks { this.config.packs }.getPacks())
    }

    private fun reloadLobby(server: MinecraftServer) {
        val previous = this.lobby
        this.lobby = this.createLobby(server)
        previous.players.transferTo(this.lobby)
        previous.close()
    }

    private fun createLobby(server: MinecraftServer): LobbyMinigame {
        val lobby = LobbyMinigames.create(this.config.lobby, this::minigame, MinigameCreationContext(server))
        lobby.resources.add(CasualResourcePackHost.createResourcesFromPacks { lobby.getAdditionalPacks() })
        this.modifyLobbyMinigame(lobby)
        lobby.start()
        return lobby
    }

    private suspend fun createTeams(server: MinecraftServer) {
        val teams = this.championships.sync.getTeams()
        val scoreboard = server.scoreboard
        if (this.championships.sync !is CasualNoopSyncService) {
            for (team in scoreboard.playerTeams.toList()) {
                scoreboard.removePlayerTeam(team)
            }
        }

        this.current.teams.setAdminTeam(scoreboard.getOrCreateAdminTeam())
        this.current.teams.setSpectatorTeam(scoreboard.getOrCreateSpectatorTeam())

        val updated = ArrayList<PlayerTeam>(teams.size)
        for (team in teams) {
            val playerTeam = scoreboard.getOrCreateTeam(team.name)
            this.syncTeamPlayers(scoreboard, playerTeam, team)
            playerTeam.setPlayerPrefix(team.prefix)
            playerTeam.setHexColor(team.color)
            playerTeam.isAllowFriendlyFire = false
            playerTeam.collisionRule = Team.CollisionRule.ALWAYS
            if (!this.current.teams.isSpectatorTeam(playerTeam)) {
                updated.add(playerTeam)
            }
        }

        for (player in this.current.players) {
            val operator = this.config.operators.contains(player.username)
            if (operator) {
                this.current.players.addAdmin(player)
            }
            if (player.team == null || player.team == this.current.teams.getAdminTeam()) {
                this.current.players.setSpectating(player)
            }
        }

        if (CasualResourcePackHost.loadTeamColors(updated)) {
            this.reloadResourcePacks()
            this.reloadPlayerResources()
        }
    }

    private suspend fun reloadTeams(server: MinecraftServer) {
        val teams = this.championships.sync.getTeams()
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
        val participants = this.championships.sync.getParticipants()
        val whitelist = server.playerList.whiteList
        val previous = whitelist.userList.toSet()
        val removed = ArrayList<String>()
        if (participants is SyncableParticipants.Strict) {
            for (entry in whitelist.entries.toList()) {
                server.playerList.whiteList.remove(entry)
            }
            removed.addAll(previous - participants.profiles.map(GameProfile::name).toSet())
        }

        val added = HashSet<String>()
        for (profile in participants.profiles) {
            whitelist.add(UserWhiteListEntry(NameAndId(profile)))
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
        val profile = event.identification
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
        val (player) = event
        this.packs.forEach { pack -> player.sendResourcePack(pack) }
        this.current.players.add(player, admin = this.config.operators.contains(player.username))
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        event.format { it.copy(username = event.player.getChatUsername(false)) }
    }

    @Suppress("unused_parameter")
    private fun onServerSave(event: ServerSaveEvent) {
        this.writeEventState()
    }

    private fun onServerTick(event: ServerTickEvent) {
        if (this.isInLobby()) {
            val uptime = this.lobby.uptime
            if (uptime.Ticks > 60.Seconds && uptime % 30.Seconds.ticks == 0) {
                this.reloadPlayers(event.server)
            }
        }
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
            this.lobby.tags.clear(CasualTags.WON)
            for (winner in minigame.tags.getUUIDsFor(CasualTags.WON)) {
                this.lobby.tags.add(winner, CasualTags.WON)
            }
        }

        minigame.settings.replay = !CasualChampionships.config.dev
    }

    private fun modifyDuelMinigame(minigame: DuelMinigame) {
        this.registerSyncMinigameStats(minigame)
        CasualGuiUtils.setMinigameUI(minigame)
        minigame.visuals.setPlayerListDisplay(CasualGuiUtils.createSimpleTabDisplay(minigame))

        minigame.resources.add(CasualResourcePackHost.createResourcesFromPacks {
            minigame.duelArena.data.packs
        })
    }

    private fun modifyLobbyMinigame(minigame: LobbyMinigame) {
        minigame.events.register<MinigameAddAdminEvent>(::outputAdminLogs)
        minigame.visuals.setSidebar(LobbySidebar.create(this.event.replace('_', ' '), minigame))
        CasualGuiUtils.setMinigameUI(minigame)
    }

    private fun outputAdminLogs(event: MinigameAddAdminEvent) {
        val player = event.player
        val dev = this.championships.config.dev
        val database = this.championships.config.database.name
        val message = when {
            dev -> Component.literal("Minigames are in dev mode!").red()
            else -> Component.literal("Minigames are NOT in dev mode!").red()
        }
        player.sendSystemMessage(message)
        val location = if (dev) "${database}_debug" else database
        player.sendSystemMessage(Component.literal("Minigames are using $location database!").red())
    }

    private fun registerSyncMinigameStats(minigame: Minigame) {
        minigame.events.register<MinigameCompleteEvent> {
            minigame.server.launch { championships.sync.syncMinigame(minigame) }
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
        val path = this.path.resolve("config.json")
        if (path.notExists()) {
            JsonUtils.encodeWith(EventConfiguration(), EventConfiguration.CODEC, path)
        }
        return when (val result = JsonUtils.decodeWith(EventConfiguration.CODEC, path)) {
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