package net.casual.championships.missilewars

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.championships.missilewars.items.MissileWarsItems
import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MissileWarsMod: DedicatedServerModInitializer {
    const val MOD_ID = "casual_missile_wars"

    val logger: Logger = LoggerFactory.getLogger("CasualMissileWars")

    val MISSILE_WARS_PACK = NamedResourcePackCreator.named("missile_wars") {
        addAssetSource(MOD_ID)
        addLangsFromData(MOD_ID)
        packDescription = "Resources for CasualChampionships Missile Wars minigame".literal()
    }

    override fun onInitializeServer() {
        MissileWarsItems.noop()
    }

    fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }
}