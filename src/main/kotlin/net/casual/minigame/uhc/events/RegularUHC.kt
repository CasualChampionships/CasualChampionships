package net.casual.minigame.uhc.events

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.minigame.MinigameSetPhaseEvent
import net.casual.arcade.map.PlaceableMap
import net.casual.arcade.map.StructureMap
import net.casual.arcade.math.Location
import net.casual.arcade.minigame.MinigameLobby
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.StructureUtils
import net.casual.minigame.Dimensions
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.UHCPhase
import net.casual.minigame.uhc.resources.UHCResources
import net.casual.util.Config
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class RegularUHC: UHCEvent, MinigameLobby {
    private val lobby: PlaceableMap

    init {
        this.lobby = StructureMap(
            StructureUtils.read(Config.resolve("lobbies/cherry_lobby.nbt")),
            Vec3i(0, 320, 0),
            Dimensions.getLobbyLevel()
        )
    }

    override fun getTeamSize(): Int {
        return 5
    }

    override fun getMinigameLobby(): MinigameLobby {
        return this
    }

    override fun getResourcePackHandler(): MinigameResources {
        return UHCResources
    }

    override fun initialise(uhc: UHCMinigame) {
        GlobalEventHandler.register<MinigameSetPhaseEvent> {
            if (it.minigame === uhc && it.phase == UHCPhase.Lobby) {
                GlobalTickedScheduler.schedule(1, MinecraftTimeUnit.Ticks) {
                    Dimensions.getLobbyLevel().dayTime = 16000
                }
            }
        }
    }

    override fun getMap(): PlaceableMap {
        return this.lobby
    }

    override fun getSpawn(): Location {
        return Location(Dimensions.getLobbyLevel(), Vec3(-0.5, 310.0, 11.5), Vec2(180.0F, 0.0F))
    }
}