package net.casualuhc.uhcmod.minigame

import net.casualuhc.arcade.Arcade
import net.casualuhc.uhcmod.util.ResourceUtils
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel

object Dimensions {
    private val type = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceUtils.id("default"))

    private val lobby = ResourceKey.create(Registries.DIMENSION, ResourceUtils.id("lobby"))

    fun noop() {

    }

    fun getLobbyLevel(): ServerLevel {
        return Arcade.server.getLevel(this.lobby)!!
    }
}