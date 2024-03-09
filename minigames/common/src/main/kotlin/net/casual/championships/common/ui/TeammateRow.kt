package net.casual.championships.common.ui

import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.sidebar.SidebarSupplier
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.championships.common.util.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer

class TeammateRow(private val index: Int, private val buffer: Component): SidebarSupplier {
    private val none = SidebarComponent.withCustomScore(this.createTeammate(), CommonComponents.UNAVAILABLE_BITMAP.append(this.buffer))

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
                health.literal().append(ComponentUtils.space(1)).append(CommonComponents.HARDCORE_HEART_BITMAP)
            } else {
                CommonComponents.UNAVAILABLE_BITMAP
            }
        } else {
            CommonComponents.NO_CONNECTION_BITMAP
        }
        return SidebarComponent.withCustomScore(formatted, score.append(this.buffer))
    }

    private fun createTeammate(name: Component = Component.empty()): MutableComponent {
        return Component.empty().append(this.buffer).append(" - ").append(name)
    }
}