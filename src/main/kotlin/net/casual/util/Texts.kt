package net.casual.util

import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.util.Texts.ComponentGenerator
import net.casual.util.Texts.ToggleComponentGenerators
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

object Texts {
    private val lang = HashMap<String, String>()

    val MOZART_FONT = ResourceUtils.id("mozart")
    val ICON_FONT = ResourceUtils.id("icons")
    val SPACES_FONT = ResourceLocation("space", "spaces")

    val CASUAL_UHC: MutableComponent get() = Component.literal("Casual UHC")

    val PACK_MESSAGE get() = translatable("uhc.pack.message")

    val TAB_HOSTED get() = translatable("uhc.tab.hosted")

    val COMMAND_NO_TEAM get() = translatable("uhc.commands.error.noTeam")
    val COMMAND_POS = generator("uhc.commands.pos")

    val LOBBY_WELCOME get() = translatable("uhc.lobby.welcome")
    val LOBBY_READY_QUESTION get() = translatable("uhc.lobby.ready.question")
    val LOBBY_YES get() = translatable("uhc.lobby.ready.yes")
    val LOBBY_NO get() = translatable("uhc.lobby.ready.no")
    val LOBBY_NOT_READY = generator("uhc.lobby.ready.notReady")
    val LOBBY_IS_READY = generator("uhc.lobby.ready.isReady")
    val LOBBY_READY_NOT_NOW get() = translatable("uhc.lobby.ready.notNow")
    val LOBBY_READY_NO_TEAM get() = translatable("uhc.lobby.ready.noTeam")
    val LOBBY_READY_ALREADY get() = translatable("uhc.lobby.ready.alreadyReady")
    val LOBBY_READY_ALL_READY get() = translatable("uhc.lobby.allReady")
    val LOBBY_GOOD_LUCK get() = translatable("uhc.lobby.goodLuck")
    
