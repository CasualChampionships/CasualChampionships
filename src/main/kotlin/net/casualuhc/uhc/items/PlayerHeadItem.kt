package net.casualuhc.uhc.items

import eu.pb4.polymer.core.api.utils.PolymerUtils
import net.casualuhc.uhc.util.HeadUtils
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PlayerHeadItem

class PlayerHeadItem: HeadItem() {
    override fun getSkullOwner(stack: ItemStack): CompoundTag {
        val owner = stack.getTagElement(PlayerHeadItem.TAG_SKULL_OWNER)
        return owner ?: PolymerUtils.createSkullOwner(HeadUtils.STEVE)
    }

    override fun addEffects(player: ServerPlayer) {
        player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 60, 2))
        player.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, 15 * 20, 1))
        player.addEffect(MobEffectInstance(MobEffects.SATURATION, 5, 4))
    }
}