package net.casual.championships.minigame.core

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.settings.display.DisplayableGameSettingBuilder
import net.casual.arcade.utils.ItemUtils.named
import net.casual.championships.items.MenuItem
import net.minecraft.world.item.ItemStack

open class CasualSettings(minigame: Minigame<*>): MinigameSettings(minigame) {
    override fun defaultOptionsFor(
        builder: DisplayableGameSettingBuilder<Boolean>,
        enabled: ItemStack,
        disabled: ItemStack
    ) {
        super.defaultOptionsFor(builder, MenuItem.YES.named("Enabled"), MenuItem.NO.named("Disabled"))
    }
}