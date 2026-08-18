package net.casual.championships.missilewars

import net.casual.arcade.pack.generation.PackDefinition
import net.casual.championships.missilewars.items.MissileWarsItems
import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MissileWarsMod: DedicatedServerModInitializer {
    const val MOD_ID = "casual_missile_wars"

    val logger: Logger = LoggerFactory.getLogger("CasualMissileWars")

    val MISSILE_WARS_PACK = PackDefinition("missile_wars") {
        description = Component.literal("Resources for CasualChampionships Missile Wars minigame")
        include(MOD_ID)
        addLangs(MOD_ID)
    }

    override fun onInitializeServer() {
        MissileWarsItems.noop()
    }

    fun id(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(MOD_ID, path)
    }
}