    val ADVANCEMENT_ROOT get() = translatable("uhc.advancements.root")
    val ADVANCEMENT_ROOT_DESC get() = translatable("uhc.advancements.root.desc")
    val ADVANCEMENT_FIRST_BLOOD get() = translatable("uhc.advancements.firstBlood")
    val ADVANCEMENT_FIRST_BLOOD_DESC get() = translatable("uhc.advancements.firstBlood.desc")
    val ADVANCEMENT_EARLY_EXIT get() = translatable("uhc.advancements.earlyExit")
    val ADVANCEMENT_EARLY_EXIT_DESC get() = translatable("uhc.advancements.earlyExit.desc")
    val ADVANCEMENT_MOSTLY_HARMLESS get() = translatable("uhc.advancements.mostlyHarmless")
    val ADVANCEMENT_MOSTLY_HARMLESS_DESC get() = translatable("uhc.advancements.mostlyHarmless.desc")
    val ADVANCEMENT_HEAVY_HITTER get() = translatable("uhc.advancements.heavyHitter")
    val ADVANCEMENT_HEAVY_HITTER_DESC get() = translatable("uhc.advancements.heavyHitter.desc")
    val ADVANCEMENT_WINNER get() = translatable("uhc.advancements.winner")
    val ADVANCEMENT_WINNER_DESC get() = translatable("uhc.advancements.winner.desc")
    val ADVANCEMENT_COMBAT_LOGGER get() = translatable("uhc.advancements.combatLogger")
    val ADVANCEMENT_COMBAT_LOGGER_DESC get() = translatable("uhc.advancements.combatLogger.desc")
    val ADVANCEMENT_NOT_DUSTLESS get() = translatable("uhc.advancements.notDustless")
    val ADVANCEMENT_NOT_DUSTLESS_DESC get() = translatable("uhc.advancements.notDustless.desc")
    val ADVANCEMENT_PARKOUR_MASTER get() = translatable("uhc.advancements.parkourMaster")
    val ADVANCEMENT_PARKOUR_MASTER_DESC get() = translatable("uhc.advancements.parkourMaster.desc")
    val ADVANCEMENT_WORLD_RECORD_PACE get() = translatable("uhc.advancements.worldRecordPace")
    val ADVANCEMENT_WORLD_RECORD_PACE_DESC get() = translatable("uhc.advancements.worldRecordPace.desc")
    val ADVANCEMENT_EMBARRASSING get() = translatable("uhc.advancements.thatsEmbarrassing")
    val ADVANCEMENT_EMBARRASSING_DESC get() = translatable("uhc.advancements.thatsEmbarrassing.desc")
    val ADVANCEMENT_BUSTED get() = translatable("uhc.advancements.busted")
    val ADVANCEMENT_BUSTED_DESC get() = translatable("uhc.advancements.busted.desc")
    val ADVANCEMENT_DEMOLITION_EXPERT get() = translatable("uhc.advancements.demolitionExpert")
    val ADVANCEMENT_DEMOLITION_EXPERT_DESC get() = translatable("uhc.advancements.demolitionExpert.desc")
    val ADVANCEMENT_WE_BELIEVE get() = translatable("uhc.advancements.okWeBelieveYouNow")
    val ADVANCEMENT_WE_BELIEVE_DESC get() = translatable("uhc.advancements.okWeBelieveYouNow.desc")
    val ADVANCEMENT_FALLING_BLOCK get() = translatable("uhc.advancements.fallingBlock")
    val ADVANCEMENT_FALLING_BLOCK_DESC get() = translatable("uhc.advancements.fallingBlock.desc")
    val ADVANCEMENT_DREAM_LUCK get() = translatable("uhc.advancements.dreamLuck")
    val ADVANCEMENT_DREAM_LUCK_DESC get() = translatable("uhc.advancements.dreamLuck.desc")
    val ADVANCEMENT_BROKEN_ANKLES get() = translatable("uhc.advancements.brokenAnkles")
    val ADVANCEMENT_BROKEN_ANKLES_DESC get() = translatable("uhc.advancements.brokenAnkles.desc")
    val ADVANCEMENT_ON_THE_EDGE get() = translatable("uhc.advancements.onTheEdge")
    val ADVANCEMENT_ON_THE_EDGE_DESC get() = translatable("uhc.advancements.onTheEdge.desc")
    val ADVANCEMENT_SKILL_ISSUE get() = translatable("uhc.advancements.skillIssue")
    val ADVANCEMENT_SKILL_ISSUE_DESC get() = translatable("uhc.advancements.skillIssue.desc")
    val ADVANCEMENT_SOLOIST get() = translatable("uhc.advancements.soloist")
    val ADVANCEMENT_SOLOIST_DESC get() = translatable("uhc.advancements.soloist.desc")
    val ADVANCEMENT_NOT_NOW get() = translatable("uhc.advancements.notNow")
    val ADVANCEMENT_NOT_NOW_DESC get() = translatable("uhc.advancements.notNow.desc")
    val ADVANCEMENT_KATIE get() = translatable("uhc.advancements.ldap")
    val ADVANCEMENT_KATIE_DESC get() = translatable("uhc.advancements.ldap.desc")
    val ADVANCEMENT_BORED get() = translatable("uhc.advancements.officiallyBored")
    val ADVANCEMENT_BORED_DESC get() = translatable("uhc.advancements.officiallyBored.desc")
    val ADVANCEMENT_DISTRACTED get() = translatable("uhc.advancements.distracted")
    val ADVANCEMENT_DISTRACTED_DESC get() = translatable("uhc.advancements.distracted.desc")
    val ADVANCEMENT_FIND_THE_BUTTON get() = translatable("uhc.advancements.findTheButton")
    val ADVANCEMENT_FIND_THE_BUTTON_DESC get() = translatable("uhc.advancements.findTheButton.desc")
    val ADVANCEMENT_UH_OH get() = translatable("uhc.advancements.uhOh")
    val ADVANCEMENT_UH_OH_DESC get() = translatable("uhc.advancements.uhOh.desc")
    val ADVANCEMENT_BASICALLY get() = translatable("uhc.advancements.basically")
    val ADVANCEMENT_BASICALLY_DESC get() = translatable("uhc.advancements.basically.desc")
    val ADVANCEMENT_TEAM_PLAYER get() = translatable("uhc.advancements.teamPlayer")
    val ADVANCEMENT_TEAM_PLAYER_DESC get() = translatable("uhc.advancements.teamPlayer.desc")
    val ADVANCEMENT_LAST_MAN get() = translatable("uhc.advancements.lastManStanding")
    val ADVANCEMENT_LAST_MAN_DESC get() = translatable("uhc.advancements.lastManStanding.desc")

