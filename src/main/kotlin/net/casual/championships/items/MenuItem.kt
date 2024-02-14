package net.casual.championships.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.util.CasualUtils
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
        val MODELLER = ResourcePackItemModeller(MenuItem(), CasualResourcePack.pack)
        val TICK by MODELLER.model(CasualUtils.id("gui/green_tick"))
        val CROSS by MODELLER.model(CasualUtils.id("gui/red_cross"))
        val NEXT by MODELLER.model(CasualUtils.id("gui/green_right_arrow"))
        val PREVIOUS by MODELLER.model(CasualUtils.id("gui/red_left_arrow"))
        val LONG_NEXT by MODELLER.model(CasualUtils.id("gui/green_long_right_arrow"))
        val LONG_PREVIOUS by MODELLER.model(CasualUtils.id("gui/red_long_left_arrow"))
        val UNAVAILABLE_NEXT by MODELLER.model(CasualUtils.id("gui/greyed_green_right_arrow"))
        val UNAVAILABLE_PREVIOUS by MODELLER.model(CasualUtils.id("gui/greyed_red_left_arrow"))
        val UNAVAILABLE_LONG_NEXT by MODELLER.model(CasualUtils.id("gui/greyed_green_long_right_arrow"))
        val UNAVAILABLE_LONG_PREVIOUS by MODELLER.model(CasualUtils.id("gui/greyed_red_long_left_arrow"))
    }
}