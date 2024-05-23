package net.casual.championships.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.events.SimpleMinigamesEvent
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.championships.CasualMod
import net.casual.championships.common.CommonMod
import net.casual.championships.minigame.lobby.CasualLobbyMinigame
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.Config
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import java.util.*

class CasualChampionshipsEvent(
    lobby: LobbyTemplate = LobbyTemplate.DEFAULT,
    dimension: Optional<ResourceKey<Level>>,
    operators: List<String> = listOf(),
    minigames: List<ResourceLocation>,
    repeat: Boolean = true
): SimpleMinigamesEvent(lobby, dimension, operators, minigames, repeat) {
    override fun getAdditionalPacks(): Iterable<PackInfo> {
        return CommonMod.COMMON_PACKS.mapNotNull { creator ->
            CasualResourcePackHost.getHostedPack(creator.zippedName())?.toPackInfo(!Config.dev)
        }
    }

    override fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        val minigame = CasualLobbyMinigame(server, lobby)
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
                LobbyTemplate.CODEC.fieldOf("lobby").forGetter(SimpleMinigamesEvent::lobby),
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("lobby_dimension").forGetter(SimpleMinigamesEvent::dimension),
                Codec.STRING.listOf().fieldOf("operators").forGetter(SimpleMinigamesEvent::operators),
                ResourceLocation.CODEC.listOf().fieldOf("minigames").forGetter(SimpleMinigamesEvent::minigames),
                Codec.BOOL.fieldOf("repeat").forGetter(SimpleMinigamesEvent::repeat)
            ).apply(instance, ::CasualChampionshipsEvent)
        }
    }
}