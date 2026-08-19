package net.casual.championships.common.ui

import net.casual.arcade.guis.core.container.ContainerGuiClickBehavior
import net.casual.arcade.guis.presets.PlayerInventoryViewGui
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.player.hasPermission
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.util.CasualComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel

class CasualPlayerInventoryViewGui(
    observer: ServerPlayer,
    observee: ServerPlayer,
): PlayerInventoryViewGui(observer, observee) {
    override fun loadBackground() {
        val name = this.observee.displayName
        this.setTitle(CasualComponents.Gui.createDoubleChestGui(name))

        val head = ItemUtils.createPlayerHead(this.observee, CasualItems.FORWARD_FACING_PLAYER_HEAD).named(name)
        this.setSlot(3, head)
    }

    override fun resolveClickBehavior(slot: Int, action: SlotClickAction): ContainerGuiClickBehavior {
        if (!this.player.hasPermission(PermissionLevel.GAMEMASTERS)) {
            return ContainerGuiClickBehavior.None
        }
        return super.resolveClickBehavior(slot, action)
    }
}