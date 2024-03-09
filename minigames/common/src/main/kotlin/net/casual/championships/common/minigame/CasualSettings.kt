package net.casual.championships.common.minigame

import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.settings.display.DisplayableGameSetting
import net.casual.arcade.settings.display.DisplayableGameSettingBuilder
import net.casual.arcade.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.named
import net.casual.championships.common.item.MenuItem
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonComponents.DISABLED_MESSAGE
import net.casual.championships.common.util.CommonComponents.ENABLED_MESSAGE
import net.casual.championships.common.util.CommonScreens
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

open class CasualSettings(
    minigame: Minigame<*>,
    defaults: DisplayableSettingsDefaults = CasualDefaults("Casual Minigame Settings".literal())
): MinigameSettings(minigame, defaults) {
    class CasualDefaults(title: Component): DisplayableSettingsDefaults() {
        override val components = CommonScreens.named(title)

        override fun createComponents(setting: DisplayableGameSetting<*>): SelectionScreenComponents {
            return CommonScreens.named(setting.display.hoverName)
        }

        override fun options(builder: DisplayableGameSettingBuilder<Boolean>, enabled: ItemStack, disabled: ItemStack) {
            super.options(builder, MenuItem.TICK.named(ENABLED_MESSAGE), MenuItem.CROSS.named(DISABLED_MESSAGE))
        }
    }
}