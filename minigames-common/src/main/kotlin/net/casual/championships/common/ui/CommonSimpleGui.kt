package net.casual.championships.common.ui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElementInterface
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.visuals.screen.SimpleNestedGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.ClickType as ActionType

open class CommonSimpleGui(
    type: MenuType<*>,
    player: ServerPlayer,
    manipulatePlayerSlots: Boolean
): SimpleNestedGui(type, player, manipulatePlayerSlots) {
    override fun onClick(index: Int, type: ClickType, action: ActionType, element: GuiElementInterface?): Boolean {
        val callback = element?.guiCallback
        if (callback != null && callback != GuiElementInterface.EMPTY_CALLBACK) {
            this.player.sendSound(SoundEvents.UI_BUTTON_CLICK)
        }
        return super.onClick(index, type, action, element)
    }
}