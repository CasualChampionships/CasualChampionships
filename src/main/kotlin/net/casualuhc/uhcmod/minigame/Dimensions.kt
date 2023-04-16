package net.casualuhc.uhcmod.minigame

import net.casualuhc.arcade.Arcade
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

object Dimensions {
    private val type = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation("uhc", "default"))

    private val lobby = ResourceKey.create(Registries.DIMENSION, ResourceLocation("uhc", "lobby"))

    fun noop() {

    }

    fun getLobbyLevel(): ServerLevel {
        return Arcade.server.getLevel(this.lobby)!!
    }
}