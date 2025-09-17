package net.casual.championships.common.items

import net.casual.arcade.items.ItemStackFactory
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.casual.championships.common.util.casual
import net.minecraft.world.item.ItemStack

object CasualGuiItems {
    private val display = ItemStackFactory.modeller(CasualItems.DUMMY)
    private val tinted = ItemStackFactory.modeller(CasualItems.TINTABLE_DUMMY)

    val TICK by display.modelled("gui/green_tick")
    val TICK_SELECTED by display.modelled("gui/green_tick_selected")
    val GREY_TICK by display.modelled("gui/greyed_green_tick")
    val CROSS by display.modelled("gui/red_cross")
    val CROSS_SELECTED by display.modelled("gui/red_cross_selected")
    val GREY_CROSS by display.modelled("gui/greyed_red_cross")
    val GREEN_RIGHT by display.modelled("gui/green_right_arrow")
    val GREEN_LEFT by display.modelled("gui/green_left_arrow")
    val RED_RIGHT by display.modelled("gui/red_right_arrow")
    val RED_LEFT by display.modelled("gui/red_left_arrow")
    val GREEN_LONG_RIGHT by display.modelled("gui/green_long_right_arrow")
    val GREEN_LONG_LEFT by display.modelled("gui/green_long_left_arrow")
    val RED_LONG_RIGHT by display.modelled("gui/red_long_right_arrow")
    val RED_LONG_LEFT by display.modelled("gui/red_long_left_arrow")
    val GREY_GREEN_RIGHT by display.modelled("gui/greyed_green_right_arrow")
    val GREY_GREEN_LEFT by display.modelled("gui/greyed_green_left_arrow")
    val GREY_RED_RIGHT by display.modelled("gui/greyed_red_right_arrow")
    val GREY_RED_LEFT by display.modelled("gui/greyed_red_left_arrow")
    val GREY_GREEN_LONG_RIGHT by display.modelled("gui/greyed_green_long_right_arrow")
    val GREY_GREEN_LONG_LEFT by display.modelled("gui/greyed_green_long_left_arrow")
    val GREY_RED_LONG_RIGHT by display.modelled("gui/greyed_red_long_right_arrow")
    val GREY_RED_LONG_LEFT by display.modelled("gui/greyed_red_long_left_arrow")

    val GREEN_DIAGONAL by display.modelled("gui/green_diagonal_arrow")

    val RED_BACK by display.modelled("gui/red_back_button")

    val GEAR by display.modelled("gui/gear")
    val ARENA by display.modelled("gui/arena")
    val NATURAL_REGEN by display.modelled("gui/natural_regen")
    val HEALTH_BOOST by display.modelled("gui/health_boost")
    val GLOWING by display.modelled("gui/glowing")
    val GAMEMODE_SWITCHER by display.modelled("gui/gamemode_switcher")

    val LARGE by display.modelled("gui/large_icon")
    val LARGE_SELECTED by display.modelled("gui/large_icon_selected")
    val MEDIUM by display.modelled("gui/medium_icon")
    val MEDIUM_SELECTED by display.modelled("gui/medium_icon_selected")
    val SMALL by display.modelled("gui/small_icon")
    val SMALL_SELECTED by display.modelled("gui/small_icon_selected")

    val ONE_TIMES by display.modelled("gui/one_times")
    val ONE_TIMES_SELECTED by display.modelled("gui/one_times_selected")
    val TWO_TIMES by display.modelled("gui/two_times")
    val TWO_TIMES_SELECTED by display.modelled("gui/two_times_selected")
    val THREE_TIMES by display.modelled("gui/three_times")
    val THREE_TIMES_SELECTED by display.modelled("gui/three_times_selected")

    val GREEN_HIGHLIGHT by display.modelled("gui/green_highlight")

    val BORDER_DISTANCE by display.modelled("border/border_distance")
    val BORDER_RADIUS by display.modelled("border/border_radius")

    val FLAG by tinted.modelled("gui/flag")

    fun all(): List<ItemStack> {
        return this.display.all().concat(this.tinted.all())
    }

    fun ItemStackFactory.Modeller.modelled(path: String): ItemStackFactory {
        return this.modelled(casual(path))
    }
}