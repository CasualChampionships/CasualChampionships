package net.casualuhc.uhcmod.screen

import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.casualuhc.uhcmod.settings.GameSettings
import net.minecraft.network.chat.Component
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class RuleScreen(inventory: Inventory, syncId: Int, private val page: Int): CustomScreen(inventory, syncId, 6) {
    init {
        this.modifyInventory {
            val gameSettings = GameSettings.RULES.values.stream().skip(5L * page).toList()
            for (i in 1..5) {
                if (gameSettings.size <= i - 1) {
                    break
                }
                var startSlot = i * 9
                val gameSetting = gameSettings[i - 1]
                it.setItem(startSlot, gameSetting.display)

                startSlot += 2
                val itemStacks = gameSetting.options.keys.stream().limit(7).toList()
                for (j in 0 until itemStacks.size) {
                    it.setItem(startSlot + j, itemStacks[j])
                }
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
            player.openMenu(createScreenFactory(this.page - 1))
            return
        }
        if (slotId == 8) {
            player.openMenu(createScreenFactory(this.page + 1))
            return
        }
        if (slotId < 9 || slotId > 53 || slotId % 9 == 0) {
            return
        }
        val row = Math.floorDiv(slotId, 9)
        val ruleStack: ItemStack = this.slots[row * 9].item
        val clickedStack: ItemStack = this.slots[slotId].item

        if (clickedStack.isEmpty) {
            return
        }

        val setting = GameSettings.RULES[ruleStack]
        if (setting != null) {
            setting.setValueFromOption(clickedStack)
            val message = Component.literal("${setting.name} has been set to ").append(clickedStack.displayName)
            player.sendSystemMessage(message)
        }
    }

    companion object {
        fun createScreenFactory(page: Int): SimpleMenuProvider? {
            return if (page < 0) null else SimpleMenuProvider(
                { syncId, inv, _ -> RuleScreen(inv, syncId, page) },
                Component.literal("Rule Config Screen")
            )
        }
    }
}
