package net.casual.championships.missilewars.items

import net.casual.championships.missilewars.MissileWarsMod
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object MissileWarsItems {
    val FLYING_MACHINE = register("flying_machine", FlyingMachineItem.MODELLER.item())

    fun noop() {

    }

    private fun <T: Item> register(key: String, item: T): T {
        Items.registerItem(MissileWarsMod.id(key), item)
        return item
    }
}