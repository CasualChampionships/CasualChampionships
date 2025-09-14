package net.casual.championships.minigame

import com.mojang.serialization.JsonOps
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.events.server.ServerStopEvent
import net.casual.arcade.events.server.player.PlayerChatEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.server.player.PlayerRequestLoginEvent
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.ready.MinigamePlayerReadyHandler
import net.casual.arcade.minigame.ready.ReadyChecker
import net.casual.arcade.minigame.template.minigame.MinigamesTemplate
import net.casual.arcade.minigame.template.minigame.SequentialMinigames
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.minigame.utils.MinigameUtils.broadcastChangesToAdmin
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.PlayerUtils.broadcastToOps
import net.casual.arcade.utils.PlayerUtils.getChatUsername
import net.casual.arcade.utils.ServerUtils.setMessageOfTheDay
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.chat.PlayerChatFormatter
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.convertCasing
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.casual.arcade.utils.set
import net.casual.arcade.utils.string.SmallCapsTitleCase
import net.casual.arcade.utils.string.TitleCase
import net.casual.arcade.utils.toSmallCaps
import net.casual.championships.CasualChampionships
import net.casual.championships.commands.*
import net.casual.championships.common.ui.CasualCountdown
import net.casual.championships.common.ui.CasualTeamReadyHandler
import net.casual.championships.common.util.*
import net.casual.championships.common.util.CasualGuiUtils.broadcastWithSound
import net.casual.championships.data.*
import net.casual.championships.duel.minigame.DuelMinigame
import net.casual.championships.events.CasualChampionshipsReloadEvent
import net.casual.championships.minigame.lobby.CasualLobbyMinigameFactory
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.minigame.UHCMinigameFactory
import net.casual.championships.config.CasualConfig
import net.casual.championships.util.CasualTeamUtils.getOrCreateAdminTeam
import net.casual.championships.util.CasualTeamUtils.getOrCreateSpectatorTeam
import net.casual.database.CasualDatabase
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.UserWhiteListEntry
import net.minecraft.world.level.GameRules
import net.minecraft.world.scores.Team
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer

@Suppress("UnstableApiUsage")
object CasualMinigames {
    private val path: Path = CasualUtils.resolve("event")
    internal val winners = HashSet<String>()

    private var minigames: SequentialMinigames? = null

    private var dataManager: DataManager? = null

    val minigame: Minigame
        get() = this.getMinigames().getCurrent()

    @JvmField
    var floodgates = false

    fun getMinigames(): SequentialMinigames {
        return this.minigames ?: throw IllegalArgumentException("Tried to access minigames too early!")
    }

    fun getDataManager(): DataManager {
        return this.dataManager ?: throw IllegalArgumentException("Tried to access data manager too early!")
    }

    fun isWinner(player: ServerPlayer): Boolean {
        return this.winners.contains(player.scoreboardName)
    }

    fun hasWinner(): Boolean {
        return this.winners.isNotEmpty()
    }

