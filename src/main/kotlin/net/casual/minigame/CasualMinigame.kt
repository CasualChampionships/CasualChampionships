package net.casual.minigame

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.utils.ComponentUtils.aqua
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.TickUtils
import net.casual.managers.TeamManager
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.events.UHCEvents
import net.casual.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

object CasualMinigame {
    private var minigame: Minigame<*>? = null

    fun getCurrent(): Minigame<*> {
        return this.minigame!!
    }

    fun setLobby(server: MinecraftServer, next: Minigame<*>? = null): CasualLobbyMinigame {
        val lobby = CasualLobbyMinigame(server, UHCEvents.getUHC().getLobby())
        this.setNewMinigameAndStart(lobby)
        if (next != null) {
            lobby.setNextMinigame(next)
        }
        return lobby
    }

    fun setNewMinigameAndStart(minigame: Minigame<*>) {
        val current = this.minigame
        this.minigame = minigame
        if (current != null) {
            for (player in current.getPlayers()) {
                minigame.addPlayer(player)
            }
            current.close()
        }
        minigame.start()
        minigame.setTabDisplay(this.createTabDisplay())
    }

    internal fun registerEvents() {
        Minigames.registerFactory("casual_uhc") { UHCMinigame(it) }

        GlobalEventHandler.register<ServerLoadedEvent>(0) {
            this.setLobby(it.server, UHCMinigame(it.server))
        }
        GlobalEventHandler.register<PlayerJoinEvent>(0) {
            this.getCurrent().addPlayer(it.player)
        }
    }

    private fun createTabDisplay(): ArcadeTabDisplay {
        val display = ArcadeTabDisplay(
            ComponentSupplier.of(
                Component.literal("\n")
                    .append(Texts.ICON_UHC)
                    .append(Texts.space())
                    .append(Texts.CASUAL_UHC.gold().bold())
                    .append(Texts.space())
                    .append(Texts.ICON_UHC)
            )
        ) { _ ->
            val tps = TickUtils.calculateTPS()
            val formatting = if (tps >= 20) ChatFormatting.DARK_GREEN else if (tps > 15) ChatFormatting.YELLOW else if (tps > 10) ChatFormatting.RED else ChatFormatting.DARK_RED
            Component.literal("\n")
                .append("TPS: ")
                .append(Component.literal("%.1f".format(tps)).withStyle(formatting))
                .append("\n")
                .append(Texts.TAB_HOSTED.aqua().bold())
        }
        return display
    }
}