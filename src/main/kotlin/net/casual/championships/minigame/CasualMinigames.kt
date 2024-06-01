package net.casual.championships.minigame

import com.google.gson.JsonArray
import com.mojang.serialization.JsonOps
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.player.PlayerRequestLoginEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.events.player.PlayerTeamLeaveEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.SequentialMinigames
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.white
import net.casual.arcade.utils.ComponentUtils.yellow
import net.casual.arcade.utils.FantasyUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.MinigameUtils.transferPlayersTo
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.ServerUtils.setMessageOfTheDay
import net.casual.arcade.utils.StringUtils.toSmallCaps
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.json.LongSerializer
import net.casual.championships.CasualMod
import net.casual.championships.common.ui.CasualCountdown
import net.casual.championships.common.ui.CasualReadyChecker
import net.casual.championships.common.util.CommonSounds
import net.casual.championships.common.util.CommonUI
import net.casual.championships.common.util.CommonUI.broadcastWithSound
import net.casual.championships.common.util.PerformanceUtils
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.duel.DuelSettings
import net.casual.championships.events.CasualConfigReloaded
import net.casual.championships.managers.TeamManager
import net.casual.championships.missilewars.ExampleMinigame
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.uhc.UHCMinigame
import net.casual.championships.util.Config
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.scores.Team
import java.nio.file.Path
import java.util.HashSet
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*

@Suppress("UnstableApiUsage")
object CasualMinigames {
    private val seed by Config.any(default = WorldOptions.randomSeed(), serializer = LongSerializer)
    private val path: Path = Config.resolve("event")
    private val winners = HashSet<String>()

    private var minigames: SequentialMinigames? = null

    val minigame: Minigame<*>
        get() = this.getMinigames().getCurrent()

    @JvmField
    var floodgates = false

    fun getMinigames(): SequentialMinigames {
        return this.minigames ?: throw IllegalArgumentException("Tried to access minigames too early!")
    }

    fun isWinner(player: ServerPlayer): Boolean {
        return this.winners.contains(player.scoreboardName)
    }

    fun hasWinner(): Boolean {
        return this.winners.isNotEmpty()
    }

    internal fun registerEvents() {
        Minigames.registerFactory(UHCMinigame.ID, this::createUHCMinigame)
        Minigames.registerFactory(DuelMinigame.ID, this::createDuelMinigame)
        Minigames.registerFactory(ExampleMinigame.ID) { ExampleMinigame(it.server) }

        GlobalEventHandler.register<PlayerRequestLoginEvent> { event ->
            if (!floodgates && !event.server.playerList.isOp(event.profile)) {
                event.cancel("CasualChampionships isn't quite ready yet...".literal())
                this.minigame.chat.broadcastTo(
                    "${event.profile.name} tried to join, but floodgates are closed".literal(),
                    this.minigame.players.admins
                )
            }
        }

        GlobalEventHandler.register<ServerLoadedEvent>(Int.MAX_VALUE) {
            TeamManager.createTeams()

            val minigames = SequentialMinigames(this.readMinigameEvent(), it.server)
            this.minigames = minigames

            val data = this.loadMinigameEventData()
            if (data != null) {
                minigames.setData(data)
            }

            it.server.setMessageOfTheDay(this.getMOTD())
        }

        GlobalEventHandler.register<PlayerJoinEvent> {
            val player = it.player
            this.getMinigames().addPlayer(player)
        }

        GlobalEventHandler.register<ServerSaveEvent> {
            this.writeMinigameEventData(this.getMinigames().getData())
        }
        // GlobalEventHandler.register<ServerStoppingEvent> {
        //     this.writeMinigameEvent(this.getMinigames().event)
        // }

        GlobalEventHandler.register<CasualConfigReloaded> {
            val minigames = this.getMinigames()
            minigames.event = this.readMinigameEvent()
            minigames.reloadLobby()
        }
    }

