package net.casual.championships.common.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.common.CommonMod
import net.casual.championships.common.CommonMod.id
import net.casual.championships.common.util.CommonItems
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MenuItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getModeller(): ItemModeller {
        return MODELLER
    }

    companion object {
        val MODELLER = ResourcePackItemModeller(MenuItem(), CommonMod.CUSTOM_MODEL_PACK.getCreator())
        val TICK by MODELLER.model(id("gui/green_tick"))
        val TICK_SELECTED by MODELLER.model(id("gui/green_tick_selected"))
        val GREY_TICK by MODELLER.model(id("gui/greyed_green_tick"))
        val CROSS by MODELLER.model(id("gui/red_cross"))
        val CROSS_SELECTED by MODELLER.model(id("gui/red_cross_selected"))
        val GREY_CROSS by MODELLER.model(id("gui/greyed_red_cross"))
        val GREEN_RIGHT by MODELLER.model(id("gui/green_right_arrow"))
        val GREEN_LEFT by MODELLER.model(id("gui/green_left_arrow"))
        val RED_RIGHT by MODELLER.model(id("gui/red_right_arrow"))
        val RED_LEFT by MODELLER.model(id("gui/red_left_arrow"))
        val GREEN_LONG_RIGHT by MODELLER.model(id("gui/green_long_right_arrow"))
        val GREEN_LONG_LEFT by MODELLER.model(id("gui/green_long_left_arrow"))
        val RED_LONG_RIGHT by MODELLER.model(id("gui/red_long_right_arrow"))
        val RED_LONG_LEFT by MODELLER.model(id("gui/red_long_left_arrow"))
        val GREY_GREEN_RIGHT by MODELLER.model(id("gui/greyed_green_right_arrow"))
        val GREY_GREEN_LEFT by MODELLER.model(id("gui/greyed_green_left_arrow"))
        val GREY_RED_RIGHT by MODELLER.model(id("gui/greyed_red_right_arrow"))
        val GREY_RED_LEFT by MODELLER.model(id("gui/greyed_red_left_arrow"))
        val GREY_GREEN_LONG_RIGHT by MODELLER.model(id("gui/greyed_green_long_right_arrow"))
        val GREY_GREEN_LONG_LEFT by MODELLER.model(id("gui/greyed_green_long_left_arrow"))
        val GREY_RED_LONG_RIGHT by MODELLER.model(id("gui/greyed_red_long_right_arrow"))
        val GREY_RED_LONG_LEFT by MODELLER.model(id("gui/greyed_red_long_left_arrow"))

        val GREEN_DIAGONAL by MODELLER.model(id("gui/green_diagonal_arrow"))

        val RED_BACK by MODELLER.model(id("gui/red_back_button"))

        val GEAR by MODELLER.model(id("gui/gear"))
        val ARENA by MODELLER.model(id("gui/arena"))

        val LARGE by MODELLER.model(id("gui/large_icon"))
        val LARGE_SELECTED by MODELLER.model(id("gui/large_icon_selected"))
        val MEDIUM by MODELLER.model(id("gui/medium_icon"))
        val MEDIUM_SELECTED by MODELLER.model(id("gui/medium_icon_selected"))
        val SMALL by MODELLER.model(id("gui/small_icon"))
        val SMALL_SELECTED by MODELLER.model(id("gui/small_icon_selected"))

        val ONE_TIMES by MODELLER.model(id("gui/one_times"))
        val ONE_TIMES_SELECTED by MODELLER.model(id("gui/one_times_selected"))
        val TWO_TIMES by MODELLER.model(id("gui/two_times"))
        val TWO_TIMES_SELECTED by MODELLER.model(id("gui/two_times_selected"))
        val THREE_TIMES by MODELLER.model(id("gui/three_times"))
        val THREE_TIMES_SELECTED by MODELLER.model(id("gui/three_times_selected"))

        val GREEN_HIGHLIGHT by MODELLER.model(id("gui/green_highlight"))
    }
}