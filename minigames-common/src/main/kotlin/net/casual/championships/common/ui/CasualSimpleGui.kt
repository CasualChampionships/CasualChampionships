package net.casual.championships.common.ui

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import net.casual.arcade.guis.sgui.SimpleNestedGui
import net.casual.arcade.utils.player.sendSound
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.inventory.MenuType

open class CasualSimpleGui(
    type: MenuType<*>,
    player: ServerPlayer,
    manipulatePlayerSlots: Boolean
): SimpleNestedGui(type, player, manipulatePlayerSlots) {
    override fun onClick(index: Int, type: ClickType, action: ContainerInput, element: GuiElement?): Boolean {
        val callback = element?.guiCallback
        if (callback != null && callback != GuiElement.EMPTY_CALLBACK) {
            this.player.sendSound(SoundEvents.UI_BUTTON_CLICK)
        }
        return super.onClick(index, type, action, element)
    }
}