package net.casual.championships.duel.ui.gui

import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.pack.utils.spaced
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.gray
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.white
import net.casual.arcade.utils.player.hasPermission
import net.casual.arcade.utils.player.server
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.ui.CasualContainerGui
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.duel.minigame.DuelSettings
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.item.ItemStack
import java.util.*

class DuelConfigurationGui(
    player: ServerPlayer,
    private val settings: DuelSettings,
    private val players: () -> List<ServerPlayer>,
    private val start: (ServerPlayer, List<ServerPlayer>, DuelSettings) -> Unit
): CasualContainerGui(ContainerType.Generic9x6, player, true) {
    private val selectedPlayers = HashSet<UUID>()

    init {
        this.setTitle(Component {
            empty() + spaced(-8.0F) + CasualComponents.Gui.DUELS.copy().white()
        })

        val settings = CasualGuiItems.GEAR
        settings.named(Component.literal("Settings").withMiniFont())
        this.setSlot(47, settings) {
            val gui = this.settings.gui(this.player)
            gui.setParent(this)
            gui.open()
            // DuelSettingsGui(this.player, this.settings, this).open()
        }

        val players = ItemStack(CasualItems.FORWARD_FACING_PLAYER_HEAD)
        players.named(Component.literal("Select Players").withMiniFont())
        this.setSlot(51, players) {
            DuelPlayerSelectionGui(this.player, this).open()
        }

        this.updateConfirm()

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.close()
        }
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
        this.updateConfirm()
        return !wasSelected
    }

    private fun updateConfirm() {
        if (this.selectedPlayers.isEmpty() && !this.player.hasPermission(PermissionLevel.OWNERS)) {
            val waiting = CasualGuiItems.GREY_TICK
                .named(CasualComponents.CONFIRM.withMiniFont())
                .lore(Component.literal("Select players to start!").gray().withMiniFont())
            this.setSlot(49, waiting)
            return
        }

        val confirm = CasualGuiItems.TICK.named(CasualComponents.CONFIRM.withMiniFont())
        this.setSlot(49, confirm) {
            val playerList = this.player.server.playerList
            this.start.invoke(this.player, this.selectedPlayers.mapNotNull(playerList::getPlayer), this.settings)
            this.close()
        }
    }
}