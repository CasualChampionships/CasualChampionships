package net.casual.championships.util

import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.championships.util.Texts.ComponentGenerator
import net.casual.championships.util.Texts.ToggleComponentGenerators
import net.casual.championships.util.Texts.iconed
import net.minecraft.client.Minecraft
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component.translatable
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import kotlin.reflect.KProperty

object Texts {
    val MOZART_FONT = CasualUtils.id("mozart")
    val ICON_FONT = CasualUtils.id("icons")
    val SPACES_FONT = ResourceLocation("space", "spaces")

    val CASUAL_UHC get() = Component.literal("Casual UHC")

    val PACK_MESSAGE by constant("uhc.pack.message")

    val TAB_HOSTED by constant("uhc.tab.hosted")

    val COMMAND_NO_TEAM by constant("uhc.commands.error.noTeam")
    val COMMAND_POS = generate("uhc.commands.pos")

    val LOBBY_WELCOME by constant("uhc.lobby.welcome")
    val LOBBY_READY_QUESTION by constant("uhc.lobby.ready.question")
    val LOBBY_YES by constant("uhc.lobby.ready.yes")
    val LOBBY_NO by constant("uhc.lobby.ready.no")
    val LOBBY_NOT_READY = generate("uhc.lobby.ready.notReady")
    val LOBBY_IS_READY = generate("uhc.lobby.ready.isReady")
    val LOBBY_READY_NOT_NOW by constant("uhc.lobby.ready.notNow")
    val LOBBY_READY_NO_TEAM by constant("uhc.lobby.ready.noTeam")
    val LOBBY_READY_ALREADY by constant("uhc.lobby.ready.alreadyReady")
    val LOBBY_READY_ALL_READY by constant("uhc.lobby.allReady")
    val LOBBY_GOOD_LUCK by constant("uhc.lobby.goodLuck")
    
