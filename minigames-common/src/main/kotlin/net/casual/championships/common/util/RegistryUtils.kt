package net.casual.championships.common.util

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop
import net.casual.arcade.utils.toKey
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.Items

// TODO: Move into arcade?

class NamespacedRegistryRegister<T: Any>(
    private val registry: Registry<T>,
    private val namespace: (String) -> ResourceLocation
) {
    fun register(path: String, element: T) {
        Registry.register(this.registry, this.namespace.invoke(path), element)
    }

    operator fun invoke(path: String, element: T) {
        this.register(path, element)
    }
}

class NamespacedItemRegistryRegister(
    private val namespace: (String) -> ResourceLocation
) {
    fun register(path: String, provider: (Properties) -> Item): Item {
        val key = this.namespace.invoke(path).toKey(Registries.ITEM)
        return Items.registerItem(key, provider)
    }

    operator fun invoke(path: String, provider: (Properties) -> Item): Item {
        return this.register(path, provider)
    }
}