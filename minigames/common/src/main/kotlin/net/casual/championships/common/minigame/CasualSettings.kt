package net.casual.championships.common.minigame

import eu.pb4.sgui.api.gui.GuiInterface
import net.casual.arcade.gui.screen.SelectionGuiBuilder
import net.casual.arcade.gui.screen.SelectionGuiStyle
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.settings.display.MenuGameSetting
import net.casual.arcade.settings.display.MenuGameSettingBuilder
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils.addSettings
import net.casual.championships.common.items.MenuItem.Companion.CROSS
import net.casual.championships.common.items.MenuItem.Companion.GREY_CROSS
import net.casual.championships.common.items.MenuItem.Companion.GREY_TICK
import net.casual.championships.common.items.MenuItem.Companion.TICK
import net.casual.championships.common.util.CommonComponents.DISABLED
import net.casual.championships.common.util.CommonComponents.DISABLE
import net.casual.championships.common.util.CommonComponents.ENABLED
import net.casual.championships.common.util.CommonComponents.ENABLE
import net.casual.championships.common.util.CommonScreens
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.random.Random

open class CasualSettings(
    minigame: Minigame<*>,
    defaults: DisplayableSettingsDefaults = Defaults("Casual Minigame Settings".literal())
): MinigameSettings(minigame, defaults) {
    class Defaults(title: Component): DisplayableSettingsDefaults() {
        private val components = CommonScreens.named(title)

        override fun createSettingsGuiBuilder(player: ServerPlayer): SelectionGuiBuilder {
            return SelectionGuiBuilder(player, this.components)
                .style(SelectionGuiStyle.centered(5, 3))
        }

        override fun createOptionsGuiBuilder(parent: GuiInterface, setting: MenuGameSetting<*>): SelectionGuiBuilder {
            return SelectionGuiBuilder(parent, CommonScreens.named(setting.display.hoverName))
                .style(SelectionGuiStyle.centered(setting.optionCount))
        }

        override fun options(builder: MenuGameSettingBuilder<Boolean>, enabled: ItemStack, disabled: ItemStack) {
            builder.option("enabled", TICK.named(ENABLED), true) { setting, _, _ ->
                if (setting.get()) TICK.named(ENABLED) else GREY_TICK.named(ENABLE)
            }
            builder.option("disabled", CROSS.named(DISABLED), false) { setting, _, _ ->
                if (setting.get()) GREY_CROSS.named(DISABLE) else CROSS.named(DISABLED)
            }
        }
    }
}