package net.casual.championships.common.ui

import net.casual.arcade.font.heads.PlayerHeadComponents
import net.casual.arcade.font.heads.PlayerHeadFont
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
import net.minecraft.world.scores.Team

class TeammateRow(private val index: Int, private val buffer: Component): SidebarSupplier {
    private val none = SidebarComponent.withCustomScore(
        Component.empty().append(this.buffer).append(" - "),
        CommonComponents.Bitmap.UNAVAILABLE.append(this.buffer)
    )

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

        val formatted = this.createTeammate(name, team)
        val score = if (teammate != null) {
            if (teammate.isSurvival && teammate.isAlive) {
                val health = " %04.1f".format(teammate.health / 2.0)
                health.literal().append(ComponentUtils.space(1)).append(CommonComponents.Bitmap.HARDCORE_HEART)
            } else {
                CommonComponents.Bitmap.UNAVAILABLE
            }
        } else {
            CommonComponents.Bitmap.NO_CONNECTION
        }
        return SidebarComponent.withCustomScore(formatted, score.append(this.buffer))
    }

    private fun createTeammate(name: String, team: Team): MutableComponent {
        return Component.empty().apply {
            append(buffer)
            append(" ")
            append(PlayerHeadComponents.getHeadOrDefault(name))
            append(" ")
            append(name.literal().withStyle(team.color))
        }
    }
}