    val BROADCAST_HOSTED_BY get() = translatable("uhc.broadcast.hostedBy")
    val BROADCAST_BORDER_WARNING get() = translatable("uhc.broadcast.borderWarning")
    val BROADCAST_SPECTATING get() = translatable("uhc.broadcast.spectator")
    val BROADCAST_NO_POTIONS get() = translatable("uhc.broadcast.noPotions")
    val BROADCAST_GLOBAL_CHAT get() = translatable("uhc.broadcast.globalChat")
    val BROADCAST_NIGHT_VISION get() = translatable("uhc.broadcast.nighVision")
    val BROADCAST_TEAM_GLOW get() = translatable("uhc.broadcast.teamGlow")
    val BROADCAST_MINESWEEPER get() = translatable("uhc.broadcast.minesweeper")
    val BROADCAST_RULES get() = translatable("uhc.broadcast.rules")
    val BROADCAST_S_COMMAND get() = translatable("uhc.broadcast.specCommand")
    val BROADCAST_PLAYER_HEADS get() = translatable("uhc.broadcast.playerHeads")
    val BROADCAST_REMOVE_ME get() = translatable("uhc.broadcast.joke")
    
    val MINESWEEPER_EXIT get() = translatable("uhc.minesweeper.exit")
    val MINESWEEPER_DESC_1 get() = translatable("uhc.minesweeper.desc.1")
    val MINESWEEPER_DESC_2 get() = translatable("uhc.minesweeper.desc.2")
    val MINESWEEPER_DESC_3 get() = translatable("uhc.minesweeper.desc.3")
    val MINESWEEPER_DESC_4 get() = translatable("uhc.minesweeper.desc.4")
    val MINESWEEPER_FLAGS get() = translatable("uhc.minesweeper.flags")
    val MINESWEEPER_MINE get() = translatable("uhc.minesweeper.mine")
    val MINESWEEPER_FLAG get() = translatable("uhc.minesweeper.flag")
    val MINESWEEPER_TIMER get() = translatable("uhc.minesweeper.timer")
    val MINESWEEPER_PLAY_AGAIN get() = translatable("uhc.minesweeper.playAgain")
    val MINESWEEPER_WON = generator("uhc.minesweeper.won")
    val MINESWEEPER_LOST get() = translatable("uhc.minesweeper.lost")
    val MINESWEEPER_RECORD = generator("uhc.minesweeper.record")

    val UHC_DIRECTION = generator("uhc.game.direction")
    val UHC_DISTANCE_TO_WB = generator("uhc.game.distance")
    val UHC_WB_RADIUS = generator("uhc.game.radius")
    val UHC_GRACE_FIRST = translatable("uhc.game.grace.first")
    val UHC_GRACE_GENERIC = generator("uhc.game.grace.generic")
    val UHC_GRACE_OVER get() = translatable("uhc.game.grace.over")
    val UHC_ELIMINATED = generator("uhc.game.eliminated")
    val UHC_OUTSIDE_BORDER = generator("uhc.game.outsideBorder")
    val UHC_NORTH get() = translatable("uhc.game.north")
    val UHC_NORTH_EAST get() = translatable("uhc.game.northEast")
    val UHC_EAST get() = translatable("uhc.game.east")
    val UHC_SOUTH_EAST get() = translatable("uhc.game.southEast")
    val UHC_SOUTH get() = translatable("uhc.game.south")
    val UHC_SOUTH_WEST get() = translatable("uhc.game.southWest")
    val UHC_WEST get() = translatable("uhc.game.west")
    val UHC_NORTH_WEST get() = translatable("uhc.game.northWest")
    val UHC_ADDED_TO_TEAM = generator("uhc.game.addedToTeam")
    val UHC_GOLDEN_HEAD get() = translatable("uhc.game.goldenHead")
    val UHC_WON = generator("uhc.game.won")

