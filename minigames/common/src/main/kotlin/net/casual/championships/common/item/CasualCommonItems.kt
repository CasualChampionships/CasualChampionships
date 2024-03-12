package net.casual.championships.common.item

import net.casual.arcade.resources.NamedResourcePackCreator
import net.casual.arcade.utils.ResourcePackUtils.addMissingItemModels
import net.casual.championships.common.CasualCommonMod
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object CasualCommonItems {
    val CUSTOM_MODEL_PACK = NamedResourcePackCreator.named("common_models") {
        addMissingItemModels(CasualCommonMod.MOD_ID)
    }

    val GOLDEN_HEAD = register("golden_head", GoldenHeadItem())
    val PLAYER_HEAD = register("player_head", PlayerHeadItem())

    val MENU = register("menu", MenuItem.MODELLER.item())

    val MINESWEEPER = register("minesweeper", MinesweeperItem.MODELLER.item())

    val MYSTERIOUS_PEARL = register("mysterious_pearl", MysteriousPearlItem())

    fun noop() {

    }

    private fun <T: Item> register(key: String, item: T): T {
        Items.registerItem(CasualCommonMod.id(key), item)
        return item
    }
}