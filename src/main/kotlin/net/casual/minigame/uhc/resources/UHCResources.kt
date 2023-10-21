package net.casual.minigame.uhc.resources

import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.resources.PackInfo
import net.casual.resources.CasualResourcePackHost
import net.casual.util.Config
import net.casual.util.Texts
import net.minecraft.server.MinecraftServer

object UHCResources: MinigameResources {
    override fun getInfo(): PackInfo? {
        return CasualResourcePackHost.getHostedPack("casual-pack")?.toPackInfo(true, Texts.PACK_MESSAGE)
        // TODO: Fix CasualResourcePackHost
        // return PackInfo(
        //     "https://download.mc-packs.net/pack/99c112b0749ace0a6fd82bc91140eb75784b2e77.zip",
        //     "99c112b0749ace0a6fd82bc91140eb75784b2e77",
        //     !Config.dev,
        //     Texts.PACK_MESSAGE
        // )
    }
}