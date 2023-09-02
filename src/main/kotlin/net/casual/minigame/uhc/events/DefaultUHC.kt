package net.casual.minigame.uhc.events

import net.casual.arcade.map.BoxMap
import net.casual.arcade.map.PlaceableMap
import net.casual.arcade.math.Location
import net.casual.arcade.minigame.MinigameLobby
import net.casual.arcade.minigame.MinigameResources
import net.casual.minigame.Dimensions
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.resources.UHCResources
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

object DefaultUHC: UHCEvent, MinigameLobby {
    private val area by lazy { BoxMap(Vec3i(0, 300, 0), 40, 10, Dimensions.getLobbyLevel()) }

    override fun getTeamSize(): Int {
        return 5
    }

    override fun getSpawn(): Location {
        return Location(area.level, Vec3.atCenterOf(area.center.above(2)), Vec2(0.0F, 0.0F))
    }

    override fun getMap(): PlaceableMap {
        return area
    }

    override fun getMinigameLobby(): MinigameLobby {
        return this
    }

    override fun getResourcePackHandler(): MinigameResources {
        return UHCResources
    }

    override fun initialise(uhc: UHCMinigame) {
        uhc.server.motd = "            §6፠ §bWelcome to Casual UHC! §6፠\n     §6Yes, it's back! Is your team prepared?"
    }
}