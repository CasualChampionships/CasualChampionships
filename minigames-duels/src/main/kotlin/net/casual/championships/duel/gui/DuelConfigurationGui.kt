package net.casual.championships.duel.gui

import eu.pb4.sgui.api.elements.GuiElement
import net.casual.arcade.guis.sgui.setSlot
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.PlayerUtils.hasPermission
import net.casual.arcade.utils.PlayerUtils.server
import net.casual.arcade.utils.component.gray
import net.casual.arcade.utils.component.white
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.ui.CasualSimpleGui
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.duel.minigame.DuelSettings
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.*

class DuelConfigurationGui(
    player: ServerPlayer,
    private val settings: DuelSettings,
    private val players: () -> List<ServerPlayer>,
    private val start: (ServerPlayer, List<ServerPlayer>, DuelSettings) -> Unit
): CasualSimpleGui(MenuType.GENERIC_9x6, player, true) {
    private val selectedPlayers = HashSet<UUID>()
    private val confirm: GuiElement
    private val waiting: GuiElement

    init {
        fun start() {
            val playerList = this.player.server.playerList
            this.start.invoke(this.player, this.selectedPlayers.mapNotNull(playerList::getPlayer), this.settings)
            this.close()
        }

        val settings = CasualGuiItems.GEAR
        settings.named(Component.literal("Settings").withMiniFont())
        this.setSlot(38, settings) { ->
            DuelSettingsGui(this.player, this.settings, this).open()
        }

        val kits = Items.IRON_SWORD.defaultInstance
        kits.named(Component.literal("Kits").withMiniFont())
        this.setSlot(48, kits) { ->
            DuelKitsGui(this.player, this.settings, this).open()
        }

        val allPlayers = ItemStack(CasualItems.GOLDEN_HEAD)
        allPlayers.named(Component.literal("Duel with EVERYONE").withMiniFont()).hideTooltip(DataComponents.LORE)
        this.setSlot(50, allPlayers) { ->
            val players = this.getAvailablePlayers()
            val filtered = players.stream()
                .filter { it.uuid != this.player.uuid }
                .toList()

            for (player in filtered) {
                if (!this.isPlayerSelected(player.uuid)) {
                    this.toggleSelection(player.uuid)
                }
            }

            start()
        }

        val players = ItemStack(CasualItems.FORWARD_FACING_PLAYER_HEAD)
        players.named(Component.literal("Select Players").withMiniFont())
        this.setSlot(42, players) { ->
            DuelPlayerSelectionGui(this.player, this).open()
        }

        val confirm = CasualGuiItems.TICK
        confirm.named(CasualComponents.CONFIRM.withMiniFont())
        this.confirm = GuiElement(confirm) { _, _, _, _ ->
            start()
        }
        val waiting = CasualGuiItems.GREY_TICK
        waiting.named(CasualComponents.CONFIRM.withMiniFont())
        waiting.lore(Component.literal("Select players to start!").gray().withMiniFont())
        this.waiting = GuiElement(waiting, GuiElement.EMPTY_CALLBACK)

        this.updateConfirm()

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) { ->
            this.close()
        }

        this.title = Component.empty()
            .append(SpacingFontResources.spaced(-8))
            .append(CasualComponents.Gui.DUELS.copy().white())
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
            this.setSlot(40, this.waiting)
            return
        }
        this.setSlot(40, this.confirm)
    }
}