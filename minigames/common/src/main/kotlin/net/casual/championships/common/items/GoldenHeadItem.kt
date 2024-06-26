package net.casual.championships.common.items

import eu.pb4.polymer.core.api.utils.PolymerUtils
import net.casual.championships.common.util.HeadUtils
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

class GoldenHeadItem: HeadItem() {
    override fun getResolvableProfile(stack: ItemStack): ResolvableProfile {
        return PolymerUtils.createProfileComponent(HeadUtils.GOLDEN, null)
    }

    override fun addEffects(player: ServerPlayer) {
        player.addEffect(MobEffectInstance(REGENERATION, 50, 3))
        player.addEffect(MobEffectInstance(MOVEMENT_SPEED, 20 * 20, 1))
        player.addEffect(MobEffectInstance(SATURATION, 5, 4))

        player.addEffect(MobEffectInstance(ABSORPTION, 120 * 20, 0))
        player.addEffect(MobEffectInstance(DAMAGE_RESISTANCE, 5 * 20, 0))
    }
}