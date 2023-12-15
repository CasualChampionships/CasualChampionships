package net.casual.championships.level

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import xyz.nucleoid.fantasy.RuntimeWorld
import xyz.nucleoid.fantasy.RuntimeWorldConfig

class VanillaLikeRuntimeLevel(
    server: MinecraftServer,
    registryKey: ResourceKey<Level>,
    config: RuntimeWorldConfig,
    style: Style,
    val dimensionLike: ResourceKey<Level>
): RuntimeWorld(server, registryKey, config, style) {
}