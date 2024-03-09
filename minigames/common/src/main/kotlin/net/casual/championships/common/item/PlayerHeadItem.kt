package net.casual.championships.common.item

import eu.pb4.polymer.core.api.utils.PolymerUtils
import net.casual.arcade.utils.HeadTextures
import net.casual.arcade.utils.ItemUtils.isOf
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.PlayerHeadItem

class PlayerHeadItem: HeadItem() {
    override fun getSkullOwner(stack: ItemStack): Tag {
        val owner = stack.tag?.get(PlayerHeadItem.TAG_SKULL_OWNER)
        return owner ?: PolymerUtils.createSkullOwner(HeadTextures.STEVE)
    }

    override fun addEffects(player: ServerPlayer) {
        player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 60, 2))
        player.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SPEED, 15 * 20, 1))
        player.addEffect(MobEffectInstance(MobEffects.SATURATION, 5, 4))
    }

    override fun getName(stack: ItemStack): Component? {
        if (stack.isOf(CasualCommonItems.PLAYER_HEAD) && stack.hasTag()) {
            var string: String? = null
            val tag = stack.orCreateTag
            if (tag.contains("SkullOwner", Tag.TAG_STRING.toInt())) {
                string = tag.getString("SkullOwner")
            } else if (tag.contains("SkullOwner", Tag.TAG_COMPOUND.toInt())) {
                val owner = tag.getCompound("SkullOwner")
                if (owner.contains("Name", Tag.TAG_STRING.toInt())) {
                    string = owner.getString("Name")
                }
            }
            if (string != null) {
                return Component.translatable("${Items.PLAYER_HEAD.descriptionId}.named", string)
            }
        }
        return super.getName(stack)
    }
}