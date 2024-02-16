package net.casual.championships.minigame

import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.MinigamesEventConfig
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.FantasyUtils
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.LevelUtils
import net.casual.arcade.utils.MinigameUtils.transferTo
import net.casual.arcade.utils.ResourceUtils
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonUI
import net.casual.championships.common.util.PerformanceUtils
import net.casual.championships.minigame.duel.DuelMinigame
import net.casual.championships.minigame.duel.DuelSettings
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.uhc.UHCMinigame
import net.casual.championships.uhc.UHCResources
import net.casual.championships.util.Config
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.world.level.GameType
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.RuntimeWorldConfig

class CasualMinigamesEvent(config: MinigamesEventConfig): MinigamesEvent(config) {
    override fun getPackInfo(name: String): PackInfo? {
        return CasualResourcePackHost.getHostedPack(name)?.toPackInfo(!Config.dev)
    }

    override fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        val minigame = object: LobbyMinigame(server, lobby) {
            override val settings: MinigameSettings = CasualSettings(this)
        }

        minigame.events.register<MinigameAddPlayerEvent> { event ->
            val (_, player) = event

            player.sendSystemMessage(CommonComponents.WELCOME_MESSAGE.generate("CasualChampionships").gold())
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
        minigame.events.register<PlayerTeamJoinEvent> { event ->
            val (player, team) = event
            if (!minigame.teams.isTeamIgnored(team)) {
                minigame.removeSpectator(player)
            } else if (minigame.isSpectating(player)) {
                minigame.makeSpectator(player)
            }
        }
        minigame.addResources(UHCResources)
        minigame.ui.setTabDisplay(CommonUI.createTabDisplay())

        return minigame
    }

    fun createUHCMinigame(context: MinigameCreationContext): UHCMinigame {
        val server = context.server
        // TODO: Fix seed when server restarts.
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
            server,
            FantasyUtils.PersistentConfig(overworldId, overworldConfig),
            FantasyUtils.PersistentConfig(netherId, netherConfig),
            FantasyUtils.PersistentConfig(endId, endConfig)
        )
        val minigame = UHCMinigame.of(server, overworld, nether, end)
        PerformanceUtils.reduceMinigameMobcap(minigame)
        PerformanceUtils.disableEntityAI(minigame)

        // TODO: Make this nicer
        minigame.addResources(object: MinigameResources {
            override fun getPacks(): Collection<PackInfo> {
                return listOf(CasualResourcePackHost.getHostedPack("uhc")!!.toPackInfo(true))
            }
        })

        return minigame
    }

    fun createDuelMinigame(context: MinigameCreationContext): DuelMinigame {
        return this.createDuelMinigame(context.server, DuelSettings())
    }

    fun createDuelMinigame(server: MinecraftServer, settings: DuelSettings): DuelMinigame {
        val minigame = DuelMinigame(server, settings)
        minigame.events.register<MinigameCloseEvent> {
            minigame.transferTo(this.current, false)
        }
        return minigame
    }
}