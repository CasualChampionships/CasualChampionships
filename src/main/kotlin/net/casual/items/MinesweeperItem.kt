package net.casual.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.resources.CasualResourcePack
import net.casual.util.CasualUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MinesweeperItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getModeller(): ItemModeller {
        return MODELLER
    }

    companion object {
        val MODELLER = ResourcePackItemModeller(MinesweeperItem(), CasualResourcePack.pack)
        val UNKNOWN by MODELLER.model(CasualUtils.id("minesweeper/unknown"))
        val ONE by MODELLER.model(CasualUtils.id("minesweeper/1"))
        val TWO by MODELLER.model(CasualUtils.id("minesweeper/2"))
        val THREE by MODELLER.model(CasualUtils.id("minesweeper/3"))
        val FOUR by MODELLER.model(CasualUtils.id("minesweeper/4"))
        val FIVE by MODELLER.model(CasualUtils.id("minesweeper/5"))
        val SIX by MODELLER.model(CasualUtils.id("minesweeper/6"))
        val SEVEN by MODELLER.model(CasualUtils.id("minesweeper/7"))
        val EIGHT by MODELLER.model(CasualUtils.id("minesweeper/8"))
        val MINE by MODELLER.model(CasualUtils.id("minesweeper/mine"))
        val EMPTY by MODELLER.model(CasualUtils.id("minesweeper/empty"))
    }
}