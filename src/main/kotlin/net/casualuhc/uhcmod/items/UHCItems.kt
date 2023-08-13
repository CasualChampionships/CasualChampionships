package net.casualuhc.uhcmod.items

import net.casualuhc.uhcmod.util.ResourceUtils.id
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object UHCItems {
    val GOLDEN_HEAD = register("golden_head", GoldenHeadItem())
    val PLAYER_HEAD = register("player_head", PlayerHeadItem())

    val MINESWEEPER = register("minesweeper", MinesweeperItem.STATES.getServerItem())
    val LIGHTNING_STAFF = register("lightning_staff", LightningStaffItem.STATES.getServerItem())

    fun noop() {

    }

    private fun <T: Item> register(key: String, item: T): T {
        Items.registerItem(id(key), item)
        return item
    }
}