    fun createUHCMinigame(context: MinigameCreationContext): UHCMinigame {
        val overworldId: ResourceLocation
        val netherId: ResourceLocation
        val endId: ResourceLocation
        if (context.hasCustomData()) {
            val dimensions = context.getCustomData().obj("dimensions")
            overworldId = ResourceLocation(dimensions.string("overworld"))
            netherId = ResourceLocation(dimensions.string("nether"))
            endId = ResourceLocation(dimensions.string("end"))
        } else {
            overworldId = ResourceUtils.random { "overworld_$it" }
            netherId = ResourceUtils.random { "nether_$it" }
            endId = ResourceUtils.random { "end_$it" }
        }

        val server = context.server
        val seed = CasualMinigames.seed
        val (overworld, nether, end) = FantasyUtils.createPersistentVanillaLikeLevels(
            server,
            FantasyUtils.PersistentConfig(overworldId, FantasyUtils.createOverworldConfig(seed)),
            FantasyUtils.PersistentConfig(netherId, FantasyUtils.createNetherConfig(seed)),
            FantasyUtils.PersistentConfig(endId, FantasyUtils.createEndConfig(seed))
        )
        val minigame = UHCMinigame.of(server, overworld, nether, end)
        PerformanceUtils.reduceMinigameMobcap(minigame)
        PerformanceUtils.disableEntityAI(minigame)
        this.setCasualUI(minigame)

        minigame.addResources(object: MinigameResources {
            override fun getPacks(): Collection<PackInfo> {
                val pack = CasualResourcePackHost.getHostedPack("uhc") ?: return listOf()
                return listOf(pack.toPackInfo(!Config.dev))
            }
        })

        minigame.events.register<MinigameCloseEvent> {
            this.updateDatabase(minigame)
            this.getMinigames().returnToLobby()
        }
        minigame.events.register<PlayerTeamJoinEvent> {
            for (teammate in it.team.getOnlinePlayers()) {
                minigame.effects.forceUpdate(teammate, it.player)
                minigame.effects.forceUpdate(it.player, teammate)
            }
        }
        minigame.events.register<PlayerTeamLeaveEvent> {
            for (teammate in it.team.getOnlinePlayers()) {
                minigame.effects.forceUpdate(teammate, it.player)
                minigame.effects.forceUpdate(it.player, teammate)
            }
        }
        minigame.events.register<MinigameCompleteEvent> {
            this.winners.clear()
            this.winners.addAll(minigame.winners)
        }

        minigame.settings.replay = !Config.dev

        return minigame
    }

    fun createDuelMinigame(context: MinigameCreationContext): DuelMinigame {
        return this.createDuelMinigame(context.server, DuelSettings(listOf()))
    }

    fun createDuelMinigame(server: MinecraftServer, settings: DuelSettings): DuelMinigame {
        val minigame = DuelMinigame(server, settings)
        minigame.events.register<MinigameCloseEvent> {
            minigame.transferPlayersTo(this.minigame)
        }
        @Suppress("MoveLambdaOutsideParentheses")
        // If the lobby starts reading, we kick everyone back to the main minigame
        minigame.events.register(MinigameSetPhaseEvent::class.java, ListenerFlags.NONE, {
            if (it.minigame is LobbyMinigame && this.minigame === it.minigame && it.phase == LobbyMinigame.LobbyPhase.Readying) {
                minigame.close()
            }
        })
        this.setCasualUI(minigame)
        minigame.ui.setPlayerListDisplay(CommonUI.createSimpleTabDisplay(minigame))
        return minigame
    }

