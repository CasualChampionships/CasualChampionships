package net.casual.items

import net.casual.util.CasualUtils.id
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object CasualItems {
    val GOLDEN_HEAD = register("golden_head", GoldenHeadItem())
    val PLAYER_HEAD = register("player_head", PlayerHeadItem())

    val MINESWEEPER = register("minesweeper", MinesweeperItem.MODELLER.item())
    val LIGHTNING_STAFF = register("lightning_staff", LightningStaffItem.MODELLER.item())

    fun noop() {

    }

    private fun <T: Item> register(key: String, item: T): T {
        Items.registerItem(id(key), item)
        return item
    }
}