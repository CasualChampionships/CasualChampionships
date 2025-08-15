package net.casual.championships.minigame.lobby

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.getDimensionPath
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.championships.CasualMod
import net.casual.championships.common.util.CommonConfig
import net.casual.championships.duel.arena.DuelArenasTemplate
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.resources.CasualResourcePackHost
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import java.util.*

class CasualLobbyMinigameFactory(
    private val name: Optional<String>,
    private val duelArenas: List<DuelArenasTemplate>
): MinigameFactory {
    private lateinit var modules: MinigameDataModules

    override fun codec(): MapCodec<out MinigameFactory> {
        return CODEC
    }

    override fun create(context: MinigameCreationContext): CasualLobbyMinigame {
        this.initializeModules(context.server)

        val data = this.modules.get<CasualLobbyData>()!!
        val level = CustomLevelBuilder.build(context.server) {
            spoofedDimensionKey(CasualMod.id("lobby"))
            randomDimensionKey()
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(context.server, data.biome))
            defaultLevelProperties()
            persistence(LevelPersistence.Temporary)
            if (data.timeOfDay.isEmpty) {
                tickTime(true)
            } else {
                timeOfDay = data.timeOfDay.get().toLong()
            }
            weather {
                if (data.raining) {
                    raining = true
                    rainTime = 999999
                }
            }
        }

        val path = context.server.getDimensionPath(level.dimension())
        this.modules.get<MinigameWorldData>()!!.extract(path)

        val minigame = CasualLobbyMinigame(
            context.server,
            context.uuid,
            data.spawn.get().with(level),
            this.duelArenas,
            this.modules,
            this
        )
        CasualMinigames.setCasualUI(minigame)
        minigame.resources.add(CasualResourcePackHost.createResourcesFromPacks { data.packs })
        return minigame
    }

    private fun initializeModules(server: MinecraftServer) {
        if (this::modules.isInitialized) {
            return
        }
        if (this.name.isPresent) {
            val path = lobbies.resolve(this.name.get())
            val archive = ReadableArchive.from(path)
            this.modules = MinigameDataModules.from(archive, server)
        } else {
            CasualMod.logger.error("No lobby specified for event!")
            this.modules = MinigameDataModules.empty()
        }
    }

    companion object: CodecProvider<CasualLobbyMinigameFactory> {
        private val lobbies = CommonConfig.resolve("lobbies")

        override val ID: ResourceLocation = CasualLobbyMinigame.ID

        override val CODEC: MapCodec<out CasualLobbyMinigameFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("lobby").forGetter(CasualLobbyMinigameFactory::name),
                DuelArenasTemplate.CODEC.listOf().encodedOptionalFieldOf("duel_arenas", listOf()).forGetter(CasualLobbyMinigameFactory::duelArenas)
            ).apply(instance, ::CasualLobbyMinigameFactory)
        }
    }
}