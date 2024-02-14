package net.casual.championships.minigame.core

import net.casual.arcade.gui.screen.SelectionScreenStyle
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.settings.display.DisplayableGameSettingBuilder
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils
import net.casual.championships.items.MenuItem
import net.casual.championships.util.CasualScreenUtils
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ItemStack

open class CasualSettings(minigame: Minigame<*>): MinigameSettings(minigame) {
    override fun menu(parent: MenuProvider?): MenuProvider {
        return ScreenUtils.createSettingsMenu(
            this,
            CasualScreenUtils.named("Settings".literal()),
            parent,
            SelectionScreenStyle.centered(7, 3),
            { CasualScreenUtils.named(it.display.hoverName) }
        )
    }

    override fun defaultOptionsFor(
        builder: DisplayableGameSettingBuilder<Boolean>,
        enabled: ItemStack,
        disabled: ItemStack
    ) {
        super.defaultOptionsFor(builder, MenuItem.TICK.named("Enabled"), MenuItem.CROSS.named("Disabled"))
    }
}