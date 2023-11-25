package net.casual.minigame.uhc.gui

import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.extensions.TeamFlag
import net.casual.extensions.TeamFlagsExtension.Companion.flags
import net.casual.util.Texts
import net.casual.util.Texts.monospaced
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class TeammateRow(private val index: Int, private val buffer: String = ""): ComponentSupplier {
    override fun getComponent(player: ServerPlayer): Component {
        val team = player.team
        if (team == null || team.flags.has(TeamFlag.Ignored)) {
            return Component.literal("${this.buffer} - ").monospaced().append(Texts.ICON_CROSS)
        }
        val players = team.players
        if (this.index >= players.size) {
            return Component.literal("${this.buffer} - ").monospaced().append(Texts.ICON_CROSS)
        }

        val name: String
        val teammate: ServerPlayer?
        if (this.index == 0) {
            name = player.scoreboardName
            teammate = player
        } else {
            name = players.filter { it != player.scoreboardName }[this.index - 1]
            teammate = PlayerUtils.player(name)
        }

        val length = (players.maxOfOrNull { it.length } ?: 16).coerceAtLeast(name.length)

        val longName = Component.literal(name + " ".repeat(length - name.length)).withStyle(team.color)
        val start = Component.literal("${this.buffer} - ").append(longName).append(" ").monospaced()
        if (teammate !== null) {
            if (teammate.isSurvival && teammate.isAlive) {
                val health = " %04.1f".format(teammate.health / 2.0)
                return start.append(health).append(Texts.space(1)).append(Texts.ICON_HEART)
            }
            return start.append("     ").append(Texts.ICON_CROSS)
        }
        return start.append("     ").append(Texts.ICON_NO_CONNECTION)
    }
}