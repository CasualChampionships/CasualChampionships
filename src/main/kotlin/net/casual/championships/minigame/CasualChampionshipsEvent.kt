package net.casual.championships.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.events.MinigameData
import net.casual.arcade.minigame.events.SimpleMinigamesEvent
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.championships.CasualMod
import net.casual.championships.common.CommonMod
import net.casual.championships.minigame.lobby.CasualLobby
import net.casual.championships.minigame.lobby.CasualLobbyMinigame
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.CasualConfig
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import java.util.*

class CasualChampionshipsEvent(
    name: String = "default",
    lobby: LobbyTemplate = LobbyTemplate.DEFAULT,
    dimension: Optional<ResourceKey<Level>>,
    operators: List<String> = listOf(),
    minigames: List<MinigameData>,
    repeat: Boolean = true,
    private val additionalPacks: List<String>
): SimpleMinigamesEvent(name, lobby, dimension, operators, minigames, repeat) {
    override fun getAdditionalPacks(): Iterable<PackInfo> {
        val packs = ArrayList<PackInfo>()
        for (pack in this.additionalPacks) {
            val hosted = CasualResourcePackHost.getHostedPack(pack)
            if (hosted == null) {
                CasualMod.logger.error("Failed to load additional pack $pack")
                continue
            }
            packs.add(hosted.toPackInfo(!CasualConfig.dev))
        }

        return CommonMod.COMMON_PACKS.mapNotNullTo(packs) { creator ->
            CasualResourcePackHost.getHostedPack(creator.zippedName())?.toPackInfo(!CasualConfig.dev)
        }
    }

    override fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        val minigame = if (lobby is CasualLobby) {
            CasualLobbyMinigame(server, lobby)
        } else {
            super.createLobbyMinigame(server, lobby)
        }
        CasualMinigames.setCasualUI(minigame)
        return minigame
    }

    override fun codec(): MapCodec<out CasualChampionshipsEvent> {
        return CODEC
    }

    companion object: CodecProvider<CasualChampionshipsEvent> {
        override val ID: ResourceLocation = CasualMod.id("championships")

        override val CODEC: MapCodec<out CasualChampionshipsEvent> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.STRING.encodedOptionalFieldOf("name", "default").forGetter(SimpleMinigamesEvent::name),
                LobbyTemplate.CODEC.fieldOf("lobby").forGetter(SimpleMinigamesEvent::lobby),
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("lobby_dimension").forGetter(SimpleMinigamesEvent::dimension),
                Codec.STRING.listOf().fieldOf("operators").forGetter(SimpleMinigamesEvent::operators),
                MinigameData.CODEC.listOf().fieldOf("minigames").forGetter(SimpleMinigamesEvent::minigames),
                Codec.BOOL.fieldOf("repeat").forGetter(SimpleMinigamesEvent::repeat),
                Codec.STRING.listOf().encodedOptionalFieldOf("additional_packs", listOf()).forGetter(CasualChampionshipsEvent::additionalPacks)
            ).apply(instance, ::CasualChampionshipsEvent)
        }
    }
}