package net.casualuhc.uhcmod

import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.managers.*
import net.casualuhc.uhcmod.minigame.Dimensions
import net.casualuhc.uhcmod.resources.UHCResourcePackHost
import net.casualuhc.uhcmod.util.AntiCheat
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.Texts
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class UHCMod: ModInitializer {
    companion object {
        val logger: Logger = LoggerFactory.getLogger("UHC")
    }

    override fun onInitialize() {
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

        Dimensions.noop()

        Config.registerEvents()
    }

    // TODO:
    //   Revoking advancements
    //   EasterUHC - Add event UHCs
    //   Display player health to spectators
    //   Make player's only spectate teams?
    //   Duels? - Implement duel command
    //   Testing - Run tests (implement unit tests?)
    //   Minesweeper AI - Make sure all maps are non-luck based
}