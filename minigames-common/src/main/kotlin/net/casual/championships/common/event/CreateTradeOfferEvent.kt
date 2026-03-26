package net.casual.championships.common.event

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext

data class CreateTradeOfferEvent(
    val context: LootContext,
    var offer: ItemStack
): LevelEvent {
    override val level: ServerLevel
        get() = this.context.level
}