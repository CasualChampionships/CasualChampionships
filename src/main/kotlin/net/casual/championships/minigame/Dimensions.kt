package net.casual.championships.minigame

import net.casual.arcade.Arcade
import net.casual.championships.util.CasualUtils
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel

// TODO: Use fantasy
object Dimensions {
    private val type = ResourceKey.create(Registries.DIMENSION_TYPE, CasualUtils.id("default"))

    private val lobby = ResourceKey.create(Registries.DIMENSION, CasualUtils.id("lobby"))

    fun noop() {

    }

    fun getLobbyLevel(): ServerLevel {
        return Arcade.getServer().getLevel(lobby)!!
    }
}