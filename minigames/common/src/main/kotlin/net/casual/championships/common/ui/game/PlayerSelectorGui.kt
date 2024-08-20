package net.casual.championships.common.ui.game

import com.mojang.authlib.GameProfile
import eu.pb4.sgui.api.gui.GuiInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.white
import net.casual.arcade.utils.ComponentUtils.yellow
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonItems
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

class PlayerSelectorGui(
    player: ServerPlayer,
    profiles: List<GameProfile>,
    val parent: GuiInterface? = null
): SimpleGui(MenuType.GENERIC_9x6, player, true) {
    init {
        this.title = Component.empty()
            .append(ComponentUtils.space(-8))
            .append(CommonComponents.Gui.PLAYER_SELECTOR.white())

        for ((i, profile) in profiles.take(5).withIndex()) {
            val head = ItemUtils.createPlayerHead(profile, CommonItems.FORWARD_FACING_PLAYER_HEAD)
            val name = Component.literal(profile.name).yellow().mini()
            head.set(DataComponents.CUSTOM_NAME, name)
            this.setSlot(47 + i, head) { _, _, _, _ ->
                val selected = this.player.server.playerList.getPlayer(profile.id)
                if (selected != null) {
                    this.player.teleportTo(selected.location)
                }
            }
        }

        this.setSlot(58, MenuItem.RED_BACK.hideTooltip()) { _, _, _, _ ->
            if (this.parent != null) this.parent.open() else this.close()
        }
    }
}