    val ADVANCEMENT_ROOT by constant("uhc.advancements.root")
    val ADVANCEMENT_ROOT_DESC by constant("uhc.advancements.root.desc")
    val ADVANCEMENT_FIRST_BLOOD by constant("uhc.advancements.firstBlood")
    val ADVANCEMENT_FIRST_BLOOD_DESC by constant("uhc.advancements.firstBlood.desc")
    val ADVANCEMENT_EARLY_EXIT by constant("uhc.advancements.earlyExit")
    val ADVANCEMENT_EARLY_EXIT_DESC by constant("uhc.advancements.earlyExit.desc")
    val ADVANCEMENT_MOSTLY_HARMLESS by constant("uhc.advancements.mostlyHarmless")
    val ADVANCEMENT_MOSTLY_HARMLESS_DESC by constant("uhc.advancements.mostlyHarmless.desc")
    val ADVANCEMENT_HEAVY_HITTER by constant("uhc.advancements.heavyHitter")
    val ADVANCEMENT_HEAVY_HITTER_DESC by constant("uhc.advancements.heavyHitter.desc")
    val ADVANCEMENT_WINNER by constant("uhc.advancements.winner")
    val ADVANCEMENT_WINNER_DESC by constant("uhc.advancements.winner.desc")
    val ADVANCEMENT_COMBAT_LOGGER by constant("uhc.advancements.combatLogger")
    val ADVANCEMENT_COMBAT_LOGGER_DESC by constant("uhc.advancements.combatLogger.desc")
    val ADVANCEMENT_NOT_DUSTLESS by constant("uhc.advancements.notDustless")
    val ADVANCEMENT_NOT_DUSTLESS_DESC by constant("uhc.advancements.notDustless.desc")
    val ADVANCEMENT_PARKOUR_MASTER by constant("uhc.advancements.parkourMaster")
    val ADVANCEMENT_PARKOUR_MASTER_DESC by constant("uhc.advancements.parkourMaster.desc")
    val ADVANCEMENT_WORLD_RECORD_PACE by constant("uhc.advancements.worldRecordPace")
    val ADVANCEMENT_WORLD_RECORD_PACE_DESC by constant("uhc.advancements.worldRecordPace.desc")
    val ADVANCEMENT_EMBARRASSING by constant("uhc.advancements.thatsEmbarrassing")
    val ADVANCEMENT_EMBARRASSING_DESC by constant("uhc.advancements.thatsEmbarrassing.desc")
    val ADVANCEMENT_BUSTED by constant("uhc.advancements.busted")
    val ADVANCEMENT_BUSTED_DESC by constant("uhc.advancements.busted.desc")
    val ADVANCEMENT_DEMOLITION_EXPERT by constant("uhc.advancements.demolitionExpert")
    val ADVANCEMENT_DEMOLITION_EXPERT_DESC by constant("uhc.advancements.demolitionExpert.desc")
    val ADVANCEMENT_WE_BELIEVE by constant("uhc.advancements.okWeBelieveYouNow")
    val ADVANCEMENT_WE_BELIEVE_DESC by constant("uhc.advancements.okWeBelieveYouNow.desc")
    val ADVANCEMENT_FALLING_BLOCK by constant("uhc.advancements.fallingBlock")
    val ADVANCEMENT_FALLING_BLOCK_DESC by constant("uhc.advancements.fallingBlock.desc")
    val ADVANCEMENT_DREAM_LUCK by constant("uhc.advancements.dreamLuck")
    val ADVANCEMENT_DREAM_LUCK_DESC by constant("uhc.advancements.dreamLuck.desc")
    val ADVANCEMENT_BROKEN_ANKLES by constant("uhc.advancements.brokenAnkles")
    val ADVANCEMENT_BROKEN_ANKLES_DESC by constant("uhc.advancements.brokenAnkles.desc")
    val ADVANCEMENT_ON_THE_EDGE by constant("uhc.advancements.onTheEdge")
    val ADVANCEMENT_ON_THE_EDGE_DESC by constant("uhc.advancements.onTheEdge.desc")
    val ADVANCEMENT_SKILL_ISSUE by constant("uhc.advancements.skillIssue")
    val ADVANCEMENT_SKILL_ISSUE_DESC by constant("uhc.advancements.skillIssue.desc")
    val ADVANCEMENT_SOLOIST by constant("uhc.advancements.soloist")
    val ADVANCEMENT_SOLOIST_DESC by constant("uhc.advancements.soloist.desc")
    val ADVANCEMENT_NOT_NOW by constant("uhc.advancements.notNow")
    val ADVANCEMENT_NOT_NOW_DESC by constant("uhc.advancements.notNow.desc")
    val ADVANCEMENT_KATIE by constant("uhc.advancements.ldap")
    val ADVANCEMENT_KATIE_DESC by constant("uhc.advancements.ldap.desc")
    val ADVANCEMENT_BORED by constant("uhc.advancements.officiallyBored")
    val ADVANCEMENT_BORED_DESC by constant("uhc.advancements.officiallyBored.desc")
    val ADVANCEMENT_DISTRACTED by constant("uhc.advancements.distracted")
    val ADVANCEMENT_DISTRACTED_DESC by constant("uhc.advancements.distracted.desc")
    val ADVANCEMENT_FIND_THE_BUTTON by constant("uhc.advancements.findTheButton")
    val ADVANCEMENT_FIND_THE_BUTTON_DESC by constant("uhc.advancements.findTheButton.desc")
    val ADVANCEMENT_UH_OH by constant("uhc.advancements.uhOh")
    val ADVANCEMENT_UH_OH_DESC by constant("uhc.advancements.uhOh.desc")
    val ADVANCEMENT_BASICALLY by constant("uhc.advancements.basically")
    val ADVANCEMENT_BASICALLY_DESC by constant("uhc.advancements.basically.desc")
    val ADVANCEMENT_TEAM_PLAYER by constant("uhc.advancements.teamPlayer")
    val ADVANCEMENT_TEAM_PLAYER_DESC by constant("uhc.advancements.teamPlayer.desc")
    val ADVANCEMENT_LAST_MAN by constant("uhc.advancements.lastManStanding")
    val ADVANCEMENT_LAST_MAN_DESC by constant("uhc.advancements.lastManStanding.desc")

    val BROADCAST_HOSTED_BY by constant("uhc.broadcast.hostedBy")
    val BROADCAST_BORDER_WARNING by constant("uhc.broadcast.borderWarning")
    val BROADCAST_SPECTATING by constant("uhc.broadcast.spectator")
    val BROADCAST_NO_POTIONS by constant("uhc.broadcast.noPotions")
    val BROADCAST_GLOBAL_CHAT by constant("uhc.broadcast.globalChat")
    val BROADCAST_NIGHT_VISION by constant("uhc.broadcast.nighVision")
    val BROADCAST_TEAM_GLOW by constant("uhc.broadcast.teamGlow")
    val BROADCAST_MINESWEEPER by constant("uhc.broadcast.minesweeper")
    val BROADCAST_RULES by constant("uhc.broadcast.rules")
    val BROADCAST_S_COMMAND by constant("uhc.broadcast.specCommand")
    val BROADCAST_PLAYER_HEADS by constant("uhc.broadcast.playerHeads")
    val BROADCAST_REMOVE_ME by constant("uhc.broadcast.joke")
    
