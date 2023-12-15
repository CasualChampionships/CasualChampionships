package net.casual.championships.minigame.uhc.resources

import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.resources.PackInfo
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.Texts

object UHCResources: MinigameResources {
    override fun getInfo(): PackInfo? {
        return CasualResourcePackHost.getHostedPack("casual-pack")?.toPackInfo(true, Texts.PACK_MESSAGE)
    }
}