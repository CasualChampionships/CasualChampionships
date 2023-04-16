package net.casualuhc.uhcmod.uhc.handlers

import net.casualuhc.uhcmod.resources.UHCResourcePackHost
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.HeadUtils
import net.casualuhc.uhcmod.util.Texts
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
}