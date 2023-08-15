package net.casualuhc.uhc.uhc.handlers

import net.casualuhc.uhc.resources.UHCResourcePackHost
import net.casualuhc.uhc.util.Config
import net.casualuhc.uhc.util.HeadUtils
import net.casualuhc.uhc.util.Texts
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

interface ResourceHandler {
    fun getInfo(): MinecraftServer.ServerResourcePackInfo? {
        val hosted = UHCResourcePackHost.getHostedPack("uhc-pack") ?: return null
        return MinecraftServer.ServerResourcePackInfo(hosted.url, hosted.hash, !Config.booleanOrDefault("dev", false), Texts.PACK_MESSAGE)
    }

    fun getInfo(player: ServerPlayer): MinecraftServer.ServerResourcePackInfo? {
        return this.getInfo()
    }

    fun getGoldenHeadTexture(): String {
        return HeadUtils.GOLDEN
    }

    companion object {
        fun default(): ResourceHandler {
            return object: ResourceHandler { }
        }
    }
}