package net.casual.championships

import net.casual.championships.managers.CommandManager
import net.casual.championships.managers.DataManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.CasualRegistration
import net.casual.championships.util.Config
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CasualMod: DedicatedServerModInitializer {
    const val ID = "casual"

    val logger: Logger = LoggerFactory.getLogger("Casual")
    val container: ModContainer = FabricLoader.getInstance().getModContainer(ID).get()

    fun id(name: String): ResourceLocation {
        return ResourceLocation(ID, name)
    }

    override fun onInitializeServer() {
        logger.info("Starting CasualChampionships... Version: ${container.metadata.version}")

        CasualRegistration.register()

        Config.registerEvents()
        CommandManager.registerEvents()
        DataManager.registerEvents()
        CasualResourcePackHost.registerEvents()
        CasualMinigames.registerEvents()

        CasualResourcePack.generateAll()
    }

    // TODO:
    //   Lots of translations (Ready)
    //   SpreadPlayers
    //   Minesweeper AI - Make sure all maps are non-luck based
}