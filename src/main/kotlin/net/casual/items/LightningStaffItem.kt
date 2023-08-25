package net.casual.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ModelledItemStates
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.minigame.uhc.resources.UHCResourcePack
import net.casual.util.ItemModelUtils.create
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.level.Level

class LightningStaffItem private constructor(): AxeItem(Tiers.DIAMOND, 4.0f, -3.0f, Properties()), ArcadeModelledItem {
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

    override fun getStates(): ModelledItemStates {
        return STATES
    }

    companion object {
        val STATES = ModelledItemStates(LightningStaffItem(), UHCResourcePack.pack)
        val DEFAULT = STATES.create("weapons/lightning_staff")
    }
}