package net.casual.championships.common.ui

import net.casual.arcade.font.heads.PlayerHeadComponents
import net.casual.arcade.font.padding.PaddingNoSplitFont
import net.casual.arcade.font.padding.PaddingSplitFont
import net.casual.arcade.gui.tab.PlayerListEntries
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.withFont
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class CasualPlayerListEntries(
    private val minigame: Minigame<*>
): PlayerListEntries {
    private var entries: List<ServerPlayer> = listOf()

    override val size: Int = 80

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        if (index !in this.entries.indices) {
            return PlayerListEntries.Entry.HIDDEN
        }
        val player = this.entries[index]
        return PlayerListEntries.Entry(
            Component.empty()
                .append(PaddingNoSplitFont.padding(-8))
                .append(PlayerHeadComponents.getHeadOrDefault(player))
                .append(ComponentUtils.space(2))
                .append(Component.literal(player.scoreboardName).mini()),
            PlayerListEntries.Texture.HIDDEN,
            -1
        )
    }

    override fun updateEntries() {
        this.entries = this.minigame.getAllPlayers()
    }
}