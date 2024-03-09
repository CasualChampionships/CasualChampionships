package net.casual.championships.common.util

import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.ComponentUtils.withFont
import net.casual.arcade.utils.ComponentUtils.withSpacesFont
import net.casual.championships.common.CasualCommonMod
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import kotlin.reflect.KProperty

object CommonComponents {
    val CASUAL_IMAGE_FONT = CasualCommonMod.id("bitmap_font")

    val WELCOME_MESSAGE = generate("casual.welcome")
    val YES_MESSAGE by constant("casual.yes")
    val NO_MESSAGE by constant("casual.no")
    val GOOD_LUCK_MESSAGE by constant("casual.goodLuck")

    val HOSTED_BY_MESSAGE by constant("casual.hostedBy")

    val TOGGLE_TEAMGLOW = generate("casual.tags.teamglow")
    val TOGGLE_FULLBRIGHT = generate("casual.tags.fullbright")

    val ENABLED_MESSAGE by constant("casual.toggle.enabled") { lime() }
    val DISABLED_MESSAGE by constant("casual.toggle.disabled") { crimson() }

    val PREVIOUS_MESSAGE by constant("casual.gui.previous")
    val NEXT_MESSAGE by constant("casual.gui.next")
    val EXIT_MESSAGE by constant("casual.gui.exit")
    val BACK_MESSAGE by constant("casual.gui.back")
    val SPECTATOR_TITLE_MESSAGE by constant("casual.gui.spectator.title")

    val READY_QUERY_MESSAGE by constant("casual.ready.question")
    val NOT_READY_MESSAGE = generate("casual.ready.notReady")
    val IS_READY_MESSAGE = generate("casual.ready.isReady")
    val CANNOT_READY_MESSAGE by constant("casual.ready.notNow")
    val ALREADY_READY_MESSAGE by constant("casual.ready.alreadyReady")
    val ALREADY_UNREADY_MESSAGE by constant("casual.ready.alreadyUnready")
    val ALL_READY_MESSAGE by constant("casual.ready.allReady")

    val TEAMS_MESSAGE by constant("casual.team.teams")
    val TEAMMATES_MESSAGE by constant("casual.team.teammates")
    val ADDED_TO_TEAM_MESSAGE = generate("casual.team.added")

    val NO_TEAM by constant("casual.team.noTeam")
    val NOT_SPECTATING_MESSAGE by constant("casual.spectator.notSpectating")

    val BROADCAST_POSITION_MESSAGE = generate("casual.position.broadcast")

    val MINESWEEPER_EXIT by constant("casual.minesweeper.exit")
    val MINESWEEPER_DESC_1 by constant("casual.minesweeper.desc.1")
    val MINESWEEPER_DESC_2 by constant("casual.minesweeper.desc.2")
    val MINESWEEPER_DESC_3 by constant("casual.minesweeper.desc.3")
    val MINESWEEPER_DESC_4 by constant("casual.minesweeper.desc.4")
    val MINESWEEPER_FLAGS by constant("casual.minesweeper.flags")
    val MINESWEEPER_MINE by constant("casual.minesweeper.mine")
    val MINESWEEPER_FLAG by constant("casual.minesweeper.flag")
    val MINESWEEPER_TIMER by constant("casual.minesweeper.timer")
    val MINESWEEPER_PLAY_AGAIN by constant("casual.minesweeper.playAgain")
    val MINESWEEPER_WON = generate("casual.minesweeper.won")
    val MINESWEEPER_LOST by constant("casual.minesweeper.lost")
    val MINESWEEPER_RECORD = generate("casual.minesweeper.record")

    val DIRECTION_MESSAGE by constant("casual.game.direction")
    val NORTH_MESSAGE by constant("casual.game.direction.north")
    val NORTH_EAST_MESSAGE by constant("casual.game.direction.northEast")
    val EAST_MESSAGE by constant("casual.game.direction.east")
    val SOUTH_EAST_MESSAGE by constant("casual.game.direction.southEast")
    val SOUTH_MESSAGE by constant("casual.game.direction.south")
    val SOUTH_WEST_MESSAGE by constant("casual.game.direction.southWest")
    val WEST_MESSAGE by constant("casual.game.direction.west")
    val NORTH_WEST_MESSAGE by constant("casual.game.direction.northWest")

    val BORDER_DISTANCE_MESSAGE by constant("casual.game.borderDistance")
    val BORDER_RADIUS_MESSAGE by constant("casual.game.borderRadius")
    val INSIDE_BORDER_MESSAGE = generate("casual.game.insideBorder")
    val BORDER_INITIAL_GRACE_MESSAGE = generate("casual.game.grace.first")
    val BORDER_GENERIC_GRACE_MESSAGE = generate("casual.game.grace.generic")
    val BORDER_GRACE_OVER_MESSAGE by constant("casual.game.grace.over")
    val HAS_BEEN_ELIMINATED_MESSAGE = generate("casual.game.eliminated")
    val GAME_WON_MESSAGE = generate("casual.game.won")

