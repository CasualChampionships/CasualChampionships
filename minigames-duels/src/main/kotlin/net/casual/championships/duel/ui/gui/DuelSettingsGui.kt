package net.casual.championships.duel.ui.gui

import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.minigame.utils.addSettingDisplay
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

        val settings = listOf(
            this.settings.displayableTeams,
            this.settings.displayableGlowing,
            this.settings.displayableNaturalRegen,
            this.settings.displayablePlayerDropHeads,
            this.settings.displayableHealth,
            this.settings.displayableArena,
            this.settings.displayableArenaSize
        )

        for ((i, setting) in settings.withIndex()) {
            val slot = 46 + i
            this.addSettingDisplay(slot, setting)
        }

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.openParentOrClose()
        }
    }
}