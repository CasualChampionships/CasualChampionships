package net.casual.championships.uhc.item

import net.casual.championships.uhc.UHCMod.id
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.component.UseCooldown
import java.util.*

object UHCItems {
    val FLOWER_POWER = register("flower_power") { properties ->
        FlowerPowerItem(
            properties.component(DataComponents.MAX_DAMAGE, 3)
                .component(DataComponents.DAMAGE, 0)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.USE_COOLDOWN, UseCooldown(15.0F, Optional.of(id("flower_power"))))
                .component(DataComponents.ITEM_MODEL, id("flower_power"))
        )
    }

    fun noop() {

    }

    @Suppress("SameParameterValue")
    private fun register(path: String, provider: (Properties) -> Item): Item {
        val key = ResourceKey.create(Registries.ITEM, id(path))
        val properties = Properties().setId(key)
        return Registry.register(BuiltInRegistries.ITEM, key, provider.invoke(properties))
    }
}