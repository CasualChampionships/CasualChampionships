package net.casual.championships.common.util

import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.utils.ItemUtils.named
import net.casual.championships.common.item.MenuItem
import net.casual.championships.common.util.CommonComponents.BACK_MESSAGE
import net.casual.championships.common.util.CommonComponents.EXIT_MESSAGE
import net.casual.championships.common.util.CommonComponents.NEXT_MESSAGE
import net.casual.championships.common.util.CommonComponents.PREVIOUS_MESSAGE
import net.minecraft.network.chat.Component

object CommonScreens {
    private val COMPONENTS = SelectionScreenComponents.Builder().apply {
        next(MenuItem.LONG_NEXT.named(NEXT_MESSAGE), MenuItem.UNAVAILABLE_LONG_NEXT.named(NEXT_MESSAGE))
        previous(MenuItem.LONG_PREVIOUS.named(PREVIOUS_MESSAGE), MenuItem.UNAVAILABLE_LONG_PREVIOUS.named(PREVIOUS_MESSAGE))
        back(MenuItem.CROSS.named(BACK_MESSAGE), MenuItem.CROSS.named(EXIT_MESSAGE))
    }.build()

    fun named(title: Component): SelectionScreenComponents {
        return SelectionScreenComponents.Builder(COMPONENTS).title(title).build()
    }
}