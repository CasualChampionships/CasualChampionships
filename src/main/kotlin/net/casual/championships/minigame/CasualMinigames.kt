package net.casual.championships.minigame

import com.google.gson.JsonObject
import net.casual.arcade.area.StructuredAreaConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.MinigamesEventConfig
import net.casual.arcade.minigame.events.MinigamesEventConfigSerializer
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.*
import net.casual.arcade.utils.ComponentUtils.aqua
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.championships.minigame.duel.DuelMinigame
import net.casual.championships.minigame.duel.DuelSettings
import net.casual.championships.minigame.lobby.ui.LobbyBossBarConfig
import net.casual.championships.minigame.uhc.UHCMinigame
import net.casual.championships.minigame.uhc.resources.UHCResources
import net.casual.championships.util.CasualUtils
import net.casual.championships.util.Config
import net.casual.championships.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.level.GameType
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Suppress("JoinDeclarationAndAssignment")
object CasualMinigames {
    private val path: Path
    private val config: MinigamesEventConfigSerializer
    lateinit var event: MinigamesEvent

    val minigame: Minigame<*>
        get() = this.event.current

    @JvmField
    var floodgates = false

    init {
        this.path = Config.resolve("event")
        this.config = MinigamesEventConfigSerializer().apply {
            addAreaFactory(StructuredAreaConfig.factory(Config.resolve("lobbies")))
            addBossbarFactory(LobbyBossBarConfig)
        }

        this.readMinigameEvent()
    }

    internal fun registerEvents() {
        Minigames.registerFactory(CasualUtils.id("uhc_minigame"), this::createUHCMinigame)
        Minigames.registerFactory(CasualUtils.id("duel_minigame")) {
            DuelMinigame(it.server, DuelSettings()) {}
        }

        GlobalEventHandler.register<ServerLoadedEvent>(Int.MAX_VALUE) {
            this.loadMinigameEventData(it.server)
        }

        GlobalEventHandler.register<PlayerJoinEvent> {
            val player = it.player
            if (player.getMinigame() == null) {
                val current = this.event.current
                current.addPlayer(player)
                if (this.event.config.operators.contains(player.scoreboardName)) {
                    current.makeAdmin(player)
                }
            }
        }

        GlobalEventHandler.register<ServerSaveEvent> {
            this.writeMinigameEventData()
        }
        GlobalEventHandler.register<ServerStoppingEvent> {
            this.writeMinigameEvent()
        }
    }

    fun createTabDisplay(): ArcadeTabDisplay {
        val display = ArcadeTabDisplay(
            ComponentSupplier.of(
                Component.literal("\n")
                    .append(Texts.ICON_UHC)
                    .append(Texts.space())
                    .append(Texts.CASUAL_UHC.gold().bold())
                    .append(Texts.space())
                    .append(Texts.ICON_UHC)
            )
        ) { _ ->
            val tps = TickUtils.calculateTPS()
            val formatting = if (tps >= 20) ChatFormatting.DARK_GREEN else if (tps > 15) ChatFormatting.YELLOW else if (tps > 10) ChatFormatting.RED else ChatFormatting.DARK_RED
            Component.literal("\n")
                .append("TPS: ")
                .append(Component.literal("%.1f".format(tps)).withStyle(formatting))
                .append("\n")
                .append(Texts.TAB_HOSTED.aqua().bold())
        }
        return display
    }

    private fun createUHCMinigame(context: MinigameCreationContext): UHCMinigame {
        val server = context.server
        val seed = if (server is DedicatedServer) {
            server.properties.worldOptions.seed()
        } else {
            server.worldData.worldGenOptions().seed()
        }
        val overworldConfig = RuntimeWorldConfig()
            .setSeed(seed)
            .setShouldTickTime(true)
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(LevelUtils.overworld().chunkSource.generator)
        val netherConfig = RuntimeWorldConfig()
            .setSeed(seed)
            .setDimensionType(BuiltinDimensionTypes.NETHER)
            .setGenerator(LevelUtils.nether().chunkSource.generator)
        val endConfig = RuntimeWorldConfig()
            .setSeed(seed)
            .setDimensionType(BuiltinDimensionTypes.END)
            .setGenerator(LevelUtils.end().chunkSource.generator)

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

        val (overworld, nether, end) = FantasyUtils.createPersistentVanillaLikeLevels(
            overworldConfig = FantasyUtils.PersistentConfig(overworldId, overworldConfig),
            netherConfig = FantasyUtils.PersistentConfig(netherId, netherConfig),
            endConfig = FantasyUtils.PersistentConfig(endId, endConfig)
        )
        return UHCMinigame(
            server = server,
            overworld = overworld,
            nether = nether,
            end = end
        )
    }

    private fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        val lobbyMinigame = LobbyMinigame(server, lobby)

        lobbyMinigame.events.register<MinigameAddPlayerEvent> { event ->
            val (minigame, player) = event

            player.sendSystemMessage(Texts.LOBBY_WELCOME.append(" Casual Championships").gold())
            if (!minigame.isAdmin(player)) {
                player.setGameMode(GameType.ADVENTURE)
            } else if (Config.dev) {
                player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
            }

            val team = player.team
            if (team == null || minigame.teams.isTeamIgnored(team)) {
                GlobalTickedScheduler.later {
                    minigame.makeSpectator(player)
                }
            } else {
                GlobalTickedScheduler.later {
                    minigame.removeSpectator(player)
                }
            }
        }
        lobbyMinigame.events.register<PlayerTeamJoinEvent> { event ->
            val (player, team) = event
            if (!lobbyMinigame.teams.isTeamIgnored(team)) {
                lobbyMinigame.removeSpectator(player)
            } else if (lobbyMinigame.isSpectating(player)) {
                lobbyMinigame.makeSpectator(player)
            }
        }
        lobbyMinigame.addResources(UHCResources)
        lobbyMinigame.ui.setTabDisplay(this.createTabDisplay())

        return lobbyMinigame
    }

    private fun readMinigameEvent() {
        val path = this.path.resolve("event.json")
        if (path.exists()) {
            path.bufferedReader().use {
                val json = JsonUtils.GSON.fromJson(it, JsonObject::class.java)
                this.event = MinigamesEvent(this.config.deserialize(json), this::createLobbyMinigame)
            }
            return
        }
        this.event = MinigamesEvent(MinigamesEventConfig.DEFAULT, this::createLobbyMinigame)
        this.writeMinigameEvent()
    }

    private fun writeMinigameEvent() {
        val path = this.path.resolve("event.json")
        path.parent.createDirectories()
        path.bufferedWriter().use {
            JsonUtils.GSON.toJson(this.config.serialize(this.event.config), it)
        }
    }

    private fun loadMinigameEventData(server: MinecraftServer) {
        val eventData = this.path.resolve("event_data.json")
        if (eventData.exists()) {
            eventData.bufferedReader().use {
                val json = JsonUtils.GSON.fromJson(it, JsonObject::class.java)
                this.event.deserialize(json, server)
            }
        } else {
            this.event.returnToLobby(server)
        }
    }

    private fun writeMinigameEventData() {
        val eventData = this.path.resolve("event_data.json")
        eventData.parent.createDirectories()
        eventData.bufferedWriter().use {
            JsonUtils.GSON.toJson(this.event.serialize(), it)
        }
    }
}