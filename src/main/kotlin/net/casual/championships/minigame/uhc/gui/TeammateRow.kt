package net.casual.championships.minigame.uhc.gui

import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.sidebar.SidebarSupplier
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.championships.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

class TeammateRow(private val index: Int, private val buffer: Component): SidebarSupplier {
    private val none = SidebarComponent.withCustomScore(this.createTeammate(), Texts.ICON_CROSS.append(this.buffer))

    override fun getComponent(player: ServerPlayer): SidebarComponent {
        val team = player.team
        val minigame = player.getMinigame()
        if (team == null || (minigame != null && minigame.teams.isTeamIgnored(team))) {
            return this.none
        }
        val players = team.players
        if (this.index >= players.size) {
            return this.none
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

        val formatted = this.createTeammate(name.literal().withStyle(team.color))
        val score = if (teammate != null) {
            if (teammate.isSurvival && teammate.isAlive) {
                val health = " %04.1f".format(teammate.health / 2.0)
                health.literal().append(Texts.space(1)).append(Texts.ICON_HEART)
            } else {
                Texts.ICON_CROSS
            }
        } else {
            Texts.ICON_NO_CONNECTION
        }
        return SidebarComponent.withCustomScore(formatted, score.append(this.buffer))
    }

    private fun createTeammate(name: Component = Component.empty()): MutableComponent {
        return Component.empty().append(this.buffer).append(" - ").append(name)
    }
}