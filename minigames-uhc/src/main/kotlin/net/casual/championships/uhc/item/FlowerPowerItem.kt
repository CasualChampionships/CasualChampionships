package net.casual.championships.uhc.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.casual.arcade.utils.TimeUtils.Seconds
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class FlowerPowerItem(properties: Properties): Item(properties), PolymerItem {
    override fun getPolymerItem(stack: ItemStack, context: PacketContext): Item {
        return Items.POPPY
    }

    override fun use(level: Level, player: Player, interactionHand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(interactionHand)
        player.addEffect(MobEffectInstance(
            MobEffects.STRENGTH, 5.Seconds.ticks, 0
        ))
        stack.hurtAndBreak(1, player, interactionHand.asEquipmentSlot())
        return InteractionResult.SUCCESS_SERVER
    }
}