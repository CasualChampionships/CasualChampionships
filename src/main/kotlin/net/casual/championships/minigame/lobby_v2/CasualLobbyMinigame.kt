package net.casual.championships.minigame.lobby_v2

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.toKey
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.common.util.casual
import net.casual.championships.minigame.lobby.CasualLobbyData
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import java.util.*
import kotlin.reflect.KProperty0

class CasualLobbyMinigame(
    server: MinecraftServer,
    uuid: UUID,
    next: KProperty0<Minigame?>,
    private val modules: MinigameDataModules
): Minigame(server, uuid) {
    private val next by next

    override val id: ResourceLocation = ID

    override fun phases(): Collection<Phase<out Minigame>> {
        // TODO:
        return listOf()
    }

    private fun createLevel(): CustomLevel {
        // TODO: We should maybe make some of this optional
        val data = this.modules.get<CasualLobbyData>()!!
        val dimension = ResourceUtils.random().toKey(Registries.DIMENSION)
        this.modules.get<MinigameWorldData>()!!.extract(this.server, dimension)
        val level = CustomLevelBuilder.build(this.server) {
            spoofedDimensionKey(casual("lobby"))
            dimensionKey(dimension)
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(server))
            defaultLevelProperties()
            persistence(LevelPersistence.Temporary)
            weather {
                if (data.raining) {
                    raining = true
                    rainTime = 999999
                }
            }
            when {
                data.timeOfDay.isEmpty -> tickTime(true)
                else -> timeOfDay(data.timeOfDay.get().toLong())
            }
        }
        return level
    }

    companion object {
        private val lobbies = CasualUtils.resolve("lobbies")

        val ID = casual("lobby")

        fun create(lobby: String, next: KProperty0<Minigame?>, context: MinigameCreationContext): CasualLobbyMinigame {
            val server = context.server
            val archive = ReadableArchive.from(this.lobbies.resolve(lobby))
            val modules = MinigameDataModules.from(archive, server)
            return CasualLobbyMinigame(server, context.uuid, next, modules)
        }
    }
}