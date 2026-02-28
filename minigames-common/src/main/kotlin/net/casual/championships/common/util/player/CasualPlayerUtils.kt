package net.casual.championships.common.util.player

import net.casual.championships.common.util.casual
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

private val HEALTH_BOOST = casual("health_boost")

fun ServerPlayer.boostHealth(multiply: Double) {
    val instance = this.attributes.getInstance(Attributes.MAX_HEALTH)
    if (instance != null) {
        instance.removeModifier(HEALTH_BOOST)
        instance.addPermanentModifier(AttributeModifier(
            HEALTH_BOOST,
            multiply,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ))
    }
}

fun ServerPlayer.unboostHealth() {
    this.attributes.getInstance(Attributes.MAX_HEALTH)?.removeModifier(HEALTH_BOOST)
}