package net.casual.championships.common.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.casual.championships.common.util.CommonComponents
import net.minecraft.ChatFormatting
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks

abstract class HeadItem: BlockItem(Blocks.PLAYER_HEAD, Properties()), PolymerItem {
    abstract fun getSkullOwner(stack: ItemStack): Tag

    abstract fun addEffects(player: ServerPlayer)

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (player is ServerPlayer) {
            this.addEffects(player)
            player.swing(usedHand, true)
            player.cooldowns.addCooldown(this, 20)
            val stack = player.getItemInHand(usedHand)
            if (!player.abilities.instabuild) {
                stack.shrink(1)
            }
            return InteractionResultHolder.consume(stack)
        }
        return super.use(level, player, usedHand)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        if (player is ServerPlayer) {
            this.addEffects(player)
            player.swing(context.hand, true)
            player.cooldowns.addCooldown(this, 20)
            if (!player.abilities.instabuild) {
                context.itemInHand.shrink(1)
            }
            return InteractionResult.CONSUME
        }
        return super.useOn(context)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltipComponents: MutableList<Component>, isAdvanced: TooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced)
        tooltipComponents.add(CommonComponents.HEAD_TOOLTIP_MESSAGE.withStyle(ChatFormatting.GRAY))
    }

    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayer?): Item {
        return Items.PLAYER_HEAD
    }

    override fun getPolymerItemStack(itemStack: ItemStack, tooltipContext: TooltipFlag, player: ServerPlayer?): ItemStack {
        val out = super.getPolymerItemStack(itemStack, tooltipContext, player)
        out.orCreateTag.put("SkullOwner", this.getSkullOwner(itemStack))
        return out
    }
}

