package net.casual.championships.items

import net.casual.championships.CasualMod
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.util.*
import kotlin.collections.ArrayList

object CasualItems {
    private val ALL = ArrayList<Item>()

    val FLYING_MACHINE = register("flying_machine", FlyingMachineItem.MODELLER.item())

    fun noop() {

    }

    fun all(): Collection<Item> {
        return Collections.unmodifiableCollection(ALL)
    }

    private fun <T: Item> register(key: String, item: T): T {
        Items.registerItem(CasualMod.id(key), item)
        ALL.add(item)
        return item
    }
}