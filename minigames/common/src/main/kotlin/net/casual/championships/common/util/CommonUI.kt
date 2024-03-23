package net.casual.championships.common.util

import net.casual.arcade.chat.ChatFormatter
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.predicate.EntityObserverPredicate
import net.casual.arcade.gui.predicate.PlayerObserverPredicate
import net.casual.arcade.gui.predicate.PlayerObserverPredicate.Companion.toPlayer
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.sidebar.SidebarSupplier
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.gui.tab.ArcadePlayerListDisplay
import net.casual.arcade.gui.tab.MinigamePlayerListEntries
import net.casual.arcade.gui.tab.VanillaPlayerListEntries
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.TickUtils
import net.casual.championships.common.ui.BorderDistanceRow
import net.casual.championships.common.ui.CasualPlayerListEntries
import net.casual.championships.common.ui.TeammateRow
import net.casual.championships.common.util.CommonComponents.Bitmap.CASUAL
import net.casual.championships.common.util.CommonComponents.Bitmap.CHAMPIONSHIPS
import net.casual.championships.common.util.CommonComponents.Bitmap.KIWITECH
import net.casual.championships.common.util.CommonComponents.Bitmap.SERVER_HOSTED_BY
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object CommonUI {
    val INFO_ANNOUNCEMENT = ChatFormatter.createAnnouncement(Component.literal("[Info]").gold().bold())

    fun createPlayingNameTag(
        predicate: PlayerObserverPredicate = EntityObserverPredicate.visibleObservee().toPlayer()
    ): ArcadeNameTag {
        return ArcadeNameTag({ it.displayName!! }, predicate)
    }

    fun createPlayingHealthTag(
        predicate: PlayerObserverPredicate = CommonPredicates.VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES
    ): ArcadeNameTag {
        return ArcadeNameTag(
            { String.format("%.1f ", it.health / 2).literal().append(CommonComponents.Bitmap.HARDCORE_HEART) },
            predicate
        )
    }

    fun addTeammates(sidebar: ArcadeSidebar, size: Int): ArcadeSidebar {
        val buffer = ComponentUtils.space()
        val teammates = Component.empty().append(buffer).append(CommonComponents.TEAMMATES_MESSAGE.mini())
        sidebar.addRow(SidebarSupplier.withNoScore(teammates))
        for (i in 0 until size) {
            sidebar.addRow(TeammateRow(i, buffer))
        }
        return sidebar
    }

    fun addBorderDistanceAndRadius(sidebar: ArcadeSidebar) {
        val buffer = ComponentUtils.space()
        sidebar.addRow(BorderDistanceRow(buffer))
        sidebar.addRow { player ->
            val display = Component.empty().append(buffer).append(CommonComponents.BORDER_RADIUS_MESSAGE.mini())
            val score = (player.level().worldBorder.size / 2.0).toInt().toString().literal().append(buffer)
            SidebarComponent.withCustomScore(display, score)
        }
    }

    fun createTabDisplay(minigame: Minigame<*>): ArcadePlayerListDisplay {
        val display = ArcadePlayerListDisplay(CasualPlayerListEntries(minigame))

        display.setDisplay(
            ComponentSupplier.of("\n".literal().append(CASUAL).append(" ").append(CHAMPIONSHIPS).append("\n"))
        ) { player ->
            val tps = TickUtils.calculateTPS()
            val formatting = if (tps >= 20) ChatFormatting.DARK_GREEN else if (tps > 15) ChatFormatting.YELLOW else if (tps > 10) ChatFormatting.RED else ChatFormatting.DARK_RED
            Component.literal("\n").apply {
                if (minigame.isAdmin(player)) {
                    append("TPS: ")
                    append(Component.literal("%.1f".format(tps)).withStyle(formatting))
                    append("\n")
                }
                append("    ")
                append(SERVER_HOSTED_BY)
                append(" ")
                append(KIWITECH)
                append("    ")
            }
        }
        return display
    }
}