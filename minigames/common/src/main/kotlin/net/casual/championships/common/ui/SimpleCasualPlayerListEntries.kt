package net.casual.championships.common.ui

import net.casual.arcade.gui.tab.PlayerListEntries
import net.casual.arcade.gui.tab.VanillaPlayerListEntries
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils.mini
import net.minecraft.network.chat.Component

class SimpleCasualPlayerListEntries(
    val minigame: Minigame<*>
): VanillaPlayerListEntries({ minigame.players.playing }) {
    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val entry = super.getEntryAt(index)
        return entry.copy(display = Component.empty().append(entry.display).mini())
    }
}