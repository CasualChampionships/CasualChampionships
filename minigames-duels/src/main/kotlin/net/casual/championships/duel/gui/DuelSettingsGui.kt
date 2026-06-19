package net.casual.championships.duel.gui

import eu.pb4.sgui.api.ClickType
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.minigame.settings.display.MenuGameSetting
import net.casual.arcade.resources.utils.spaced
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.white
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.ui.CasualContainerGui
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.duel.minigame.DuelSettings
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.ContainerInput

class DuelSettingsGui(
    player: ServerPlayer,
    private val settings: DuelSettings,
    private val configuration: DuelConfigurationGui
): CasualContainerGui(ContainerType.Generic9x6, player, true) {
    init {
        this.setParent(this.configuration)

        this.setTitle(Component {
            empty() + spaced(-8.0F) + CasualComponents.Gui.DUEL_SETTINGS.copy().white()
        })

        this.createHorizontalSetting(19, this.settings.displayableTeams)
        this.createHorizontalSetting(28, this.settings.displayableGlowing)
        this.createHorizontalSetting(37, this.settings.displayableNaturalRegen)
        this.createHorizontalSetting(46, this.settings.displayablePlayerDropHeads)

        this.createVerticalSetting(23, this.settings.displayableHealth)
        this.createVerticalSetting(24, this.settings.displayableArena)
        this.createVerticalSetting(25, this.settings.displayableArenaSize)

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.openParentOrClose()
        }
    }

    private fun createHorizontalSetting(slot: Int, setting: MenuGameSetting<*>) {
        this.setSlot(slot, setting.display)
        for ((i, option) in setting.options.withIndex()) {
            this.setSlot(slot + i + 1, option.itemStack) {
                option.guiCallback.click(0, ClickType.MOUSE_RIGHT, ContainerInput.PICKUP, null)
            }
        }
    }

    private fun createVerticalSetting(slot: Int, setting: MenuGameSetting<*>) {
        this.setSlot(slot, setting.display)
        for ((i, option) in setting.options.withIndex()) {
            this.setSlot(slot + (i + 1) * 9, option.itemStack) {
                option.guiCallback.click(0, ClickType.MOUSE_RIGHT, ContainerInput.PICKUP, null)
            }
        }
    }
}