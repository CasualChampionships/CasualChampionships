package net.casualuhc.uhcmod.uhc.handlers

import net.casualuhc.arcade.area.PlaceableArea
import net.casualuhc.arcade.math.Location
import net.casualuhc.arcade.utils.PlayerUtils.teleportTo
import net.minecraft.server.level.ServerPlayer

interface LobbyHandler {
    fun getArea(): PlaceableArea

    fun getSpawn(): Location

    fun getSpawn(player: ServerPlayer): Location {
        return this.getSpawn()
    }

    fun forceTeleport(player: ServerPlayer) {
        player.teleportTo(this.getSpawn(player))
    }

    fun tryTeleport(player: ServerPlayer): Boolean {
        if (player.level != this.getSpawn().level || !this.getArea().getEntityBoundingBox().contains(player.position())) {
            this.forceTeleport(player)
            return true
        }
        return false
    }
}