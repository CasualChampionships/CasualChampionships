package net.casual.championships.common.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.common.CommonMod
import net.casual.championships.common.util.CommonItems
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
        val MODELLER = ResourcePackItemModeller(MinesweeperItem(), CommonMod.CUSTOM_MODEL_PACK.getCreator())
        val UNKNOWN by MODELLER.model(CommonMod.id("minesweeper/unknown"))
        val ONE by MODELLER.model(CommonMod.id("minesweeper/1"))
        val TWO by MODELLER.model(CommonMod.id("minesweeper/2"))
        val THREE by MODELLER.model(CommonMod.id("minesweeper/3"))
        val FOUR by MODELLER.model(CommonMod.id("minesweeper/4"))
        val FIVE by MODELLER.model(CommonMod.id("minesweeper/5"))
        val SIX by MODELLER.model(CommonMod.id("minesweeper/6"))
        val SEVEN by MODELLER.model(CommonMod.id("minesweeper/7"))
        val EIGHT by MODELLER.model(CommonMod.id("minesweeper/8"))
        val MINE by MODELLER.model(CommonMod.id("minesweeper/mine"))
        val EMPTY by MODELLER.model(CommonMod.id("minesweeper/empty"))
        val FLAG by MODELLER.model(CommonMod.id("minesweeper/flag"))
        val FLAG_COUNTER by MODELLER.model(CommonMod.id("minesweeper/flag_counter"))
    }
}