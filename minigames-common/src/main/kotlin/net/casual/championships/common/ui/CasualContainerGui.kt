package net.casual.championships.common.ui

import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.utils.player.sendSound
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents

open class CasualContainerGui(
    type: ContainerType,
    player: ServerPlayer,
    overrideInventory: Boolean
): ContainerGui(player, type, overrideInventory) {
    override fun click(slot: Int, action: SlotClickAction) {
        if (this.handlers.getOrNull(slot) != null) {
            this.player.sendSound(SoundEvents.UI_BUTTON_CLICK)
        }
        super.click(slot, action)
    }
}