    val MINESWEEPER_EXIT by constant("uhc.minesweeper.exit")
    val MINESWEEPER_DESC_1 by constant("uhc.minesweeper.desc.1")
    val MINESWEEPER_DESC_2 by constant("uhc.minesweeper.desc.2")
    val MINESWEEPER_DESC_3 by constant("uhc.minesweeper.desc.3")
    val MINESWEEPER_DESC_4 by constant("uhc.minesweeper.desc.4")
    val MINESWEEPER_FLAGS by constant("uhc.minesweeper.flags")
    val MINESWEEPER_MINE by constant("uhc.minesweeper.mine")
    val MINESWEEPER_FLAG by constant("uhc.minesweeper.flag")
    val MINESWEEPER_TIMER by constant("uhc.minesweeper.timer")
    val MINESWEEPER_PLAY_AGAIN by constant("uhc.minesweeper.playAgain")
    val MINESWEEPER_WON = generate("uhc.minesweeper.won")
    val MINESWEEPER_LOST by constant("uhc.minesweeper.lost")
    val MINESWEEPER_RECORD = generate("uhc.minesweeper.record")

    val UHC_DIRECTION by constant("uhc.game.direction")
    val UHC_DISTANCE_TO_WB by constant("uhc.game.distance")
    val UHC_WB_RADIUS by constant("uhc.game.radius")
    val UHC_GRACE_FIRST by constant("uhc.game.grace.first")
    val UHC_GRACE_GENERIC = generate("uhc.game.grace.generic")
    val UHC_GRACE_OVER by constant("uhc.game.grace.over")
    val UHC_ELIMINATED = generate("uhc.game.eliminated")
    val UHC_OUTSIDE_BORDER = generate("uhc.game.outsideBorder")
    val UHC_NORTH by constant("uhc.game.north")
    val UHC_NORTH_EAST by constant("uhc.game.northEast")
    val UHC_EAST by constant("uhc.game.east")
    val UHC_SOUTH_EAST by constant("uhc.game.southEast")
    val UHC_SOUTH by constant("uhc.game.south")
    val UHC_SOUTH_WEST by constant("uhc.game.southWest")
    val UHC_WEST by constant("uhc.game.west")
    val UHC_NORTH_WEST by constant("uhc.game.northWest")
    val UHC_ADDED_TO_TEAM = generate("uhc.game.addedToTeam")
    val UHC_GOLDEN_HEAD by constant("uhc.game.goldenHead")
    val UHC_WON = generate("uhc.game.won")

    val SPECTATOR_NOT_SPECTATING by constant("uhc.spectator.notDead")
    val SPECTATOR_NOT_ONLINE = generate("uhc.spectator.notOnline")
    val SPECTATOR_SCREEN by constant("uhc.spectator.screen")
    val SPECTATOR_PREVIOUS by constant("uhc.spectator.previous")
    val SPECTATOR_SPECTATORS by constant("uhc.spectator.spectators")
    val SPECTATOR_TEAMS by constant("uhc.spectator.teams")
    val SPECTATOR_NEXT by constant("uhc.spectator.next")

    val UHC_TEAM_GLOW = generateToggle("uhc.teamglow")
    val UHC_FULL_BRIGHT = generateToggle("uhc.fullbright")
    val UHC_DISPLAY = generateToggle("uhc.display")
    val UHC_DISPLAY_COORDS = generateToggle("uhc.display.coords")
    val UHC_DISPLAY_DIRECTION = generateToggle("uhc.display.direction")
    val UHC_DISPLAY_DISTANCE = generateToggle("uhc.display.distance")
    val UHC_DISPLAY_RADIUS = generateToggle("uhc.display.radius")
    val UHC_TOGGLE_ENABLED by constant("uhc.toggle.enabled") { green() }
    val UHC_TOGGLE_DISABLED by constant("uhc.toggle.disabled") { crimson() }

    val SIDEBAR_TEAMMATES by constant("uhc.sidebar.teammates")
    val SIDEBAR_KILLS = generate("uhc.sidebar.kills")

    val TOOLTIP_HEAD by constant("uhc.tooltips.head")

    val ICON_HEART by constant("uhc.icons.heart") { iconed() }
    val ICON_NO_CONNECTION by constant("uhc.icons.noConnection") { iconed() }
    val ICON_CROSS by constant("uhc.icons.cross") { iconed() }
    val ICON_UHC by constant("uhc.icons.uhc") { iconed() }
    val ICON_WIDE_BACKGROUND by constant("uhc.icons.wideBackground") { iconed() }
    val ICON_BACKGROUND by constant("uhc.icons.background") { iconed() }
    val ICON_SHORT_BACKGROUND by constant("uhc.icons.shortBackground") { iconed() }
    val ICON_KILLS by constant("uhc.icons.kills") { iconed() }
    val ICON_PLAYERS by constant("uhc.icons.players") { iconed() }

