package net.casual.championships.common.ui

import eu.pb4.sgui.api.ClickType
import net.casual.arcade.guis.sgui.PlayerInventoryViewGui
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.player.hasPermission
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.util.CasualComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.inventory.ContainerInput

class CasualPlayerInventoryViewGui(
    observee: ServerPlayer,
    observer: ServerPlayer
): PlayerInventoryViewGui(observee, observer) {
    override fun loadBackground() {
        val name = this.observee.displayName
        this.title = CasualComponents.Gui.createDoubleChestGui(name)

        val head = ItemUtils.createPlayerHead(this.observee, CasualItems.FORWARD_FACING_PLAYER_HEAD).named(name)
        this.setSlot(3, head)
    }

    override fun onAnyClick(index: Int, type: ClickType, action: ContainerInput): Boolean {
        if (!this.player.hasPermission(PermissionLevel.GAMEMASTERS)) {
            return false
        }
        return super.onAnyClick(index, type, action)
    }
}