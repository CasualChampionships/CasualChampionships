package net.casual.minigame.uhc.resources

import net.casual.arcade.minigame.MinigameResources
import net.casual.resources.CasualResourcePackHost
import net.casual.util.Config
import net.casual.util.Texts
import net.minecraft.server.MinecraftServer

object UHCResources: MinigameResources {
    override fun getInfo(): MinecraftServer.ServerResourcePackInfo? {
        val hosted = CasualResourcePackHost.getHostedPack("uhc-pack") ?: return null
        return MinecraftServer.ServerResourcePackInfo(hosted.url, hosted.hash, !Config.dev, Texts.PACK_MESSAGE)
    }
}