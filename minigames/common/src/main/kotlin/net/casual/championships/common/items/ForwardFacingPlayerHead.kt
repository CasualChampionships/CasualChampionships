package net.casual.championships.common.items

import eu.pb4.polymer.core.api.item.PolymerItem
import net.casual.arcade.utils.ResourcePackUtils.registerNextModel
import net.casual.championships.common.CommonMod
import net.casual.championships.common.CommonMod.CUSTOM_MODEL_PACK
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag

class ForwardFacingPlayerHead: Item(Properties()), PolymerItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayer?): Item {
        return Items.PLAYER_HEAD
    }

    override fun getPolymerCustomModelData(itemStack: ItemStack, player: ServerPlayer?): Int {
        return MODEL_ID
    }

    override fun getPolymerItemStack(
        stack: ItemStack,
        flag: TooltipFlag,
        lookup: HolderLookup.Provider,
        player: ServerPlayer?
    ): ItemStack {
        val copy = super.getPolymerItemStack(stack, flag, lookup, player)
        copy.set(DataComponents.PROFILE, stack.get(DataComponents.PROFILE))
        return copy
    }

    companion object {
        private val MODEL_ID = CUSTOM_MODEL_PACK.getCreator().registerNextModel(
            Items.PLAYER_HEAD, CommonMod.id("gui/forward_facing_player_head")
        )
    }
}