package net.casualuhc.uhcmod.uhc

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.map.BoxMap
import net.casualuhc.arcade.map.PlaceableMap
import net.casualuhc.arcade.math.Location
import net.casualuhc.uhcmod.minigame.Dimensions
import net.casualuhc.uhcmod.uhc.handlers.LobbyHandler
import net.casualuhc.uhcmod.uhc.handlers.ResourceHandler
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

enum class DefaultUHC: UHCEvent, LobbyHandler, ResourceHandler {
    INSTANCE;

    private val area by lazy { BoxMap(Vec3i(0, 300, 0), 40, 10, Dimensions.getLobbyLevel()) }

    override fun getTeamSize(): Int {
        return 5
    }

    override fun getSpawn(): Location {
        return Location(this.area.level, Vec3.atCenterOf(this.area.center.above(2)), Vec2(0.0F, 0.0F))
    }

    override fun getMap(): PlaceableMap {
        return this.area
    }

    override fun getLobbyHandler(): LobbyHandler {
        return this
    }

    override fun getResourcePackHandler(): ResourceHandler {
        return this
    }

    override fun load() {
        Arcade.server.motd = "            §6፠ §bWelcome to Casual UHC! §6፠\n     §6Yes, it's back! Is your team prepared?"
    }
}