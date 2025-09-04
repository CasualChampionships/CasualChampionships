package net.casual.championships.minigame.lobby

import eu.pb4.sgui.api.gui.HotbarGui
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.screen.setSlot
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class CasualLobbyParkourHotbarGui(
    player: ServerPlayer,
    private val exit: Location
): HotbarGui(player) {
    override fun onOpen() {
        val barrier = ItemStack(Items.BARRIER).hideTooltip()
            .named(Component.translatable("lobby.parkour.exit").bold().red())
        this.setSlot(8, barrier) { _ ->
            this.player.teleportTo(this.exit)
        }
    }
}