package net.casualuhc.uhcmod.screen

import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.casualuhc.uhcmod.items.MinesweeperItem
import net.casualuhc.uhcmod.items.UHCItems
import net.casualuhc.uhcmod.util.HeadUtils
import net.casualuhc.uhcmod.util.ItemModelUtils.addUHCModel
import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ItemsScreen(inventory: Inventory, syncId: Int, private val page: Int): CustomScreen(inventory, syncId, 6) {
    init {
        this.modifyInventory {
            var slot = 9
            it.setItem(slot++, UHCItems.PLAYER_HEAD.defaultInstance)
            it.setItem(slot++, HeadUtils.createGoldenHead())
            it.setItem(slot++, UHCItems.LIGHTNING_STAFF.defaultInstance)
            for (state in MinesweeperItem.STATES.getStates()) {
                it.setItem(slot++, MinesweeperItem.STATES.createStack(state) { s, d -> s.addUHCModel(d) })
            }

            for (i in 1..7) {
                it.setItem(i, Items.GRAY_STAINED_GLASS.defaultInstance.setHoverName(Component.empty()))
            }

            it.setItem(0, Items.RED_STAINED_GLASS.literalNamed("Previous"))
            it.setItem(8, Items.GREEN_STAINED_GLASS.literalNamed("Next"))
        }
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        if (slotId == 0 && this.page > 0) {
            //player.openMenu(createScreenFactory(this.page - 1))
            return
        }
        if (slotId == 8) {
            //player.openMenu(createScreenFactory(this.page + 1))
            return
        }
        if (slotId < 9 || slotId > 53) {
            return
        }
        val clickedStack: ItemStack = this.slots[slotId].item

        if (clickedStack.isEmpty) {
            return
        }

        val size = if (clickType == ClickType.CLONE) clickedStack.maxStackSize else 1
        player.containerMenu.carried = clickedStack.copyWithCount(size)
    }

    companion object {
        fun createScreenFactory(page: Int): SimpleMenuProvider? {
            return if (page < 0) null else SimpleMenuProvider(
                { syncId, inv, _ -> ItemsScreen(inv, syncId, page) },
                Component.literal("UHC Items Screen")
            )
        }
    }
}
