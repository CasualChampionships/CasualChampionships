package net.casual.championships.common.ui.elements

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.MathUtils.contains
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.virtual.visuals.elements.LevelSpecificElement
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.championships.common.util.CasualComponents
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import java.util.*

class BorderStatusElement(private val buffer: Component): LevelSpecificElement<SidebarComponent> {
    override fun get(level: ServerLevel): SidebarComponent {
        val phase = ((level.server.tickCount / 3) % 5) + 1
        val border = when (level.levelBoundary?.getStatus()) {
            BoundaryShape.Status.Shrinking -> CasualComponents.Border.red(phase)
            BoundaryShape.Status.Growing -> CasualComponents.Border.green(phase)
            else -> CasualComponents.Border.blue(phase)
        }
        val display = Component.empty()
            .append(this.buffer)
            .append(border)
            .append(SpacingFontResources.spaced(4))
            .append(CasualComponents.BORDER_INFO.withMiniFont())
        return SidebarComponent.withNoScore(display)
    }
}

class BorderDistanceElement(private val buffer: Component): PlayerSpecificElement<SidebarComponent> {
    override fun get(player: ServerPlayer): SidebarComponent {
        val boundary = player.level().levelBoundary ?: return SidebarComponent.EMPTY
        val position = player.position()

        val display = Component { empty() + buffer + " " + CasualComponents.BORDER_DISTANCE.withMiniFont() }
        val score = Component {
            val size = boundary.getSize()
            val horizontal = distance(boundary, position, HORIZONTAL)
            val dist = literal("XZ: ") + literal("$horizontal").withStyle(color(horizontal * 2.0 / size.x))
            if (size.y <= player.level().height * 2) {
                val vertical = distance(boundary, position, VERTICAL)
                dist + " Y: " + literal("$vertical").withStyle(color(vertical * 2.0 / size.y))
            }
            dist + buffer
        }
        return SidebarComponent.withCustomScore(display, score.withMiniFont())
    }

    private fun distance(boundary: LevelBoundary, position: Vec3, axes: EnumSet<Direction.Axis>): Int {
        val direction = boundary.getDirectionFrom(position, axes)
        val multiplier = if (boundary.getAABB().contains(position, axes)) 1 else -1
        return multiplier * direction.length().toInt()
    }

    private fun color(percent: Double): ChatFormatting {
        return when {
            percent > 0.4 -> DARK_GREEN
            percent > 0.2 -> YELLOW
            percent > 0.1 -> RED
            else -> DARK_RED
        }
    }

    private companion object {
        val HORIZONTAL: EnumSet<Direction.Axis> = EnumSet.of(Direction.Axis.X, Direction.Axis.Z)
        val VERTICAL: EnumSet<Direction.Axis> = EnumSet.of(Direction.Axis.Y)
    }
}

class BorderSizeElement(private val buffer: Component): LevelSpecificElement<SidebarComponent> {
    override fun get(level: ServerLevel): SidebarComponent {
        val display = Component.empty().append(this.buffer).append(" ").append(CasualComponents.BORDER_RADIUS.withMiniFont())
        val score = Component.literal(((level.levelBoundary?.getSize()?.x ?: 0.0) / 2.0).toInt().toString()).append(this.buffer)
        return SidebarComponent.withCustomScore(display, score.withMiniFont())
    }
}