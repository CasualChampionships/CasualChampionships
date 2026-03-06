package net.casual.championships.common.event

import net.casual.arcade.events.server.player.PlayerEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class PlayerLootVaultEvent(
    override val player: ServerPlayer,
    val item: ItemStack
): PlayerEvent