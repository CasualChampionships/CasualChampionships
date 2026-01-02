package net.casual.championships.common.minigame

import eu.pb4.sgui.api.gui.GuiInterface
import net.casual.arcade.guis.sgui.SelectionGuiBuilder
import net.casual.arcade.guis.sgui.SelectionGuiComponents
import net.casual.arcade.guis.sgui.SelectionGuiStyle
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.minigame.settings.display.MenuGameSetting
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.named
import net.casual.championships.common.items.CasualGuiItems.CROSS
import net.casual.championships.common.items.CasualGuiItems.CROSS_SELECTED
import net.casual.championships.common.items.CasualGuiItems.GREEN_LONG_LEFT
import net.casual.championships.common.items.CasualGuiItems.GREEN_LONG_RIGHT
import net.casual.championships.common.items.CasualGuiItems.GREY_GREEN_LONG_LEFT
import net.casual.championships.common.items.CasualGuiItems.GREY_GREEN_LONG_RIGHT
import net.casual.championships.common.items.CasualGuiItems.TICK
import net.casual.championships.common.items.CasualGuiItems.TICK_SELECTED
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualComponents.BACK
import net.casual.championships.common.util.CasualComponents.DISABLE
import net.casual.championships.common.util.CasualComponents.DISABLED
import net.casual.championships.common.util.CasualComponents.ENABLE
import net.casual.championships.common.util.CasualComponents.ENABLED
import net.casual.championships.common.util.CasualComponents.EXIT
import net.casual.championships.common.util.CasualComponents.NEXT
import net.casual.championships.common.util.CasualComponents.PREVIOUS
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

open class CasualSettings(
    minigame: Minigame,
    defaults: DisplayableSettingsDefaults = Defaults(Component.literal("Casual Minigame Settings"))
): MinigameSettings(minigame, defaults) {
    open class Defaults(title: Component): DisplayableSettingsDefaults() {
        private val components = named(title)

        override fun createSettingsGuiBuilder(player: ServerPlayer): SelectionGuiBuilder {
            return SelectionGuiBuilder(player, this.components)
                .style(SelectionGuiStyle.centered(5, 3))
        }

        override fun createOptionsGuiBuilder(parent: GuiInterface, setting: MenuGameSetting<*>): SelectionGuiBuilder {
            return SelectionGuiBuilder(parent, named(setting.display.hoverName))
                .style(SelectionGuiStyle.centered(setting.optionCount))
        }

        override fun options(builder: MenuGameSettingBuilder<Boolean>, enabled: ItemStack, disabled: ItemStack) {
            builder.option("enabled", TICK_SELECTED.named(ENABLED.withMiniFont()), true) { setting, _, _ ->
                if (setting.get()) TICK_SELECTED.named(ENABLED.withMiniFont()) else TICK.named(ENABLE.withMiniFont())
            }
            builder.option("disabled", CROSS_SELECTED.named(DISABLED.withMiniFont()), false) { setting, _, _ ->
                if (setting.get()) CROSS.named(DISABLE.withMiniFont()) else CROSS_SELECTED.named(DISABLED.withMiniFont())
            }
        }
    }

    private companion object {
        private val COMPONENTS: SelectionGuiComponents = SelectionGuiComponents.Builder()
            .next(GREEN_LONG_RIGHT.named(NEXT), GREY_GREEN_LONG_RIGHT.named(NEXT))
            .previous(GREEN_LONG_LEFT.named(PREVIOUS), GREY_GREEN_LONG_LEFT.named(PREVIOUS))
            .back(CROSS.named(BACK), CROSS.named(EXIT))

        fun named(title: Component): SelectionGuiComponents {
            return SelectionGuiComponents.Builder(COMPONENTS).title(CasualComponents.Gui.createDoubleChestGui(title))
        }
    }
}