    val SPECTATOR_NOT_SPECTATING get() = translatable("uhc.spectator.notDead")
    val SPECTATOR_NOT_ONLINE = generator("uhc.spectator.notOnline")
    val SPECTATOR_SCREEN get() = translatable("uhc.spectator.screen")
    val SPECTATOR_PREVIOUS get() = translatable("uhc.spectator.previous")
    val SPECTATOR_SPECTATORS get() = translatable("uhc.spectator.spectators")
    val SPECTATOR_TEAMS get() = translatable("uhc.spectator.teams")
    val SPECTATOR_NEXT get() = translatable("uhc.spectator.next")

    val UHC_TEAM_GLOW = toggleGenerator("uhc.teamglow")
    val UHC_FULL_BRIGHT = toggleGenerator("uhc.fullbright")
    val UHC_DISPLAY = toggleGenerator("uhc.display")
    val UHC_DISPLAY_COORDS = toggleGenerator("uhc.display.coords")
    val UHC_DISPLAY_DIRECTION = toggleGenerator("uhc.display.direction")
    val UHC_DISPLAY_DISTANCE = toggleGenerator("uhc.display.distance")
    val UHC_DISPLAY_RADIUS = toggleGenerator("uhc.display.radius")
    val UHC_TOGGLE_ENABLED = translatable("uhc.toggle.enabled").green()
    val UHC_TOGGLE_DISABLED = translatable("uhc.toggle.disabled").crimson()

    val SIDEBAR_TEAMMATES get() = translatable("uhc.sidebar.teammates")
    val SIDEBAR_KILLS = generator("uhc.sidebar.kills")

    val TOOLTIP_HEAD get() = translatable("uhc.tooltips.head")

    val BOSSBAR_STARTING get() = bossbar("uhc.bossbar.starting", ICON_WIDE_BACKGROUND)
    val BOSSBAR_ELAPSED get() = bossbar("uhc.bossbar.elapsed", ICON_WIDE_BACKGROUND)
    val BOSSBAR_GRACE get() = bossbar("uhc.bossbar.grace", ICON_BACKGROUND)
    val BOSSBAR_GLOWING get() = bossbar("uhc.bossbar.glowing", ICON_BACKGROUND)

    val ICON_HEART get() = literal("\uE000").iconed()
    val ICON_NO_CONNECTION get() = literal("\uE001").iconed()
    val ICON_CROSS get() = literal("\uE002").iconed()
    val ICON_UHC get() = literal("\uE003").iconed()
    val ICON_WIDE_BACKGROUND get() = literal("\uE004").iconed()
    val ICON_BACKGROUND get() = literal("\uE005").iconed()
    val ICON_SHORT_BACKGROUND get() = literal("\uE008").iconed()
    val ICON_KILLS get() = literal("\uE009").iconed()
    val ICON_PLAYERS get() = literal("\uE00A").iconed()

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

    fun translatable(key: String): MutableComponent {
        return Component.translatableWithFallback(key, lang[key])
    }

    fun translatable(key: String, args: Array<out Any?>): MutableComponent {
        return Component.translatableWithFallback(key, lang[key], *args)
    }

    fun generator(key: String): ComponentGenerator {
        return ComponentGenerator {
            translatable(key, it)
        }
    }

    fun toggle(boolean: Boolean): MutableComponent {
        return if (boolean) UHC_TOGGLE_ENABLED else UHC_TOGGLE_DISABLED
    }

    fun toggleGenerator(key: String): ToggleComponentGenerators {
        return ToggleComponentGenerators { translatable(key, arrayOf(toggle(it))) }
    }

    fun bossbar(key: String, background: MutableComponent): ComponentGenerator {
        return ComponentGenerator { args ->
            Component.empty().append(
                translatable("$key.space.1").withStyle { it.withFont(SPACES_FONT) }
            ).append(
                background.shadowless()
            ).append(
                translatable("$key.space.2").withStyle { it.withFont(SPACES_FONT) }
            ).append(
                translatable(key, args).monospaced()
            )
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
}