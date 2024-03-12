package net.casual.championships.missilewars

import com.google.gson.JsonObject
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer

class MissileWarsMinigame(server: MinecraftServer): SavableMinigame<MissileWarsMinigame>(server) {
    override val id: ResourceLocation = MissileWarsMod.id("missile_wars_minigame")

    override fun getPhases(): Collection<Phase<MissileWarsMinigame>> {
        TODO("Not yet implemented")
    }

    override fun readData(json: JsonObject) {
        TODO("Not yet implemented")
    }

    override fun writeData(json: JsonObject) {
        TODO("Not yet implemented")
    }

    companion object {
        val ID = MissileWarsMod.id("uhc_minigame")
    }
}