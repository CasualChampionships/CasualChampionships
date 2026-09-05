package net.casual.championships.duel.ui.gui

import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.pack.utils.spaced
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.white
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.ui.CasualContainerGui
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.duel.minigame.DuelSettings
import net.minecraft.server.level.ServerPlayer

class DuelKitsGui(
    player: ServerPlayer,
    private val settings: DuelSettings,
    private val configuration: DuelConfigurationGui
): CasualContainerGui(ContainerType.Generic9x6, player, true) {
    init {
        this.setParent(this.configuration)

        this.setTitle(Component {
            empty() + spaced(-8.0F) + CasualComponents.Gui.DUEL_KITS.copy().white()
        })

        val kitSlots = KIT_SLOTS.iterator()
        for (option in this.settings.displayableKit.options) {
            if (!kitSlots.hasNext()) {
                break
            }
            this.setSlot(kitSlots.next(), this.settings.getKitDisplayStack(option.id)) {
                this.settings.kit = option.id
            }
        }

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.openParentOrClose()
        }
    }

    override fun click(slot: Int, action: SlotClickAction) {
        super.click(slot, action)

        if (slot in KIT_SLOTS) {
            this.openParentOrClose()
        }
    }

    companion object {
        val KIT_SLOTS = (38..42) + (47..51)
    }
}