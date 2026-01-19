package net.casual.championships.duel.gui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementInterface
import net.casual.arcade.guis.sgui.setSlot
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.component.white
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.ui.CasualSimpleGui
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.duel.minigame.DuelSettings
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

class DuelKitsGui(
    player: ServerPlayer,
    private val settings: DuelSettings,
    private val configuration: DuelConfigurationGui
): CasualSimpleGui(MenuType.GENERIC_9x6, player, true) {
    init {
        this.setParent(this.configuration)

        this.title = Component.empty()
            .append(SpacingFontResources.spaced(-8))
            .append(CasualComponents.Gui.DUEL_KITS.copy().white())

        val kitSlots = KIT_SLOTS.iterator()
        for (option in this.settings.displayableKit.options) {
            if (!kitSlots.hasNext()) {
                break
            }
            this.setSlot(kitSlots.next(), option)
        }

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) { ->
            this.openParentOrClose()
        }
    }

    override fun onClick(
        index: Int,
        type: ClickType,
        action: net.minecraft.world.inventory.ClickType,
        element: GuiElementInterface?
    ): Boolean {
        super.onClick(index, type, action, element)

        if (element != null && index in KIT_SLOTS) {
            this.openParentOrClose()
            return true
        }
        return false
    }

    companion object {
        val KIT_SLOTS = (38..42) + (47..51)
    }
}