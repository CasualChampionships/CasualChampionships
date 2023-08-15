package net.casualuhc.uhc.items

import eu.pb4.polymer.core.api.item.PolymerItem
import net.casualuhc.arcade.items.ModelledItemStates
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.uhc.resources.UHCResourcePack
import net.casualuhc.uhc.util.ItemModelUtils.create
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.Level

class LightningStaffItem private constructor(): AxeItem(Tiers.DIAMOND, 4.0f, -3.0f, Properties()), PolymerItem {
    override fun getPolymerItem(itemStack: ItemStack, player: ServerPlayer?): Item {
        return Items.DIAMOND_AXE
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (level !is ServerLevel) {
            return super.use(level, player, usedHand)
        }
        val lightning = EntityType.LIGHTNING_BOLT.create(level) ?: return super.use(level, player, usedHand)
        lightning.setVisualOnly(true)

        val result = player.pick(50.0, 0F, true)
        lightning.moveTo(result.location)
        level.addFreshEntity(lightning)

        player.swing(usedHand, true)

        val stack = player.getItemInHand(usedHand)
        if (!player.isCreative) {
            player.cooldowns.addCooldown(this, MinecraftTimeUnit.Seconds.toTicks(20))
            stack.hurtAndBreak(100, player) { it.broadcastBreakEvent(usedHand) }
        }

        return InteractionResultHolder.pass(stack)
    }

    override fun getPolymerCustomModelData(itemStack: ItemStack, player: ServerPlayer?): Int {
        return STATES.getModel(DEFAULT).value()
    }

    companion object {
        val STATES = ModelledItemStates(LightningStaffItem(), UHCResourcePack.pack)
        val DEFAULT = STATES.create("weapons/lightning_staff")
    }
}