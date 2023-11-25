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
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.TickUtils
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.events.UHCEvents
import net.casual.util.CasualUtils
import net.casual.util.Config
import net.casual.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.util.*

object CasualMinigame {
    private var uuid: String? by Config.stringOrNull("current_minigame_uuid")
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
        this.uuid = minigame.uuid.toString()
        if (current != null) {
            for (player in current.getPlayers()) {
                minigame.addPlayer(player)
            }
            current.close()
        }
        minigame.start()
    }

    internal fun registerEvents() {
        Minigames.registerFactory(CasualUtils.id("uhc_minigame")) { UHCMinigame(it) }

        GlobalEventHandler.register<ServerLoadedEvent>(Int.MAX_VALUE) {
            val uuid = this.uuid
            if (uuid != null) {
                val current = Minigames.get(UUID.fromString(uuid))
                this.minigame = current
                if (current != null) {
                    return@register
                }
            }

            this.setLobby(it.server)
        }
        GlobalEventHandler.register<PlayerJoinEvent> {
            val player = it.player
            if (player.getMinigame() == null) {
                this.getCurrent().addPlayer(it.player)
            }
        }
    }

    fun createTabDisplay(): ArcadeTabDisplay {
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