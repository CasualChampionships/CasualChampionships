package net.casual.championships.lobby.minigame.modules

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.arcade.utils.math.location.providers.LocationProvider
import net.casual.championships.common.util.casual
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import java.util.*

class LobbyData(
    val spawn: LocationProvider = LocationProvider.DEFAULT,
    val podium: LocationProvider = LocationProvider.DEFAULT,
    val podiumView: LocationProvider = LocationProvider.DEFAULT,
    val fireworkLocations: List<LocationProvider> = listOf(),
    val fireworkColors: List<Int> = DEFAULT_COLORS,
    val raining: Boolean = false,
    val timeOfDay: Optional<Int> = Optional.empty(),
    val packs: List<String> = listOf()
): MinigameDataModule {
    companion object: MinigameDataModule.Provider {
        private const val LOBBY_DATA = "casual_lobby_data.json"

        private val DEFAULT_COLORS = listOf(0x9820e0, 0xbe51e9, 0xd564fb, 0xe07b20, 0xe8a751, 0xfbbe64)

        private val CODEC: Codec<LobbyData> = RecordCodecBuilder.create { instance ->
            instance.group(
                LocationProvider.CODEC.fieldOf("spawn").forGetter(LobbyData::spawn),
                LocationProvider.CODEC.fieldOf("podium").forGetter(LobbyData::podium),
                LocationProvider.CODEC.fieldOf("podium_view").forGetter(LobbyData::podiumView),
                LocationProvider.CODEC.listOf().fieldOf("firework_locations").forGetter(LobbyData::fireworkLocations),
                Codec.INT.listOf().encodedOptionalFieldOf("firework_colors", DEFAULT_COLORS).forGetter(LobbyData::fireworkColors),
                Codec.BOOL.optionalFieldOf("raining", false).forGetter(LobbyData::raining),
                Codec.INT.optionalFieldOf("time_of_day").forGetter(LobbyData::timeOfDay),
                Codec.STRING.listOf().encodedOptionalFieldOf("packs", listOf()).forGetter(LobbyData::packs)
            ).apply(instance, ::LobbyData)
        }

        val DEFAULT = LobbyData()

        override val id: Identifier = casual("lobby_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): MinigameDataModule {
            return archive.parseJson(LOBBY_DATA, CODEC, server).getOrThrow()
        }
    }
}