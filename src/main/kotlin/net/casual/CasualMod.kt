package net.casual

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.extensions.PlayerFlagsExtension
import net.casual.extensions.PlayerStatsExtension
import net.casual.extensions.PlayerUHCExtension
import net.casual.items.CasualItems
import net.casual.managers.CommandManager
import net.casual.managers.DataManager
import net.casual.managers.TeamManager
import net.casual.minigame.CasualMinigame
import net.casual.minigame.Dimensions
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.advancement.UHCAdvancements
import net.casual.minigame.uhc.events.DefaultUHC
import net.casual.minigame.uhc.events.RegularUHC
import net.casual.minigame.uhc.events.UHCEvents
import net.casual.minigame.uhc.resources.UHCResourcePack
import net.casual.resources.CasualResourcePackHost
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
            minigame = UHCMinigame(it.server, UHCEvents.getUHC(Config.getStringOrDefault("event", "default")) ?: DefaultUHC)
        }
        GlobalEventHandler.register<PlayerJoinEvent>(0) {
            minigame.addPlayer(it.player)
        }
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerFlagsExtension(player))
            player.addExtension(PlayerUHCExtension(player))
            player.addExtension(PlayerStatsExtension())
        }
    }

    fun registerEvents() {
        UHCEvents.register("regular", ::RegularUHC)
    }

    // TODO:
    //   Fix health being set when forceadding
    //   Fix sidebar
    //   Fix worldborder
    //   Fix stats? Again
    //   Fix server replay
    //   Implement podium
    //   Restructure UHC stuff
    //   Minesweeper AI - Make sure all maps are non-luck based
}