package net.casualuhc.uhc

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.casualuhc.uhc.advancement.UHCAdvancements
import net.casualuhc.uhc.items.UHCItems
import net.casualuhc.uhc.managers.*
import net.casualuhc.uhc.minigame.Dimensions
import net.casualuhc.uhc.minigame.uhc.UHCMinigame
import net.casualuhc.uhc.resources.UHCResourcePack
import net.casualuhc.uhc.resources.UHCResourcePackHost
import net.casualuhc.uhc.uhc.EasterUHC
import net.casualuhc.uhc.uhc.UHCEvents
import net.casualuhc.uhc.util.AntiCheat
import net.casualuhc.uhc.util.Config
import net.casualuhc.uhc.util.Texts
import net.casualuhc.uhc.util.UHCPlayerUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UHCMod: ModInitializer {
    companion object {
        const val ID = "uhc"

        val logger: Logger = LoggerFactory.getLogger("UHC")
        val uhc: ModContainer = FabricLoader.getInstance().getModContainer(this.ID).get()

        lateinit var minigame: UHCMinigame
    }

    override fun onInitialize() {
        UHCItems.noop()

        this.registerEvents()

        Config.registerEvents()
        UHCAdvancements.registerEvents()
        AntiCheat.registerEvents()
        CommandManager.registerEvents()
        TeamManager.registerEvents()
        DataManager.registerEvents()
        UHCResourcePackHost.registerEvents()

        UHCResourcePack.initialise()

        Dimensions.noop()

        GlobalEventHandler.register<ServerLoadedEvent>(0) {
            minigame = UHCMinigame(it.server)
        }
    }

    fun registerEvents() {
        UHCEvents.register("easter", ::EasterUHC)
    }

    // TODO:
    //   EasterUHC - Add event UHCs
    //   Duels? - Implement duel command
    //   Testing - Run tests (implement unit tests?)
    //   Minesweeper AI - Make sure all maps are non-luck based
}