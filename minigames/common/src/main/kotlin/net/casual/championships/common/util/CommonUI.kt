package net.casual.championships.common.util

import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.predicate.EntityObserverPredicate
import net.casual.arcade.gui.predicate.PlayerObserverPredicate
import net.casual.arcade.gui.predicate.PlayerObserverPredicate.Companion.toPlayer
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.sidebar.SidebarSupplier
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.aqua
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.TickUtils
import net.casual.championships.common.ui.BorderDistanceRow
import net.casual.championships.common.ui.TeammateRow
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object CommonUI {
    fun createPlayingNameTag(
        predicate: PlayerObserverPredicate = EntityObserverPredicate.visibleObservee().toPlayer()
    ): ArcadeNameTag {
        return ArcadeNameTag({ it.displayName!! }, predicate)
    }

    fun createPlayingHealthTag(
        predicate: PlayerObserverPredicate = CommonPredicates.VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES
    ): ArcadeNameTag {
        return ArcadeNameTag(
            { String.format("%.1f ", it.health / 2).literal().append(CommonComponents.HARDCORE_HEART_BITMAP) },
            predicate
        )
    }

    fun addTeammates(sidebar: ArcadeSidebar, size: Int): ArcadeSidebar {
        val buffer = ComponentUtils.space()
        val teammates = Component.empty().append(buffer).append(CommonComponents.TEAMMATES_MESSAGE)
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
            val display = Component.empty().append(buffer).append(CommonComponents.BORDER_RADIUS_MESSAGE)
            val score = (player.level().worldBorder.size / 2.0).toInt().toString().literal().append(buffer)
            SidebarComponent.withCustomScore(display, score)
        }
    }

    fun createTabDisplay(): ArcadeTabDisplay {
        val display = ArcadeTabDisplay(
            ComponentSupplier.of("\n".literal().append("Casual Championships").gold().bold())
        ) { _ ->
            val tps = TickUtils.calculateTPS()
            val formatting = if (tps >= 20) ChatFormatting.DARK_GREEN else if (tps > 15) ChatFormatting.YELLOW else if (tps > 10) ChatFormatting.RED else ChatFormatting.DARK_RED
            Component.literal("\n").apply {
                append("TPS: ")
                append(Component.literal("%.1f".format(tps)).withStyle(formatting))
                append("\n")
                append(CommonComponents.HOSTED_BY_MESSAGE.aqua().bold())
            }
        }
        return display
    }
}