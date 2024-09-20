package net.casual.championships.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.template.lobby.LobbyTemplate
import net.casual.arcade.minigame.template.minigame.MinigameData
import net.casual.arcade.minigame.template.minigame.SimpleMinigamesTemplate
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.championships.CasualMod
import net.casual.championships.common.CommonMod
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.CasualConfig
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import xyz.nucleoid.fantasy.util.VoidChunkGenerator
import java.util.*

class CasualChampionshipsTemplate(
    name: String = "default",
    lobby: LobbyTemplate = LobbyTemplate.DEFAULT,
    dimension: Optional<ResourceKey<Level>>,
    operators: List<String> = listOf(),
    minigames: List<MinigameData>,
    repeat: Boolean = true,
    private val lobbyBiome: ResourceLocation,
    private val additionalPacks: List<String>
): SimpleMinigamesTemplate(name, lobby, dimension, operators, minigames, repeat) {
    override fun getAdditionalPacks(): Iterable<PackInfo> {
        val packs = ArrayList<PackInfo>()
        for (pack in this.additionalPacks) {
            val hosted = CasualResourcePackHost.getHostedPack(pack)
            if (hosted == null) {
                CasualMod.logger.error("Failed to load additional pack $pack")
                continue
            }
            packs.add(hosted.toPackInfo(!CasualMod.config.dev))
        }

        return CasualResourcePackHost.getCommonPacks().mapTo(packs) {
            it.toPackInfo(!CasualMod.config.dev)
        }
    }

    override fun createTemporaryLobbyLevel(server: MinecraftServer): RuntimeWorldHandle {
        val config = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(VoidChunkGenerator(server, this.lobbyBiome))
            .setShouldTickTime(true)
        return Fantasy.get(server).openTemporaryWorld(config)
    }

    override fun codec(): MapCodec<out CasualChampionshipsTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualChampionshipsTemplate> {
        override val ID: ResourceLocation = CasualMod.id("championships")

        override val CODEC: MapCodec<out CasualChampionshipsTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("name", "default").forGetter(SimpleMinigamesTemplate::name),
                LobbyTemplate.CODEC.fieldOf("lobby").forGetter(SimpleMinigamesTemplate::lobby),
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("lobby_dimension").forGetter(SimpleMinigamesTemplate::dimension),
                Codec.STRING.listOf().fieldOf("operators").forGetter(SimpleMinigamesTemplate::operators),
                MinigameData.CODEC.listOf().fieldOf("minigames").forGetter(SimpleMinigamesTemplate::minigames),
                Codec.BOOL.fieldOf("repeat").forGetter(SimpleMinigamesTemplate::repeat),
                ResourceLocation.CODEC.encodedOptionalFieldOf("lobby_biome", Biomes.PLAINS.location()).forGetter(CasualChampionshipsTemplate::lobbyBiome),
                Codec.STRING.listOf().encodedOptionalFieldOf("additional_packs", listOf()).forGetter(CasualChampionshipsTemplate::additionalPacks)
            ).apply(instance, ::CasualChampionshipsTemplate)
        }
    }
}