    internal fun setCasualUI(minigame: Minigame<*>) {
        minigame.ui.setPlayerListDisplay(CommonUI.createCasualTabDisplay(minigame))
        minigame.ui.readier = CasualReadyChecker(minigame)
        minigame.ui.countdown = CasualCountdown

        minigame.ui.addNameTag(CommonUI.createPlayingNameTag())
        minigame.events.register<MinigameAddPlayerEvent> {
            it.player.team?.nameTagVisibility = Team.Visibility.NEVER
        }
        minigame.events.register<PlayerTeamJoinEvent> {
            it.team.nameTagVisibility = Team.Visibility.NEVER
        }

        this.setPauseNotification(minigame)
    }

    private fun setPauseNotification(minigame: Minigame<*>) {
        minigame.events.register<MinigamePauseEvent> {
            minigame.chat.broadcastWithSound(
                "Minigame is now paused".literal(),
                Sound(CommonSounds.GAME_PAUSED)
            )
        }
    }

    private fun readMinigameEvent(): MinigamesEvent {
        val path = this.path.resolve("event.json")
        if (path.exists()) {
            val json = path.reader().use {
                JsonUtils.decodeToJsonElement(it)
            }
            val result = MinigamesEvent.CODEC.parse(JsonOps.INSTANCE, json)
            val event = result.resultOrPartial {
                CasualMod.logger.error(it)
            }
            if (event.isPresent) {
                return event.get()
            } else {
                val error = result.error()
                if (error.isPresent) {
                    CasualMod.logger.error(error.get().message())
                }
            }
        }
        this.writeMinigameEvent(MinigamesEvent.DEFAULT)
        return MinigamesEvent.DEFAULT
    }

    private fun writeMinigameEvent(config: MinigamesEvent) {
        val path = this.path.resolve("event.json")
        path.parent.createDirectories()
        val json = MinigamesEvent.CODEC.encodeStart(JsonOps.INSTANCE, config).result()
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
        return Component.empty().apply {
            append("╔".literal().colour(0x009BFF))
            append("═".literal().colour(0x19A5FF))
            append("═".literal().colour(0x33AFFF))
            append("═".literal().colour(0x4DB9FF))
            append("═".literal().colour(0x66C3FF))
            append("═".literal().colour(0x80CDFF))
            append("\uD83D\uDDE1".literal().yellow())
            append(" ")
            append("C${"asual".toSmallCaps()} C${"hampionships".toSmallCaps()}".literal().bold().colour(0xFFAC1C))
            append(" ")
            append("\uD83C\uDFF9".literal().yellow())
            append("═".literal().colour(0x80CDFF))
            append("═".literal().colour(0x66C3FF))
            append("═".literal().colour(0x4DB9FF))
            append("═".literal().colour(0x33AFFF))
            append("═".literal().colour(0x19A5FF))
            append("╗".literal().colour(0x009BFF))
            append("\n")

            append("╚".literal().colour(0x009BFF))
            append("═".literal().colour(0x19A5FF))
            append("═".literal().colour(0x33AFFF))

            append("   ")
            append("be prepared".toSmallCaps().literal().lime())
            append(" ")
            append("◆".literal().white())
            append(" ")
            append("let the chaos ensue".toSmallCaps().literal().lime())
            append("    ")

            append("═".literal().colour(0x33AFFF))
            append("═".literal().colour(0x19A5FF))
            append("╝".literal().colour(0x009BFF))
        }
    }

    // TODO: Use a proper database
    private fun updateDatabase(minigame: Minigame<*>) {
        val serialized = minigame.data.toJson()
        CompletableFuture.runAsync {
            try {
                val stats = Config.resolve("stats.json")
                val existing = if (stats.exists()) {
                    stats.bufferedReader().use {
                        JsonUtils.decodeToJsonElement(it).asJsonArray
                    }
                } else JsonArray()
                existing.add(serialized)
                stats.bufferedWriter().use {
                    JsonUtils.encode(existing, it)
                }
            } catch (e: Exception) {
                CasualMod.logger.error("Failed to write stats!", e)
                // So we have it somewhere!
                CasualMod.logger.error(JsonUtils.GSON.toJson(serialized))
            }
        }
    }
}