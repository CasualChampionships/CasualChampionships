package net.casual.championships.missilewars.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.common.CommonMod
import net.casual.championships.common.util.CommonItems
import net.casual.championships.missilewars.MissileWarsMod
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class FlyingMachineItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getModeller(): ItemModeller {
        return MODELLER
    }

    companion object {
        val MODELLER = ResourcePackItemModeller(FlyingMachineItem(), CommonMod.CUSTOM_MODEL_PACK.getCreator())
        val BOUNCER by MODELLER.model(MissileWarsMod.id("missile_wars/bouncer"))
    }
}