package net.casual.championships.minigame.lobby

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.casual.championships.common.util.casual
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

class CasualLobbyParkourData(
    val exit: Location,
    val areas: List<AABB>,
    val checkpoints: List<Checkpoint>
): MinigameDataModule {
    fun isWithinParkourArea(point: Vec3): Boolean {
        return this.areas.any { it.contains(point) }
    }

    fun getIntersectingCheckpoint(point: Vec3, current: Int): Int {
        for (i in current..this.checkpoints.lastIndex) {
            val checkpoint = this.checkpoints[i]
            if (checkpoint.collision.contains(point)) {
                return i
            }
        }
        return current
    }

    data class Checkpoint(
        val spawn: Location,
        val collision: AABB,
        val title: Component
    ) {
        companion object {
            val CODEC: Codec<Checkpoint> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Location.CODEC.fieldOf("spawn").forGetter(Checkpoint::spawn),
                    ArcadeExtraCodecs.AABB.fieldOf("collision").forGetter(Checkpoint::collision),
                    ComponentSerialization.CODEC.fieldOf("title").forGetter(Checkpoint::title)
                ).apply(instance, ::Checkpoint)
            }
        }
    }

    companion object: MinigameDataModule.Provider {
        private const val LOBBY_PARKOUR_DATA = "casual_lobby_parkour_data.json"

        private val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                Location.CODEC.fieldOf("exit").forGetter(CasualLobbyParkourData::exit),
                ArcadeExtraCodecs.AABB.listOf().fieldOf("areas").forGetter(CasualLobbyParkourData::areas),
                Checkpoint.CODEC.listOf().fieldOf("checkpoints").forGetter(CasualLobbyParkourData::checkpoints)
            ).apply(instance, ::CasualLobbyParkourData)
        }

        override val id: ResourceLocation = casual("lobby_parkour_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): MinigameDataModule {
            return archive.parseJson(LOBBY_PARKOUR_DATA, CODEC).getOrThrow()
        }

    }
}