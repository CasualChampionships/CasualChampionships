package net.casual.championships.common.util

import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.resources.font.IndexedBitmapFontResources
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.ComponentUtils.translatable
import net.casual.arcade.utils.ComponentUtils.translatableWithArgs
import net.casual.arcade.utils.ComponentUtils.withSpacesFont
import net.casual.championships.common.CommonMod
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object CommonComponents {
    val WELCOME_MESSAGE = translatableWithArgs("casual.welcome")
    val YES_MESSAGE by translatable("casual.yes")
    val NO_MESSAGE by translatable("casual.no")
    val GOOD_LUCK_MESSAGE by translatable("casual.goodLuck")

    val HOSTED_BY_MESSAGE by translatable("casual.hostedBy")

    val TOGGLE_TEAMGLOW = translatableWithArgs("casual.tags.teamglow")
    val TOGGLE_FULLBRIGHT = translatableWithArgs("casual.tags.fullbright")

    val ENABLE_MESSAGE by translatable("casual.toggle.enable") { lime() }
    val DISABLE_MESSAGE by translatable("casual.toggle.disable") { crimson() }
    val ENABLED_MESSAGE by translatable("casual.toggle.enabled") { lime() }
    val DISABLED_MESSAGE by translatable("casual.toggle.disabled") { crimson() }

    val PREVIOUS_MESSAGE by translatable("casual.gui.previous")
    val NEXT_MESSAGE by translatable("casual.gui.next")
    val EXIT_MESSAGE by translatable("casual.gui.exit")
    val BACK_MESSAGE by translatable("casual.gui.back")
    val SPECTATOR_TITLE_MESSAGE by translatable("casual.gui.spectator.title")

    val READY_QUERY_MESSAGE by translatable("casual.ready.question")
    val NOT_READY_MESSAGE = translatableWithArgs("casual.ready.notReady")
    val IS_READY_MESSAGE = translatableWithArgs("casual.ready.isReady")
    val CANNOT_READY_MESSAGE by translatable("casual.ready.notNow")
    val ALREADY_READY_MESSAGE by translatable("casual.ready.alreadyReady")
    val ALREADY_UNREADY_MESSAGE by translatable("casual.ready.alreadyUnready")
    val ALL_READY_MESSAGE by translatable("casual.ready.allReady")

    val TEAMS_MESSAGE by translatable("casual.team.teams")
    val TEAMMATES_MESSAGE by translatable("casual.team.teammates")
    val ADDED_TO_TEAM_MESSAGE = translatableWithArgs("casual.team.added")

    val NO_TEAM by translatable("casual.team.noTeam")
    val NOT_SPECTATING_MESSAGE by translatable("casual.spectator.notSpectating")

    val BROADCAST_POSITION_MESSAGE = translatableWithArgs("casual.position.broadcast")

    val MINESWEEPER_EXIT by translatable("casual.minesweeper.exit")
    val MINESWEEPER_DESC_1 by translatable("casual.minesweeper.desc.1")
    val MINESWEEPER_DESC_2 by translatable("casual.minesweeper.desc.2")
    val MINESWEEPER_DESC_3 by translatable("casual.minesweeper.desc.3")
    val MINESWEEPER_DESC_4 by translatable("casual.minesweeper.desc.4")
    val MINESWEEPER_FLAGS by translatable("casual.minesweeper.flags")
    val MINESWEEPER_MINE by translatable("casual.minesweeper.mine")
    val MINESWEEPER_FLAG by translatable("casual.minesweeper.flag")
    val MINESWEEPER_TIMER by translatable("casual.minesweeper.timer")
    val MINESWEEPER_PLAY_AGAIN by translatable("casual.minesweeper.playAgain")
    val MINESWEEPER_WON = translatableWithArgs("casual.minesweeper.won")
    val MINESWEEPER_LOST by translatable("casual.minesweeper.lost")
    val MINESWEEPER_RECORD = translatableWithArgs("casual.minesweeper.record")

    val DIRECTION_MESSAGE by translatable("casual.game.direction")
    val NORTH_MESSAGE by translatable("casual.game.direction.north")
    val NORTH_EAST_MESSAGE by translatable("casual.game.direction.northEast")
    val EAST_MESSAGE by translatable("casual.game.direction.east")
    val SOUTH_EAST_MESSAGE by translatable("casual.game.direction.southEast")
    val SOUTH_MESSAGE by translatable("casual.game.direction.south")
    val SOUTH_WEST_MESSAGE by translatable("casual.game.direction.southWest")
    val WEST_MESSAGE by translatable("casual.game.direction.west")
    val NORTH_WEST_MESSAGE by translatable("casual.game.direction.northWest")

    val BORDER_INFO_MESSAGE by translatable("casual.game.borderInfo")
    val BORDER_DISTANCE_MESSAGE by translatable("casual.game.borderDistance")
    val BORDER_RADIUS_MESSAGE by translatable("casual.game.borderRadius")
    val INSIDE_BORDER_MESSAGE = translatableWithArgs("casual.game.insideBorder")
    val BORDER_INITIAL_GRACE_MESSAGE = translatableWithArgs("casual.game.grace.first")
    val BORDER_GENERIC_GRACE_MESSAGE = translatableWithArgs("casual.game.grace.generic")
    val BORDER_GRACE_OVER_MESSAGE by translatable("casual.game.grace.over")
    val HAS_BEEN_ELIMINATED_MESSAGE = translatableWithArgs("casual.game.eliminated")
    val GAME_WON_MESSAGE = translatableWithArgs("casual.game.won")

    val GOLDEN_HEAD_MESSAGE by translatable("casual.item.goldenHead")
    val HEAD_TOOLTIP_MESSAGE by translatable("casual.item.head.tooltip")

    val STARTING_IN_MESSAGE = translatableWithArgs("casual.game.startingIn")
    val STARTING_SOON_MESSAGE by translatable("casual.game.startingSoon")
    val TIME_ELAPSED_MESSAGE = translatableWithArgs("casual.game.timeElapsed")
    val GRACE_MESSAGE = translatableWithArgs("casual.game.grace")
    val GLOWING_MESSAGE = translatableWithArgs("casual.game.glowing")

    val STARTING_IN_BACKGROUNDED = backgrounded(STARTING_IN_MESSAGE, Bitmap.BACKGROUND_240) { mini() }
    val STARTING_SOON_BACKGROUNDED = backgrounded({ STARTING_SOON_MESSAGE }, Bitmap.BACKGROUND_240) { mini() }
    val TIME_ELAPSED_BACKGROUNDED = backgrounded(TIME_ELAPSED_MESSAGE, Bitmap.BACKGROUND_240) { mini() }
    val GRACE_BACKGROUNDED = backgrounded(GRACE_MESSAGE, Bitmap.BACKGROUND_180) { mini() }
    val GLOWING_BACKGROUNDED = backgrounded(GLOWING_MESSAGE, Bitmap.BACKGROUND_180) { mini() }

    fun backgrounded(
        generator: ComponentUtils.ComponentGenerator,
        background: MutableComponent,
        modifier: (MutableComponent.() -> Unit)? = null
    ): ComponentUtils.ComponentGenerator {
        val key = ComponentUtils.getTranslationKeyOf(generator.generate())
        return ComponentUtils.ComponentGenerator {
            Component.empty().apply {
                append(Component.translatable("$key.space.1").withSpacesFont())
                append(background.shadowless())
                append(Component.translatable("$key.space.2").withSpacesFont())
                val component = generator.generate(*it)
                modifier?.invoke(component)
                append(component)
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

    object Bitmap: FontResources(CommonMod.id("bitmap_font")) {
        val BACKGROUND_240 by bitmap(at("backgrounds/background240.png"), 10, 12)
        val BACKGROUND_180 by bitmap(at("backgrounds/background180.png"), 10, 12)
        val BACKGROUND_120 by bitmap(at("backgrounds/background120.png"), 10, 12)
        val BACKGROUND_40 by bitmap(at("backgrounds/background40.png"), 10, 12)
        val HARDCORE_HEART by bitmap(at("hud/hardcore_heart.png"), 7, 7)
        val NO_CONNECTION by bitmap(at("hud/no_connection.png"), 7, 7)
        val UNAVAILABLE by bitmap(at("hud/cross.png"), 7, 7)
        val KILLS_COUNT by bitmap(at("hud/kills.png"), 10, 12)
        val PLAYER_COUNT by bitmap(at("hud/players.png"), 10, 11)

        val MINESWEEPER_MENU by bitmap(at("hud/minesweeper_menu.png"), 19, 227)

        val CASUAL by bitmap(at("text/casual.png"), 8, 9)
        val CHAMPIONSHIPS by bitmap(at("text/championships.png"), 8, 9)

        val SERVER_HOSTED_BY by bitmap(at("text/server_hosted_by.png"), 8, 9)
        val KIWITECH by bitmap(at("text/kiwitech.png"), 8, 9)

        val WELCOME_TO_CASUAL_CHAMPIONSHIPS by bitmap(at("text/welcome_to_casual_championships.png"), 6, 16)
    }

    object Border: IndexedBitmapFontResources(CommonMod.id("border_font")) {
        val BORDER_DISTANCE by bitmap(at("border/border_distance.png"), 8, 8)
        val BORDER_RADIUS by bitmap(at("border/border_radius.png"), 8, 8)

        init {
            for (i in 1..5) {
                indexed(at("border/blue_border_$i.png"), 8, 8)
            }
            for (i in 1..5) {
                indexed(at("border/red_border_$i.png"), 8, 8)
            }
            for (i in 1..5) {
                indexed(CommonMod.id("item/border/green_border_$i.png"), 8, 8)
            }
        }

        fun blue(index: Int): MutableComponent {
            return this.get(index - 1)
        }

        fun red(index: Int): MutableComponent {
            return this.get(index + 4)
        }

        fun green(index: Int): MutableComponent {
            return this.get(index + 9)
        }
    }
}