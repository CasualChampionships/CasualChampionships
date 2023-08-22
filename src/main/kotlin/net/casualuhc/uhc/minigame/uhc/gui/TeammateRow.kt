package net.casualuhc.uhc.minigame.uhc.gui

import net.casualuhc.arcade.gui.suppliers.ComponentSupplier
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.uhc.extensions.PlayerUHCExtension.Companion.uhc
import net.casualuhc.uhc.extensions.TeamFlag
import net.casualuhc.uhc.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhc.extensions.TeamUHCExtension.Companion.uhc
import net.casualuhc.uhc.util.Texts
import net.casualuhc.uhc.util.Texts.monospaced
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class TeammateRow(val index: Int, val buffer: String = ""): ComponentSupplier {
    override fun getComponent(player: ServerPlayer): Component {
        val team = player.uhc.originalTeam ?: player.team
        if (team == null || team.flags.has(TeamFlag.Ignored)) {
            return Component.literal("${this.buffer} - ").monospaced().append(Texts.ICON_CROSS)
        }
        val players = team.uhc.players
        if (this.index >= players.size) {
            return Component.literal("${this.buffer} - ").monospaced().append(Texts.ICON_CROSS)
        }

        val length = (players.maxOfOrNull { it.length } ?: 16)

        val name: String
        val teammate: ServerPlayer?
        if (this.index == 0) {
            name = player.scoreboardName
            teammate = player
        } else {
            name = players.filter { it != player.scoreboardName }[this.index - 1]
            teammate = PlayerUtils.player(name)
        }
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