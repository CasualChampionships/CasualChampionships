package net.casualuhc.uhcmod.screen

import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.function.Consumer

abstract class CustomScreen(
    playerInventory: Inventory,
    syncId: Int,
    rows: Int
): AbstractContainerMenu(rowsToType(rows), syncId) {
    private val playerInventory: Inventory
    private val inventory: Container

    init {
        this.playerInventory = playerInventory
        this.inventory = SimpleContainer(9 * rows)
        val i = (rows - 4) * 18
        for (j in 0 until rows) {
            for (k in 0..8) {
                this.addSlot(Slot(this.inventory, k + j * 9, 8 + k * 18, 18 + j * 18))
            }
        }
        for (j in 0..2) {
            for (k in 0..8) {
                this.addSlot(Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i))
            }
        }
        for (j in 0..8) {
            this.addSlot(Slot(playerInventory, j, 8 + j * 18, 161 + i))
        }
    }

    constructor(player: Player, syncId: Int, rows: Int): this(Inventory(player), syncId, rows)

    fun modifyInventory(inventoryModifier: Consumer<Container>) {
        inventoryModifier.accept(this.inventory)
    }

    fun modifyPlayerInventory(inventoryModifier: Consumer<Inventory>) {
        inventoryModifier.accept(this.playerInventory)
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        super.clicked(slotId, button, clickType, player)
    }

    override fun quickMoveStack(player: Player, slot: Int): ItemStack {
        throw UnsupportedOperationException("Cannot transfer slots from within a CustomScreen")
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun removed(player: Player) {
        super.removed(player)
        player.containerMenu.sendAllDataToRemote()
        // Scheduler.schedule(0, player.playerScreenHandler::syncState);
    }

    companion object {
        private fun rowsToType(rows: Int): MenuType<*> {
            return when (rows) {
                1 -> MenuType.GENERIC_9x1
                2 -> MenuType.GENERIC_9x2
                3 -> MenuType.GENERIC_9x3
                4 -> MenuType.GENERIC_9x4
                5 -> MenuType.GENERIC_9x5
                6 -> MenuType.GENERIC_9x6
                else -> throw IllegalStateException("Invalid number of rows: $rows")
            }
        }
    }
}
