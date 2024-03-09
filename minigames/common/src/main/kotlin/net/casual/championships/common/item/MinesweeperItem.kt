package net.casual.championships.common.item

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.common.CasualCommonMod
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
        val MODELLER = ResourcePackItemModeller(MinesweeperItem(), CasualCommonItems.CUSTOM_MODEL_PACK.getCreator())
        val UNKNOWN by MODELLER.model(CasualCommonMod.id("minesweeper/unknown"))
        val ONE by MODELLER.model(CasualCommonMod.id("minesweeper/1"))
        val TWO by MODELLER.model(CasualCommonMod.id("minesweeper/2"))
        val THREE by MODELLER.model(CasualCommonMod.id("minesweeper/3"))
        val FOUR by MODELLER.model(CasualCommonMod.id("minesweeper/4"))
        val FIVE by MODELLER.model(CasualCommonMod.id("minesweeper/5"))
        val SIX by MODELLER.model(CasualCommonMod.id("minesweeper/6"))
        val SEVEN by MODELLER.model(CasualCommonMod.id("minesweeper/7"))
        val EIGHT by MODELLER.model(CasualCommonMod.id("minesweeper/8"))
        val MINE by MODELLER.model(CasualCommonMod.id("minesweeper/mine"))
        val EMPTY by MODELLER.model(CasualCommonMod.id("minesweeper/empty"))
    }
}