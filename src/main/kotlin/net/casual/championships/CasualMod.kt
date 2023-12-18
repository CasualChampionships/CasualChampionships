package net.casual.championships

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.championships.extensions.PlayerFlagsExtension
import net.casual.championships.extensions.PlayerUHCExtension
import net.casual.championships.items.CasualItems
import net.casual.championships.managers.CommandManager
import net.casual.championships.managers.DataManager
import net.casual.championships.managers.TeamManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.Dimensions
import net.casual.championships.minigame.uhc.events.RegularUHC
import net.casual.championships.minigame.uhc.events.UHCEvents
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.AntiCheat
import net.casual.championships.util.Config
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CasualMod: DedicatedServerModInitializer {
    companion object {
        const val ID = "casual"

        val logger: Logger = LoggerFactory.getLogger("Casual")
        val casual: ModContainer = FabricLoader.getInstance().getModContainer(ID).get()
    }

    override fun onInitializeServer() {
        logger.info("Starting CasualChampionships... Version: ${casual.metadata.version}")

        CasualItems.noop()

        this.registerEvents()

        Config.registerEvents()
        AntiCheat.registerEvents()
        CommandManager.registerEvents()
        TeamManager.registerEvents()
        DataManager.registerEvents()
        CasualResourcePackHost.registerEvents()
        CasualMinigames.registerEvents()

        CasualResourcePack.initialise()

        Dimensions.noop()

        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerFlagsExtension(player.connection))
            player.addExtension(PlayerUHCExtension())
        }
    }

    fun registerEvents() {
        UHCEvents.register("regular", ::RegularUHC)
    }

    // TODO:
    //   Implement podium
    //   Restructure UHC stuff
    //   Minesweeper AI - Make sure all maps are non-luck based
}