package net.casual.championships.common.ui.game

import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.resources.utils.spaced
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.white
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.ui.CasualContainerGui
import net.casual.championships.common.util.CasualComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam

class TeamSelectorGui(
    player: ServerPlayer,
    selections: List<Selection>
): CasualContainerGui(ContainerType.Generic9x6, player, true) {
    init {
        this.setTitle(Component {
            empty() + spaced(-8.0F) + CasualComponents.Gui.TEAM_SELECTOR.copy().white()
        })

        var row = 1
        var column = 1
        for (selection in selections.take(12)) {
            val slot = row * 9 + column
            this.setSlot(slot, selection.display) {
                val profiles = selection.team.getOnlinePlayers().map(ServerPlayer::getGameProfile)
                val gui = PlayerSelectorGui(this.player, profiles)
                gui.setParent(this)
                gui.open()
            }
            column += 2
            if (column > 7) {
                column = 1
                row += 2
            }
        }

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.close()
        }
    }

    data class Selection(val team: PlayerTeam, val display: ItemStack)
}

