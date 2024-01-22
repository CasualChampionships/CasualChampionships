package net.casual.championships.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.area.BoxedArea
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.area.StructureArea
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.utils.JsonUtils.double
import net.casual.arcade.utils.JsonUtils.float
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.StructureUtils
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.json.JsonSerializer
import net.casual.championships.minigame.Dimensions
import net.casual.championships.util.Config
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

object LobbySerializer: JsonSerializer<Lobby> {
    override fun deserialize(json: JsonElement): Lobby {
        json as JsonObject
        val level = Dimensions.getLobbyLevel()
        val areaData = json.obj("area")
        val area = when (areaData.string("type")) {
            "structure" -> deserializeStructureArea(areaData, level)
            "box" -> deserializeBoxArea(areaData, level)
            else -> throw IllegalArgumentException()
        }
        val spawn = this.deserializeSpawn(json.obj("spawn"), level)

        return object: Lobby {
            override val area: PlaceableArea = area
            override val spawn: Location = spawn

            // TODO:
            override fun getCountdown(): Countdown {
                return TitledCountdown.DEFAULT
            }
        }
    }

    override fun serialize(value: Lobby): JsonElement {
        throw UnsupportedOperationException()
    }

    private fun deserializeSpawn(data: JsonObject, level: ServerLevel): Location {
        val x = data.double("x")
        val y = data.double("y")
        val z = data.double("z")
        val yaw = data.float("yaw")
        val pitch = data.float("pitch")
        return Location.of(x, y, z, yaw, pitch, level)
    }

    private fun deserializeStructureArea(data: JsonObject, level: ServerLevel): StructureArea {
        val path = data.string("path")
        val x = data.int("x")
        val y = data.int("y")
        val z = data.int("z")
        val location = Config.resolve("lobbies/${if (path.endsWith(".nbt")) path else "$path.nbt"}")
        return StructureArea(StructureUtils.read(location), Vec3i(x, y, z), level)
    }

    private fun deserializeBoxArea(data: JsonObject, level: ServerLevel): BoxedArea {
        val x = data.int("x")
        val y = data.int("y")
        val z = data.int("z")
        val radius = data.int("radius")
        val height = data.int("height")
        val block = BuiltInRegistries.BLOCK.get(ResourceLocation(data.string("block")))
        return BoxedArea(Vec3i(x, y, z), radius, height, level, block)
    }
}