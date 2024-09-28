package net.casual.championships.duel.ui

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.SimpleGui
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.grey
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.white
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.visuals.screen.setSlot
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.ui.CommonSimpleGui
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonItems
import net.casual.championships.duel.DuelSettings
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import java.util.*

class DuelConfigurationGui(
    player: ServerPlayer,
    private val settings: DuelSettings,
    private val players: () -> List<ServerPlayer>,
    private val start: (ServerPlayer, List<ServerPlayer>, DuelSettings) -> Unit
): CommonSimpleGui(MenuType.GENERIC_9x6, player, true) {
    private val selectedPlayers = HashSet<UUID>()
    private val confirm: GuiElement
    private val waiting: GuiElement

    init {
        val settings = MenuItem.GEAR
        settings.named("Settings".literal().mini())
        this.setSlot(47, settings) { ->
            DuelSettingsGui(this.player, this.settings, this).open()
        }

        val players = ItemStack(CommonItems.FORWARD_FACING_PLAYER_HEAD)
        players.named("Select Players".literal().mini())
        this.setSlot(51, players) { ->
            DuelPlayerSelectionGui(this.player, this).open()
        }

        val confirm = MenuItem.TICK
        confirm.named(CommonComponents.CONFIRM.mini())
        this.confirm = GuiElement(confirm) { _, _, _, _ ->
            val playerList = this.player.server.playerList
            this.start.invoke(this.player, this.selectedPlayers.mapNotNull(playerList::getPlayer), this.settings)
            this.close()
        }
        val waiting = MenuItem.GREY_TICK
        waiting.named(CommonComponents.CONFIRM.mini())
        waiting.lore("Select players to start!".literal().grey().mini())
        this.waiting = GuiElement(waiting, GuiElement.EMPTY_CALLBACK)

        this.setSlot(49, this.waiting)

        this.setSlot(58, MenuItem.RED_BACK.hideTooltip()) { ->
            this.close()
        }

        this.title = Component.empty()
            .append(ComponentUtils.space(-8))
            .append(CommonComponents.Gui.DUELS.white())
    }

    fun getAvailablePlayers(): List<ServerPlayer> {
        return this.players.invoke()
    }

    fun isPlayerSelected(uuid: UUID): Boolean {
        return this.selectedPlayers.contains(uuid)
    }

    fun toggleSelection(uuid: UUID): Boolean {
        val wasSelected = !this.selectedPlayers.add(uuid)
        if (wasSelected) {
            this.selectedPlayers.remove(uuid)
        }
        val element = if (this.selectedPlayers.isEmpty()) this.waiting else this.confirm
        this.setSlot(49, element)
        return !wasSelected
    }
}