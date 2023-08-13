package net.casualuhc.uhcmod

import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.items.UHCItems
import net.casualuhc.uhcmod.managers.*
import net.casualuhc.uhcmod.minigame.Dimensions
import net.casualuhc.uhcmod.resources.UHCResourcePack
import net.casualuhc.uhcmod.resources.UHCResourcePackHost
import net.casualuhc.uhcmod.uhc.EasterUHC
import net.casualuhc.uhcmod.uhc.UHCEvents
import net.casualuhc.uhcmod.util.AntiCheat
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.Texts
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
    }

    override fun onInitialize() {
        UHCItems.noop()

        this.registerEvents()

        Texts.registerEvents()
        UHCManager.registerEvents()
        UHCAdvancements.registerEvents()
        PlayerManager.registerEvents()
        AntiCheat.registerEvents()
        CommandManager.registerEvents()
        TeamManager.registerEvents()
        DataManager.registerEvents()
        WorldBorderManager.registerEvents()
        UHCResourcePackHost.registerEvents()

        UHCResourcePack.initialise()

        Dimensions.noop()

        Config.registerEvents()
    }

    fun registerEvents() {
        UHCEvents.register("easter", ::EasterUHC)
    }

    // TODO:
    //   Fix Glow?
    //   Nerf World Borders
    //   EasterUHC - Add event UHCs
    //   Duels? - Implement duel command
    //   Testing - Run tests (implement unit tests?)
    //   Minesweeper AI - Make sure all maps are non-luck based
}