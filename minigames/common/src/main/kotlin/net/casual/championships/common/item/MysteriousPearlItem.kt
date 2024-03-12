package net.casual.championships.common.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.casual.arcade.utils.ResourcePackUtils.registerNextModel
import net.casual.championships.common.CasualCommonMod.id
import net.casual.championships.common.entity.MysteriousPearl
import net.casual.championships.common.item.CasualCommonItems.CUSTOM_MODEL_PACK
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items.POPPED_CHORUS_FRUIT
import net.minecraft.world.level.Level

class MysteriousPearlItem: Item(Properties()), PolymerItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return POPPED_CHORUS_FRUIT
    }

    override fun getPolymerCustomModelData(itemStack: ItemStack?, player: ServerPlayer?): Int {
        return MODEL_ID
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        val throwable = MysteriousPearl(level, player)
        throwable.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 1.5F, 1.0F)
        level.addFreshEntity(throwable)

        if (!player.abilities.instabuild) {
            stack.shrink(1)
        }

        return InteractionResultHolder.success(stack)
    }

    companion object {
        private val MODEL_ID = CUSTOM_MODEL_PACK.getCreator().registerNextModel(
            POPPED_CHORUS_FRUIT,
            id("test/mysterious_pearl"),
        )
    }
}