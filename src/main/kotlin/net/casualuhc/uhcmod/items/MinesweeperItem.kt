package net.casualuhc.uhcmod.items

import eu.pb4.polymer.core.api.item.PolymerItem
import net.casualuhc.arcade.items.ModelledItemStates
import net.casualuhc.uhcmod.resources.UHCResourcePack
import net.casualuhc.uhcmod.util.ItemModelUtils.create
import net.casualuhc.uhcmod.util.ItemModelUtils.getUHCModel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MinesweeperItem private constructor(): Item(Properties()), PolymerItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getPolymerCustomModelData(itemStack: ItemStack, player: ServerPlayer?): Int {
        return itemStack.getUHCModel()
    }

    companion object {
        val STATES = ModelledItemStates(MinesweeperItem(), UHCResourcePack.pack)
        val UNKNOWN = STATES.create("minesweeper/unknown")
        val ONE = STATES.create("minesweeper/1")
        val TWO = STATES.create("minesweeper/2")
        val THREE = STATES.create("minesweeper/3")
        val FOUR = STATES.create("minesweeper/4")
        val FIVE = STATES.create("minesweeper/5")
        val SIX = STATES.create("minesweeper/6")
        val SEVEN = STATES.create("minesweeper/7")
        val EIGHT = STATES.create("minesweeper/8")
        val MINE = STATES.create("minesweeper/mine")
        val EMPTY = STATES.create("minesweeper/empty")
    }
}