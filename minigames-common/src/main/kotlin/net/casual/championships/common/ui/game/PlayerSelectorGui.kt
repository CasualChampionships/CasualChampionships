package net.casual.championships.common.ui.game

import com.mojang.authlib.GameProfile
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.pack.font.spacing.SpacingFontResources.spaced
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.utils.component.white
import net.casual.arcade.utils.component.yellow
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.locationWithLevel
import net.casual.arcade.utils.player.server
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.ui.CasualContainerGui
import net.casual.championships.common.util.CasualComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class PlayerSelectorGui(
    player: ServerPlayer,
    profiles: List<GameProfile>
): CasualContainerGui(ContainerType.Generic9x6, player, true) {
    init {
        this.setTitle(Component {
            empty() + spaced(-8.0F) + CasualComponents.Gui.TEAM_PLAYER_SELECTOR.copy().white()
        })

        for ((i, profile) in profiles.take(5).withIndex()) {
            val head = ItemUtils.createPlayerHead(profile, CasualItems.FORWARD_FACING_PLAYER_HEAD)
            val name = Component.literal(profile.name).yellow().withMiniFont()
            this.setSlot(47 + i, head.named(name)) {
                val selected = this.player.server.playerList.getPlayer(profile.id)
                if (selected != null) {
                    this.player.teleportTo(selected.locationWithLevel)
                }
            }
        }

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) {
            this.openParentOrClose()
        }
    }
}

