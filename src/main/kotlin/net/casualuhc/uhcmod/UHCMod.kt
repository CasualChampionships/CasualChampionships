package net.casualuhc.uhcmod

import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.managers.*
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

        Config.registerEvents()
    }

    // TODO:
    //   ResourcePack - Update pack for translations
    //   EasterUHC - Add event UHCs
    //   DiscordBot - Update the discord bot for the new DB
    //   Duels? - Implement duel command
    //   Testing - Run tests (implement unit tests?)
    //   Minesweeper AI - Make sure all maps are non-luck based
}