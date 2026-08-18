package net.casual.championships.uhc.ui.elements

import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.formatMMSS
import net.casual.arcade.utils.component.wrap
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.virtual.visuals.elements.LevelSpecificElement
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.championships.uhc.boundary.UHCBoundary
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel

class BoundaryMovingElement(
    private val boundary: UHCBoundary,
    private val buffer: Component
): LevelSpecificElement<SidebarComponent> {
    override fun get(level: ServerLevel): SidebarComponent {
        val boundary = level.levelBoundary ?: return SidebarComponent.EMPTY
        if (boundary.shape.getStatus().isMoving()) {
            return this.createRow("casual.game.borderPausingIn", this.boundary.getTimeUntilPause())
        }
        if (this.boundary.isFinal(level)) {
            return SidebarComponent.withNoScore(
                this.buffer.wrap().append(this.buffer)
                    .append(Component.translatable("casual.game.borderFinished").withMiniFont())
            )
        }
        return this.createRow("casual.game.borderMovingIn", this.boundary.getTimeUntilMove())
    }

    private fun createRow(translation: String, remaining: MinecraftTimeDuration): SidebarComponent {
        val display = this.buffer.wrap().append(this.buffer)
            .append(Component.translatable(translation).withMiniFont())
        val score = Component.literal(remaining.formatMMSS())
            .withStyle(this.colorTime(remaining))
            .withMiniFont()
            .append(this.buffer)
        return SidebarComponent.withCustomScore(display, score)
    }

    private fun colorTime(time: MinecraftTimeDuration): ChatFormatting {
        return when {
            time > 30.Minutes -> DARK_GREEN
            time > 15.Minutes -> GREEN
            time > 8.Minutes -> YELLOW
            time > 3.Minutes -> GOLD
            time > 1.Minutes -> RED
            else -> DARK_RED
        }
    }
}