    fun reloadResourcePacks() {
        for (player in this.minigame.players) {
            this.getMinigames().sendResourcesTo(player)
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<MinigameInitializeEvent> { event -> this.modifyMinigame(event.minigame) }

        // TODO: Move these?
        UHCMinigameFactory.register(MinigameRegistries.MINIGAME_FACTORY)
        CasualLobbyMinigameFactory.register(MinigameRegistries.MINIGAME_FACTORY)

        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> { event ->
            event.register(MinesweeperCommand, CasualCommand, ViewCommand, ReplayCommand, RenameCommand)
        }

        GlobalEventHandler.Server.register<PlayerRequestLoginEvent> { event ->
            if (event.isAccepted && !floodgates && !event.server.playerList.isOp(event.profile)) {
                event.deny(Component.literal("CasualChampionships isn't quite ready yet..."))
            }
            if (!event.isAccepted) {
                event.server.playerList.players.broadcastToOps(
                    Component.literal("${event.profile.name} tried to join, but was denied because: ").append(event.reason!!)
                )
            }
        }

        // This must happen before minigames are loaded
        GlobalEventHandler.Server.register<ServerStartEvent>(0) {
            val minigames = SequentialMinigames(this.readMinigameEvent(), it.server)
            this.minigames = minigames

            // We have to set it globally because Mojang made it a global toggle
            it.server.gameRules.set(GameRules.RULE_LOCATOR_BAR, false, it.server)
            it.server.setMessageOfTheDay(this.getMOTD())

            this.dataManager = createDataManager(CasualChampionships.config)

        }
        GlobalEventHandler.Server.register<ServerStartEvent>(Int.MAX_VALUE) {
            val data = this.loadMinigameEventData()
            if (data != null) {
                this.getMinigames().setData(data)
            }

            if (this.dataManager !is EmptyDataManager) {
                it.server.playerList.setUsingWhiteList(true)
            }
            this.createTeams(it.server)
        }

        GlobalEventHandler.Server.register<PlayerJoinEvent>(phase = PlayerJoinEvent.PHASE_INITIALIZED) {
            it.joinMessageModification = PlayerJoinEvent.JoinMessageModification.Delay
        }
        GlobalEventHandler.Server.register<PlayerJoinEvent> {
            val player = it.player
            this.getMinigames().addPlayer(player)
        }

        GlobalEventHandler.Server.register<PlayerChatEvent> { event ->
            event.format { it.copy(username = event.player.getChatUsername(false)) }
        }

        GlobalEventHandler.Server.register<ServerSaveEvent> {
            this.writeMinigameEventData(this.getMinigames().getData())
        }

        GlobalEventHandler.Server.register<CasualChampionshipsReloadEvent>(priority = 10_000) { (_, config) ->
            val minigames = this.getMinigames()
            minigames.event = this.readMinigameEvent()
            minigames.reloadLobby()

            this.getDataManager().close()
            this.dataManager = this.createDataManager(config)
        }

        GlobalEventHandler.Server.register<ServerStopEvent> {
            this.getDataManager().close()
        }
    }

    private fun modifyMinigame(minigame: Minigame) {
        when (minigame) {
            is UHCMinigame -> this.modifyUHCMinigame(minigame)
            is DuelMinigame -> this.modifyDuelMinigame(minigame)
        }
    }

    private fun modifyUHCMinigame(minigame: UHCMinigame) {
        PerformanceUtils.reduceMinigameMobcap(minigame)
        PerformanceUtils.disableEntityAI(minigame)
        setCasualUI(minigame)

        minigame.resources.add(
            MinigameResources.of(CasualResourcePackHost.uhc.toPackInfo(!CasualChampionships.config.dev))
        )
        minigame.resources.add(
            MinigameResources.of(CasualResourcePackHost.boundary.toPackInfo(!CasualChampionships.config.dev))
        )

        minigame.events.register<MinigameCloseEvent> {
            getMinigames().returnToLobby()
        }
        minigame.events.register<MinigameCompleteEvent> {
            this.winners.clear()
            this.winners.addAll(minigame.winners)
            getDataManager().syncUHCData(minigame)
        }

        minigame.settings.replay = !CasualChampionships.config.dev
    }

    private fun modifyDuelMinigame(minigame: DuelMinigame) {
        minigame.events.register<MinigameCloseEvent> {
            minigame.players.transferTo(this.minigame)
        }
        minigame.events.register<MinigameCompleteEvent> {
            this.getDataManager().syncDuelData(minigame)
        }
        this.setCasualUI(minigame)
        minigame.ui.setPlayerListDisplay(CasualGuiUtils.createSimpleTabDisplay(minigame))

        minigame.resources.add(CasualResourcePackHost.createResourcesFromPacks {
            minigame.duelArena.data.packs
        })
    }

    internal fun setCasualUI(minigame: Minigame) {
        CasualGuiUtils.setMinigameUI(minigame)
    }

    private fun readMinigameEvent(): MinigamesTemplate {
        val path = this.path.resolve("event.json")
        if (path.exists()) {
            val json = path.reader().use {
                JsonUtils.decodeToJsonElement(it)
            }
            val result = MinigamesTemplate.CODEC.parse(JsonOps.INSTANCE, json)
            val event = result.resultOrPartial {
                CasualUtils.logger.error(it)
            }
            if (event.isPresent) {
                return event.get()
            } else {
                val error = result.error()
                if (error.isPresent) {
                    CasualUtils.logger.error(error.get().message())
                }
            }
        }
        this.writeMinigameEvent(MinigamesTemplate.DEFAULT)
        return MinigamesTemplate.DEFAULT
    }

    private fun writeMinigameEvent(config: MinigamesTemplate) {
        val path = this.path.resolve("event.json")
        path.parent.createDirectories()
        val json = MinigamesTemplate.CODEC.encodeStart(JsonOps.INSTANCE, config).result()
        if (json.isPresent) {
            path.writer().use {
                JsonUtils.encode(json.get(), it)
            }
        }
    }

    private fun loadMinigameEventData(): SequentialMinigames.Data? {
        val eventData = this.path.resolve("event_data.json")
        if (eventData.exists()) {
            val json = eventData.reader().use {
                JsonUtils.decodeToJsonElement(it)
            }
            val result = SequentialMinigames.Data.CODEC.parse(JsonOps.INSTANCE, json).result()
            if (result.isPresent) {
                return result.get()
            }
        }
        return null
    }

    private fun writeMinigameEventData(data: SequentialMinigames.Data) {
        val eventData = this.path.resolve("event_data.json")
        eventData.parent.createDirectories()
        val json = SequentialMinigames.Data.CODEC.encodeStart(JsonOps.INSTANCE, data).result()
        if (json.isPresent) {
            eventData.writer().use {
                JsonUtils.encode(json.get(), it)
            }
        }
    }

    private fun getMOTD(): Component {
        return Component {
            val title = literal("\uD83D\uDDE1").yellow() + " " +
                literal("Casual Championships".convertCasing(TitleCase, SmallCapsTitleCase)).bold().color(0xFFAC1C) +
                " " + literal("\uD83C\uDFF9").yellow()
            val subtitle = literal("   be prepared".toSmallCaps()).lime() + wrap() +
                literal(" ◆ ").white() + literal("let the chaos ensue    ".toSmallCaps()).lime()

            empty() + literal("╔═════", 0x009BFF, 0x80CDFF) + title + literal("═════╗", 0x80CDFF, 0x009BFF) + nl +
                literal("╚══", 0x009BFF, 0x33AFFF) + subtitle + literal("══╝", 0x33AFFF, 0x009BFF)
        }
    }

    fun createTeams(server: MinecraftServer) {
        this.getDataManager().createTeams(server).thenApplyAsync({ teams ->
            val scoreboard = server.scoreboard
            this.minigame.teams.setAdminTeam(scoreboard.getOrCreateAdminTeam())
            this.minigame.teams.setSpectatorTeam(scoreboard.getOrCreateSpectatorTeam())
            val minigames = this.getMinigames()
            for (player in server.playerList.players) {
                if (minigames.event.isAdmin(player)) {
                    this.minigame.players.addAdmin(player)
                    val team = player.team
                    if (team == null || team == this.minigame.teams.getAdminTeam()) {
                        this.minigame.players.setSpectating(player)
                    }
                }
            }
            for (player in this.minigame.players) {
                if (player.team == null) {
                    this.minigame.players.setSpectating(player)
                }
            }

            if (CasualResourcePackHost.loadTeamColors(teams)) {
                this.reloadResourcePacks()
            }
        }, server)

        this.reloadWhitelist(server)
    }

    fun reloadTeams(server: MinecraftServer) {
        this.getDataManager().reloadTeams(server)
        return this.reloadWhitelist(server)
    }

    private fun reloadWhitelist(server: MinecraftServer) {
        this.getDataManager().getParticipants().thenApplyAsync({ participants ->
            val whitelist = server.playerList.whiteList
            val previous = whitelist.userList.toSet()
            for (entry in whitelist.entries.toList()) {
                server.playerList.whiteList.remove(entry)
            }
            val added = HashSet<String>()
            val removed = previous - participants.mapTo(HashSet()) { it.name }
            for (participant in participants) {
                whitelist.add(UserWhiteListEntry(participant))
                if (!previous.contains(participant.name)) {
                    added.add(participant.name)
                }
            }
            if (added.isEmpty() && removed.isEmpty()) {
                return@thenApplyAsync
            }
            val component = Component.literal("Reloaded whitelist.")
            if (added.isNotEmpty()) {
                component.append(Component.literal("\nAdded: ").green()).append(added.joinToString { it })
            }
            if (removed.isNotEmpty()) {
                component.append(Component.literal("\nRemoved: ").red()).append(removed.joinToString { it })
            }
            server.playerList.players.broadcastToOps(component.withMiniFont())
        }, server)
    }

    private fun createDataManager(config: CasualConfig): DataManager {
        val login = config.database
        if (login.url.isEmpty()) {
            return EmptyDataManager
        }
        val location = if (config.dev) "${login.name}_debug" else login.name
        val database = CasualDatabase(login.url + "/$location", login.username, login.password)
        database.initialize()
        return DatabaseDataManager(this.getMinigames().event.name, database)
    }
}