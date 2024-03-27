package net.casual.championships.common.minigame

import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.settings.display.MenuGameSetting
import net.casual.arcade.settings.display.MenuGameSettingBuilder
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.named
import net.casual.championships.common.items.MenuItem.Companion.CROSS
import net.casual.championships.common.items.MenuItem.Companion.GREY_CROSS
import net.casual.championships.common.items.MenuItem.Companion.GREY_TICK
import net.casual.championships.common.items.MenuItem.Companion.TICK
import net.casual.championships.common.util.CommonComponents.DISABLED_MESSAGE
import net.casual.championships.common.util.CommonComponents.DISABLE_MESSAGE
import net.casual.championships.common.util.CommonComponents.ENABLED_MESSAGE
import net.casual.championships.common.util.CommonComponents.ENABLE_MESSAGE
import net.casual.championships.common.util.CommonScreens
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

open class CasualSettings(
    minigame: Minigame<*>,
    defaults: DisplayableSettingsDefaults = CasualDefaults("Casual Minigame Settings".literal())
): MinigameSettings(minigame, defaults) {
    class CasualDefaults(title: Component): DisplayableSettingsDefaults() {
        override val components = CommonScreens.named(title)

        override fun createComponents(setting: MenuGameSetting<*>): SelectionScreenComponents {
            return CommonScreens.named(setting.display.hoverName)
        }

        override fun options(builder: MenuGameSettingBuilder<Boolean>, enabled: ItemStack, disabled: ItemStack) {
            builder.option("enabled", TICK.named(ENABLED_MESSAGE), true) { setting, _, _ ->
                if (setting.get()) TICK.named(ENABLED_MESSAGE) else GREY_TICK.named(ENABLE_MESSAGE)
            }
            builder.option("disabled", CROSS.named(DISABLED_MESSAGE), false) { setting, _, _ ->
                if (setting.get()) GREY_CROSS.named(DISABLE_MESSAGE) else CROSS.named(DISABLED_MESSAGE)
            }
        }
    }
}