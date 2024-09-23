package net.casual.championships.common.util

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import net.casual.championships.common.CommonMod
import net.casual.championships.common.entities.MysteriousPearl
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object CommonEntities {
    val MYSTERIOUS_PEARL = register(
        "mysterious_pearl",
        EntityType.Builder.of(::MysteriousPearl, MobCategory.MISC).sized(0.25F, 0.25F)
    )

    fun noop() {

    }

    private fun <T: Entity> register(key: String, builder: EntityType.Builder<T>): EntityType<T> {
        val type = builder.build(key)
        Registry.register(BuiltInRegistries.ENTITY_TYPE, CommonMod.id(key), type)
        PolymerEntityUtils.registerType(type)
        return type
    }
}