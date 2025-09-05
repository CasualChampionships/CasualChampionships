package net.casual.championships.common.ui.minesweeper

import net.casual.arcade.items.ItemStackFactory
import net.casual.championships.common.items.CasualGuiItems.modelled
import net.casual.championships.common.items.CasualItems
import net.minecraft.world.item.ItemStack

internal object MinesweeperItems {
    private val modeller = ItemStackFactory.modeller(CasualItems.DUMMY)
    val UNKNOWN by modeller.modelled("minesweeper/unknown")
    val ONE by modeller.modelled("minesweeper/1")
    val TWO by modeller.modelled("minesweeper/2")
    val THREE by modeller.modelled("minesweeper/3")
    val FOUR by modeller.modelled("minesweeper/4")
    val FIVE by modeller.modelled("minesweeper/5")
    val SIX by modeller.modelled("minesweeper/6")
    val SEVEN by modeller.modelled("minesweeper/7")
    val EIGHT by modeller.modelled("minesweeper/8")
    val MINE by modeller.modelled("minesweeper/mine")
    val FLAG by modeller.modelled("minesweeper/flag")
    val FLAG_COUNTER by modeller.modelled("minesweeper/flag_counter")

    private val numbers = arrayOf(ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT)

    fun of(tile: Int): ItemStack {
        return numbers.getOrNull(tile - 1)
            ?: throw IllegalArgumentException("Invalid tile")
    }
}