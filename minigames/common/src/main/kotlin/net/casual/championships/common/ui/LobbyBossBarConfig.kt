package net.casual.championships.common.ui

import com.google.gson.JsonObject
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.minigame.events.lobby.ui.TimerBossBarConfig
import net.casual.arcade.minigame.events.lobby.ui.TimerBossBarConfigFactory

class LobbyBossBarConfig: TimerBossBarConfig {
    override val id: String = LobbyBossBarConfig.id

    override fun create(): TimerBossBar {
        return LobbyBossBar()
    }

    override fun write(): JsonObject {
        return JsonObject()
    }

    companion object: TimerBossBarConfigFactory {
        override val id: String = "casual_lobby"

        override fun create(data: JsonObject): TimerBossBarConfig {
            return LobbyBossBarConfig()
        }
    }
}