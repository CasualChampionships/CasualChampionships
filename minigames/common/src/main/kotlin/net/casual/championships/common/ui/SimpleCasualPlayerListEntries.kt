package net.casual.championships.common.ui

import net.casual.arcade.gui.tab.PlayerListEntries
import net.casual.arcade.gui.tab.VanillaPlayerListEntries
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils.italicise
import net.casual.arcade.utils.ComponentUtils.mini
import net.minecraft.network.chat.Component

class SimpleCasualPlayerListEntries(
    val minigame: Minigame<*>
): VanillaPlayerListEntries(
    { minigame.players.all },
    Comparator.comparing(minigame.players::isPlaying).thenComparing(DEFAULT_ORDER)
) {
    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val entry = super.getEntryAt(index)
        val display = Component.empty().append(entry.display).mini()
        if (this.minigame.players.isSpectating(this.getPlayerAt(index))) {
            display.italicise()
        }
        return entry.copy(display = display)
    }
}