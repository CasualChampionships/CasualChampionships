package net.casual.championships.missilewars

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.MinecraftServer
import java.util.*

class MissileWarsMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, MissileWarPhase.entries) {
    companion object {
        val ID = MissileWarsMod.id("missile_wars_minigame")
    }
}