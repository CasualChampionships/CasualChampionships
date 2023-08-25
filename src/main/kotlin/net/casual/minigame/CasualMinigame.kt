package net.casual.minigame

import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.SavableMinigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import java.nio.file.Path

abstract class CasualMinigame(
    id: ResourceLocation,
    server: MinecraftServer,
    path: Path
): SavableMinigame(id, server, path) {
    abstract fun canReadyUp(): Boolean

    abstract fun getResources(): MinigameResources
}