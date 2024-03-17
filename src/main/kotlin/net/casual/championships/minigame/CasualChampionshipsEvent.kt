package net.casual.championships.minigame

import com.google.gson.JsonArray
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.minigame.MinigameSetPhaseEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.events.player.PlayerTeamLeaveEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.NONE
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.MinigamesEventConfig
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.events.lobby.LobbyMinigame.LobbyPhase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.FantasyUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.LevelUtils
import net.casual.arcade.utils.MinigameUtils.transferPlayersTo
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.json.LongSerializer
import net.casual.championships.CasualMod
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonUI
import net.casual.championships.common.util.PerformanceUtils
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.duel.DuelSettings
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.uhc.UHCMinigame
import net.casual.championships.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameType
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.levelgen.WorldOptions
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import java.util.concurrent.CompletableFuture
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

class CasualChampionshipsEvent(config: MinigamesEventConfig): MinigamesEvent(config) {
    private val seed by Config.any(default = WorldOptions.randomSeed(), serializer = LongSerializer)

    override fun getAdditionalPacks(): List<String> {
        return CasualResourcePack.getPackNames()
    }

    override fun getPackInfo(name: String): PackInfo? {
        return CasualResourcePackHost.getHostedPack(name)?.toPackInfo(!Config.dev)
    }

    override fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        return CasualLobbyMinigame(server, lobby)
    }

    fun createUHCMinigame(context: MinigameCreationContext): UHCMinigame {
        val server = context.server
        val seed = this.seed
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

        minigame.addResources(object: MinigameResources {
            override fun getPacks(): Collection<PackInfo> {
                val pack = CasualResourcePackHost.getHostedPack("uhc") ?: return listOf()
                return listOf(pack.toPackInfo(!Config.dev))
            }
        })

        minigame.events.register<MinigameCloseEvent> {
            this.updateDatabase(minigame)
            this.returnToLobby(server)
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

        minigame.settings.replay = !Config.dev

        return minigame
    }

    fun createDuelMinigame(context: MinigameCreationContext): DuelMinigame {
        return this.createDuelMinigame(context.server, DuelSettings())
    }

    fun createDuelMinigame(server: MinecraftServer, settings: DuelSettings): DuelMinigame {
        val minigame = DuelMinigame(server, settings)
        minigame.events.register<MinigameCloseEvent> {
            minigame.transferPlayersTo(this.current)
        }
        minigame.events.register(MinigameSetPhaseEvent::class.java, flags = NONE) {
            if (it.minigame is LobbyMinigame && this.current === it.minigame && it.phase == LobbyPhase.Readying) {
                minigame.close()
            }
        }
        return minigame
    }

    // TODO: Use a proper database
    private fun updateDatabase(minigame: Minigame<*>) {
        val serialized = minigame.data.toJson()
        CompletableFuture.runAsync {
            try {
                val stats = Config.resolve("stats.json")
                val existing = if (stats.exists()) {
                    stats.bufferedReader().use {
                        JsonUtils.GSON.fromJson(it, JsonArray::class.java)
                    }
                } else JsonArray()
                existing.add(serialized)
                stats.bufferedWriter().use {
                    JsonUtils.GSON.toJson(existing, it)
                }
            } catch (e: Exception) {
                CasualMod.logger.error("Failed to write stats!", e)
                // So we have it somewhere!
                CasualMod.logger.error(JsonUtils.GSON.toJson(serialized))
            }
        }
    }
}