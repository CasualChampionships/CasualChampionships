package net.casual.championships.common.util

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.utils.ResourcePackUtils.addMissingItemModels
import net.casual.championships.common.CommonMod
import net.casual.championships.common.items.*
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object CommonItems {
    val CUSTOM_MODEL_PACK = NamedResourcePackCreator.named("common_models") {
        addMissingItemModels(CommonMod.MOD_ID)
    }

    val GOLDEN_HEAD = register("golden_head", GoldenHeadItem())
    val PLAYER_HEAD = register("player_head", PlayerHeadItem())

    val MENU = register("menu", MenuItem.MODELLER.item())
    val BORDER = register("border", BorderItem.MODELLER.item())

    val MINESWEEPER = register("minesweeper", MinesweeperItem.MODELLER.item())

    val MYSTERIOUS_PEARL = register("mysterious_pearl", MysteriousPearlItem())

    fun noop() {

    }

    private fun <T: Item> register(key: String, item: T): T {
        Items.registerItem(CommonMod.id(key), item)
        return item
    }
}