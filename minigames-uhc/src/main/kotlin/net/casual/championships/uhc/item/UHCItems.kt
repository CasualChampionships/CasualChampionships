package net.casual.championships.uhc.item

import net.casual.arcade.utils.registries.NamespacedItemRegistryRegister
import net.casual.championships.common.util.casual
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.UseCooldown
import java.util.*

object UHCItems {
    private val register = NamespacedItemRegistryRegister(::casual)

    val FLOWER_POWER = register("flower_power") { properties ->
        FlowerPowerItem(
            properties.component(DataComponents.MAX_DAMAGE, 3)
                .component(DataComponents.DAMAGE, 0)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.USE_COOLDOWN, UseCooldown(15.0F, Optional.of(casual("flower_power"))))
                .component(DataComponents.ITEM_MODEL, casual("flower_power"))
        )
    }

    internal fun load() {

    }
}