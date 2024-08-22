package net.casual.championships.duel.ui

import net.casual.arcade.gui.screen.SimpleNestedGui
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.white
import net.casual.arcade.utils.ComponentUtils.yellow
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils.setSlot
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonItems
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

class DuelPlayerSelectionGui(
    player: ServerPlayer,
    private val configuration: DuelConfigurationGui
): SimpleNestedGui(MenuType.GENERIC_9x6, player, true) {
    private var page = 0

    init {
        this.setParent(this.configuration)

        this.setSlot(58, MenuItem.RED_BACK.hideTooltip()) { ->
            this.openParentOrClose()
        }

        this.loadPlayers()

        this.title = Component.empty()
            .append(ComponentUtils.space(-8))
            .append(CommonComponents.Gui.PLAYER_SELECTOR.white())
    }

    private fun loadPlayers() {
        this.clearPlayers()

        val players = this.configuration.getAvailablePlayers()
        val filtered = players.stream()
            .filter { it.uuid != this.player.uuid }
            .skip(this.page * 12L)
            .limit(12)
            .toList()

        // If all the players suddenly leave, we just jump
        // back to the first page to avoid many empty pages
        if (filtered.isEmpty() && this.page != 0) {
            this.page = 0
            this.loadPlayers()
            return
        }

        var row = 1
        var column = 1
        for (player in filtered) {
            val slot = row * 9 + column
            val head = ItemUtils.createPlayerHead(player, CommonItems.FORWARD_FACING_PLAYER_HEAD)
            if (this.configuration.isPlayerSelected(player.uuid)) {
                this.setSlot(slot - 9, MenuItem.GREEN_HIGHLIGHT.hideTooltip())
            }
            val name = Component.literal(player.scoreboardName).yellow().mini()
            this.setSlot(slot, head.named(name)) { _, _, _, _ ->
                if (this.configuration.toggleSelection(player.uuid)) {
                    this.setSlot(slot - 9, MenuItem.GREEN_HIGHLIGHT.hideTooltip())
                } else {
                    this.clearSlot(slot - 9)
                }
            }
            column += 2
            if (column > 7) {
                column = 1
                row += 2
            }
        }

        if (this.page != 0) {
            this.setSlot(57, MenuItem.RED_LEFT.hideTooltip()) { ->
                this.page -= 1
                this.loadPlayers()
            }
        } else {
            this.setSlot(57, MenuItem.GREY_RED_LEFT.hideTooltip())
        }
        if ((this.page + 1) * 12 < players.size - 1) {
            this.setSlot(59, MenuItem.RED_RIGHT.hideTooltip()) { ->
                this.page += 1
                this.loadPlayers()
            }
        } else {
            this.setSlot(59, MenuItem.GREY_RED_RIGHT.hideTooltip())
        }
    }

    private fun clearPlayers() {
        repeat(3) { row ->
            repeat(4) { column ->
                val slot = (column * 2 + 1) + (row * 2 + 1) * 9
                this.clearSlot(slot)
                this.clearSlot(slot - 9)
            }
        }
    }
}