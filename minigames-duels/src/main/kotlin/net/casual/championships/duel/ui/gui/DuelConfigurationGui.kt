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
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
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
        this.setSlot(38, settings) {
            DuelSettingsGui(this.player, this.settings, this).open()
        }

        val kits = Items.IRON_SWORD.defaultInstance
        kits.named(Component.literal("Kits").withMiniFont())
            .hideTooltip(DataComponents.ATTRIBUTE_MODIFIERS)
        this.setSlot(48, kits) {
            DuelKitsGui(this.player, this.settings, this).open()
        }

        val allPlayers = ItemStack(CasualItems.GOLDEN_HEAD)
        allPlayers.named(Component.literal("Duel with EVERYONE").withMiniFont())
            .hideTooltip(DataComponents.LORE)
        this.setSlot(50, allPlayers) {
            val players = this.getAvailablePlayers()
            val filtered = players.stream()
                .filter { it.uuid != this.player.uuid }
                .toList()

            for (player in filtered) {
                if (!this.isPlayerSelected(player.uuid)) {
                    this.toggleSelection(player.uuid)
                }
            }

            this.start()
        }

        val players = ItemStack(CasualItems.FORWARD_FACING_PLAYER_HEAD)
        players.named(Component.literal("Select Players").withMiniFont())
        this.setSlot(42, players) {
            DuelPlayerSelectionGui(this.player, this).open()
        }

        this.updateConfirm()

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.close()
        }
    }

    fun start() {
        val playerList = this.player.server.playerList
        this.start.invoke(this.player, this.selectedPlayers.mapNotNull(playerList::getPlayer), this.settings)
        this.close()
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
            this.setSlot(40, waiting)
            return
        }

        val confirm = CasualGuiItems.TICK.named(CasualComponents.CONFIRM.withMiniFont())
        this.setSlot(40, confirm) {
            this.start()
        }
    }
}