    val BOSSBAR_STARTING = generateBossbar("uhc.bossbar.starting", ICON_WIDE_BACKGROUND)
    val BOSSBAR_STARTING_SOON = generateBossbar("uhc.bossbar.startingSoon", ICON_WIDE_BACKGROUND)
    val BOSSBAR_ELAPSED = generateBossbar("uhc.bossbar.elapsed", ICON_WIDE_BACKGROUND)
    val BOSSBAR_GRACE = generateBossbar("uhc.bossbar.grace", ICON_BACKGROUND)
    val BOSSBAR_GLOWING = generateBossbar("uhc.bossbar.glowing", ICON_BACKGROUND)

    fun literal(literal: String): MutableComponent {
        return Component.literal(literal)
    }

    fun space(top: Int, bottom: Int): MutableComponent {
        return translatable("space.$top/$bottom").withStyle { it.withFont(SPACES_FONT) }
    }

    fun space(space: Int = 4): MutableComponent {
        val clamped = Mth.clamp(space, -8192, 8192)
        return translatable("space.$clamped").withStyle { it.withFont(SPACES_FONT) }
    }

    fun offset(offset: Int, component: Component): MutableComponent {
        val clamped = Mth.clamp(offset, -8192, 8192)
        return translatable("offset.${clamped}", arrayOf(component)).withStyle { it.withFont(SPACES_FONT) }
    }

    private fun constant(key: String, modifier: (MutableComponent.() -> Unit)? = null): Constant {
        return Constant(key, modifier)
    }

    fun generate(key: String): ComponentGenerator {
        return ComponentGenerator {
            translatable(key, *it)
        }
    }

    fun generateToggle(key: String): ToggleComponentGenerators {
        return ToggleComponentGenerators { translatable(key, toggle(it)) }
    }

    fun toggle(boolean: Boolean): MutableComponent {
        return if (boolean) UHC_TOGGLE_ENABLED else UHC_TOGGLE_DISABLED
    }

    fun generateBossbar(key: String, background: MutableComponent): ComponentGenerator {
        return ComponentGenerator { args ->
            Component.empty()
                .append(translatable("$key.space.1").spaced())
                .append(background.shadowless())
                .append(translatable("$key.space.2").spaced())
                .append(translatable(key, *args))
        }
    }

    fun direction(direction: Direction): MutableComponent {
        return when (direction) {
            Direction.NORTH -> UHC_NORTH
            Direction.EAST -> UHC_EAST
            Direction.SOUTH -> UHC_SOUTH
            Direction.WEST -> UHC_WEST
            else -> Component.literal(direction.getName())
        }
    }

    fun direction(direction: Direction8): MutableComponent {
        return when (direction) {
            Direction8.NORTH -> UHC_NORTH
            Direction8.NORTH_EAST -> UHC_NORTH_EAST
            Direction8.EAST -> UHC_EAST
            Direction8.SOUTH_EAST -> UHC_SOUTH_EAST
            Direction8.SOUTH -> UHC_SOUTH
            Direction8.SOUTH_WEST -> UHC_SOUTH_WEST
            Direction8.WEST -> UHC_WEST
            Direction8.NORTH_WEST -> UHC_NORTH_WEST
        }
    }

    fun MutableComponent.monospaced(): MutableComponent {
        return this.withStyle { it.withFont(MOZART_FONT) }
    }
    
    fun MutableComponent.spaced(): MutableComponent {
        return this.withStyle { it.withFont(SPACES_FONT) }
    }

    fun MutableComponent.iconed(): MutableComponent {
        return this.withStyle { it.withFont(ICON_FONT) }
    }

    fun MutableComponent.regular(): MutableComponent {
        return this.withStyle { it.withFont(Style.DEFAULT_FONT) }
    }

    fun MutableComponent.shadowless(): MutableComponent {
        return this.withStyle { it.withColor(0x4E5C24) }
    }

    fun interface ComponentGenerator {
        fun generate(vararg args: Any?): MutableComponent
    }

    fun interface ToggleComponentGenerators {
        fun generate(boolean: Boolean): MutableComponent
    }
    
    private class Constant(private val key: String, val consumer: (MutableComponent.() -> Unit)?) {
        operator fun getValue(any: Any, property: KProperty<*>): MutableComponent {
            val component = translatable(this.key)
            this.consumer?.invoke(component)
            return component
        }
    }
}