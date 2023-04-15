package net.casualuhc.uhcmod.uhc

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.utils.LevelUtils
import net.casualuhc.uhcmod.uhc.handlers.BossBarHandler
import net.casualuhc.uhcmod.uhc.handlers.LobbyHandler
import net.casualuhc.uhcmod.uhc.handlers.ResourceHandler
import net.casualuhc.uhcmod.uhc.impl.BoxLobbyHandler
import net.minecraft.core.Vec3i

enum class DefaultUHC: UHCEvent {
    INSTANCE;

    private val lobby: LobbyHandler
    private val boss: BossBarHandler
    private val resource: ResourceHandler

    init {
        this.lobby = BoxLobbyHandler(Vec3i(0, 300, 0), 40, 10, LevelUtils.overworld())
        this.boss = object: BossBarHandler { }
        this.resource = object: ResourceHandler { }
    }

    override fun getName(): String {
        return "default"
    }

    override fun getLobbyHandler(): LobbyHandler {
        return this.lobby
    }

    override fun getBossBarHandler(): BossBarHandler {
        return this.boss
    }

    override fun getResourcePackHandler(): ResourceHandler {
        return this.resource
    }

    override fun load() {
        Arcade.server.motd = "            §6፠ §bWelcome to Casual UHC! §6፠\n     §6Yes, it's back! Is your team prepared?"
    }
}