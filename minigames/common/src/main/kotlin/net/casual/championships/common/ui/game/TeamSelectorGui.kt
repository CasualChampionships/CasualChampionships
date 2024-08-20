package net.casual.championships.common.ui.game

import eu.pb4.sgui.api.gui.SimpleGui
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.white
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.util.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.scores.PlayerTeam

class TeamSelectorGui(
    player: ServerPlayer,
    selections: List<Selection>
): SimpleGui(MenuType.GENERIC_9x6, player, true) {
    init {
        this.title = Component.empty()
            .append(ComponentUtils.space(-8))
            .append(CommonComponents.Gui.TEAM_SELECTOR.white())

        var row = 1
        var column = 1
        for (selection in selections.take(12)) {
            val slot = row * 9 + column
            this.setSlot(slot, selection.display) { _, _, _, _ ->
                PlayerSelectorGui(
                    this.player,
                    selection.team.getOnlinePlayers().map(ServerPlayer::getGameProfile),
                    this
                ).open()
            }
            column += 2
            if (column > 7) {
                column = 1
                row += 2
            }
        }

        this.setSlot(58, MenuItem.RED_BACK.hideTooltip()) { _, _, _, _ ->
            this.close()
        }
    }

    data class Selection(val team: PlayerTeam, val display: ItemStack = ItemUtils.colouredHeadForFormatting(team.color))
}

