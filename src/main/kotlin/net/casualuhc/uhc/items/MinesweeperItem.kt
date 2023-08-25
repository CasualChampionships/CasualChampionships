package net.casualuhc.uhc.items

import net.casualuhc.arcade.items.ArcadeModelledItem
import net.casualuhc.arcade.items.ModelledItemStates
import net.casualuhc.uhc.resources.UHCResourcePack
import net.casualuhc.uhc.util.ItemModelUtils.create
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MinesweeperItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getStates(): ModelledItemStates {
        return STATES
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