    val GOLDEN_HEAD_MESSAGE by constant("casual.item.goldenHead")
    val HEAD_TOOLTIP_MESSAGE by constant("casual.item.head.tooltip")

    val STARTING_IN_MESSAGE = generate("casual.game.startingIn")
    val STARTING_SOON_MESSAGE by constant("casual.game.startingSoon")
    val TIME_ELAPSED_MESSAGE = generate("casual.game.timeElapsed")
    val GRACE_MESSAGE = generate("casual.game.grace")
    val GLOWING_MESSAGE = generate("casual.game.glowing")

    val BACKGROUND_240_BITMAP by bitmap("\uE000")
    val BACKGROUND_180_BITMAP by bitmap("\uE001")
    val BACKGROUND_120_BITMAP by bitmap("\uE002")
    val BACKGROUND_40_BITMAP by bitmap("\uE003")
    val HARDCORE_HEART_BITMAP by bitmap("\uE004")
    val NO_CONNECTION_BITMAP by bitmap("\uE005")
    val UNAVAILABLE_BITMAP by bitmap("\uE006")
    val KILLS_COUNT_BITMAP by bitmap("\uE007")
    val PLAYER_COUNT_BITMAP by bitmap("\uE008")

    val STARTING_IN_BACKGROUNDED = backgrounded(STARTING_IN_MESSAGE.generate(), BACKGROUND_240_BITMAP)
    val STARTING_SOON_BACKGROUNDED = backgrounded(STARTING_SOON_MESSAGE, BACKGROUND_240_BITMAP)
    val TIME_ELAPSED_BACKGROUNDED = backgrounded(TIME_ELAPSED_MESSAGE.generate(), BACKGROUND_240_BITMAP)
    val GRACE_BACKGROUNDED = backgrounded(GRACE_MESSAGE.generate(), BACKGROUND_180_BITMAP)
    val GLOWING_BACKGROUNDED = backgrounded(GLOWING_MESSAGE.generate(), BACKGROUND_180_BITMAP)

    fun generate(key: String): ComponentGenerator {
        return ComponentGenerator {
            Component.translatable(key, *it)
        }
    }

    fun constant(key: String, modifier: (MutableComponent.() -> Unit)? = null): Constant {
        return Constant(key, Component::translatable, modifier)
    }

    fun bitmap(unicode: String, modifier: (MutableComponent.() -> Unit)? = null): Constant {
        return Constant(unicode, Component::literal) {
            withFont(CASUAL_IMAGE_FONT)
            modifier?.invoke(this)
        }
    }

    fun backgrounded(component: Component, background: MutableComponent): ComponentGenerator {
        val key = ComponentUtils.getTranslationKeyOf(component)
        return ComponentGenerator {
            Component.empty().apply {
                append(Component.translatable("$key.space.1").withSpacesFont())
                append(background.shadowless())
                append(Component.translatable("$key.space.2").withSpacesFont())
                append(Component.translatable(key, *it))
            }
        }
    }

    fun direction(direction: Direction): MutableComponent {
        return when (direction) {
            Direction.NORTH -> NORTH_MESSAGE
            Direction.EAST -> EAST_MESSAGE
            Direction.SOUTH -> SOUTH_MESSAGE
            Direction.WEST -> WEST_MESSAGE
            else -> Component.literal(direction.getName())
        }
    }

    fun direction(direction: Direction8): MutableComponent {
        return when (direction) {
            Direction8.NORTH -> NORTH_MESSAGE
            Direction8.NORTH_EAST -> NORTH_EAST_MESSAGE
            Direction8.EAST -> EAST_MESSAGE
            Direction8.SOUTH_EAST -> SOUTH_EAST_MESSAGE
            Direction8.SOUTH -> SOUTH_MESSAGE
            Direction8.SOUTH_WEST -> SOUTH_WEST_MESSAGE
            Direction8.WEST -> WEST_MESSAGE
            Direction8.NORTH_WEST -> NORTH_WEST_MESSAGE
        }
    }

    fun interface ComponentGenerator {
        fun generate(vararg args: Any?): MutableComponent
    }

    fun interface ToggleComponentGenerators {
        fun generate(boolean: Boolean): MutableComponent
    }

    class Constant(
        private val key: String,
        private val supplier: (String) -> MutableComponent,
        private val consumer: (MutableComponent.() -> Unit)?,
    ) {
        operator fun getValue(any: Any, property: KProperty<*>): MutableComponent {
            val component = this.supplier(this.key)
            this.consumer?.invoke(component)
            return component
        }
    }
}