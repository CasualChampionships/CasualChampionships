package net.casual.championships.minigame.uhc.resources

import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.resources.PackInfo
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.Config
import net.casual.championships.util.Texts

object UHCResources: MinigameResources {
    override fun getPacks(): Collection<PackInfo> {
        val pack = CasualResourcePackHost.getHostedPack("casual-pack")?.toPackInfo(!Config.dev, Texts.PACK_MESSAGE)
        return listOf(pack ?: return super.getPacks())
    }
}