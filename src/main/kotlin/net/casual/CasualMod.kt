package net.casual

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.managers.CommandManager
import net.casual.managers.DataManager
import net.casual.managers.TeamManager
import net.casual.minigame.uhc.advancement.UHCAdvancements
import net.casual.items.CasualItems
import net.casual.minigame.CasualMinigame
import net.casual.minigame.Dimensions
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.resources.UHCResourcePack
import net.casual.resources.CasualResourcePackHost
import net.casual.minigame.uhc.events.RegularUHC
import net.casual.minigame.uhc.events.UHCEvents
import net.casual.util.AntiCheat
import net.casual.util.Config
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CasualMod: ModInitializer {
    companion object {
        const val ID = "uhc"

        val logger: Logger = LoggerFactory.getLogger("UHC")
        val uhc: ModContainer = FabricLoader.getInstance().getModContainer(ID).get()

        lateinit var minigame: CasualMinigame
    }

    override fun onInitialize() {
        CasualItems.noop()

        this.registerEvents()

        Config.registerEvents()
        UHCAdvancements.registerEvents()
        AntiCheat.registerEvents()
        CommandManager.registerEvents()
        TeamManager.registerEvents()
        DataManager.registerEvents()
        CasualResourcePackHost.registerEvents()

        UHCResourcePack.initialise()

        Dimensions.noop()

        GlobalEventHandler.register<ServerLoadedEvent>(0) {
            minigame = UHCMinigame(it.server, UHCEvents.getUHC(Config.getString("event"))!!)
        }
        GlobalEventHandler.register<PlayerJoinEvent>(0) {
            minigame.addPlayer(it.player)
        }
    }

    fun registerEvents() {
        UHCEvents.register("regular", ::RegularUHC)
    }

    // TODO:
    //   Duels? - Implement duel command
    //   Testing - Run tests (implement unit tests?)
    //   Minesweeper AI - Make sure all maps are non-luck based
}