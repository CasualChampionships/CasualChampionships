package net.casual.championships.common.ui.game

import com.mojang.authlib.GameProfile
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.utils.component.white
import net.casual.arcade.utils.component.yellow
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.screen.setSlot
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.ui.CasualSimpleGui
import net.casual.championships.common.util.CasualComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

class PlayerSelectorGui(
    player: ServerPlayer,
    profiles: List<GameProfile>
): CasualSimpleGui(MenuType.GENERIC_9x6, player, true) {
    init {
        this.title = Component.empty()
            .append(SpacingFontResources.spaced(-8))
            .append(CasualComponents.Gui.TEAM_PLAYER_SELECTOR.copy().white())

        for ((i, profile) in profiles.take(5).withIndex()) {
            val head = ItemUtils.createPlayerHead(profile, CasualItems.FORWARD_FACING_PLAYER_HEAD)
            val name = Component.literal(profile.name).yellow().withMiniFont()
            this.setSlot(47 + i, head.named(name)) { ->
                val selected = this.player.levelServer.playerList.getPlayer(profile.id)
                if (selected != null) {
                    this.player.teleportTo(selected.locationWithLevel)
                }
            }
        }

        this.setSlot(58, CasualGuiItems.RED_BACK.hideTooltip()) { ->
            this.openParentOrClose()
        }
    }
}

