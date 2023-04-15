package net.casualuhc.uhcmod.util

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.utils.ComponentUtils.crimson
import net.casualuhc.arcade.utils.ComponentUtils.green
import net.casualuhc.uhcmod.events.uhc.UHCConfigLoadedEvent
import net.casualuhc.uhcmod.util.Texts.ComponentGenerator
import net.casualuhc.uhcmod.util.Texts.ToggleComponentGenerators
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object Texts {
    private val lang = HashMap<String, String>()

    val PACK_MESSAGE: MutableComponent get() = this.translatable("uhc.pack.message")

    val TAB_HOSTED get() = this.translatable("uhc.tab.hosted")

    val COMMAND_NO_TEAM get() = this.translatable("uhc.commands.error.noTeam")
    val COMMAND_POS get() = this.generator("uhc.commands.pos")

    val LOBBY_WELCOME get() = this.translatable("uhc.lobby.welcome")
    val LOBBY_STARTING_SOON get() = this.translatable("uhc.lobby.starting.soon")
    val LOBBY_STARTING_ONE get() = this.translatable("uhc.lobby.starting.one")
    val LOBBY_STARTING_GENERIC = this.generator("uhc.lobby.starting.generic")
    val LOBBY_READY_QUESTION get() = this.translatable("uhc.lobby.ready.question")
    val LOBBY_YES get() = this.translatable("uhc.lobby.ready.yes")
    val LOBBY_NO get() = this.translatable("uhc.lobby.ready.no")
    val LOBBY_NOT_READY = this.generator("uhc.lobby.ready.notReady")
    val LOBBY_IS_READY = this.generator("uhc.lobby.ready.isReady")
    val LOBBY_READY_NOT_NOW get() = this.translatable("uhc.lobby.ready.notNow")
    val LOBBY_READY_NO_TEAM get() = this.translatable("uhc.lobby.ready.noTeam")
    val LOBBY_READY_ALREADY get() = this.translatable("uhc.lobby.ready.alreadyReady")
    val LOBBY_READY_ALL_READY get() = this.translatable("uhc.lobby.allReady")
    val LOBBY_GOOD_LUCK get() = this.translatable("uhc.lobby.goodLuck")
    
    val ADVANCEMENT_ROOT get() = this.translatable("uhc.advancements.root")
    val ADVANCEMENT_ROOT_DESC get() = this.translatable("uhc.advancements.root.desc")
    val ADVANCEMENT_FIRST_BLOOD get() = this.translatable("uhc.advancements.firstBlood")
    val ADVANCEMENT_FIRST_BLOOD_DESC get() = this.translatable("uhc.advancements.firstBlood.desc")
    val ADVANCEMENT_EARLY_EXIT get() = this.translatable("uhc.advancements.earlyExit")
    val ADVANCEMENT_EARLY_EXIT_DESC get() = this.translatable("uhc.advancements.earlyExit.desc")
    val ADVANCEMENT_MOSTLY_HARMLESS get() = this.translatable("uhc.advancements.mostlyHarmless")
    val ADVANCEMENT_MOSTLY_HARMLESS_DESC get() = this.translatable("uhc.advancements.mostlyHarmless.desc")
    val ADVANCEMENT_HEAVY_HITTER get() = this.translatable("uhc.advancements.heavyHitter")
    val ADVANCEMENT_HEAVY_HITTER_DESC get() = this.translatable("uhc.advancements.heavyHitter.desc")
    val ADVANCEMENT_WINNER get() = this.translatable("uhc.advancements.winner")
    val ADVANCEMENT_WINNER_DESC get() = this.translatable("uhc.advancements.winner.desc")
    val ADVANCEMENT_COMBAT_LOGGER get() = this.translatable("uhc.advancements.combatLogger")
    val ADVANCEMENT_COMBAT_LOGGER_DESC get() = this.translatable("uhc.advancements.combatLogger.desc")
    val ADVANCEMENT_NOT_DUSTLESS get() = this.translatable("uhc.advancements.notDustless")
    val ADVANCEMENT_NOT_DUSTLESS_DESC get() = this.translatable("uhc.advancements.notDustless.desc")
    val ADVANCEMENT_PARKOUR_MASTER get() = this.translatable("uhc.advancements.parkourMaster")
    val ADVANCEMENT_PARKOUR_MASTER_DESC get() = this.translatable("uhc.advancements.parkourMaster.desc")
    val ADVANCEMENT_WORLD_RECORD_PACE get() = this.translatable("uhc.advancements.worldRecordPace")
    val ADVANCEMENT_WORLD_RECORD_PACE_DESC get() = this.translatable("uhc.advancements.worldRecordPace.desc")
    val ADVANCEMENT_EMBARRASSING get() = this.translatable("uhc.advancements.thatsEmbarrassing")
    val ADVANCEMENT_EMBARRASSING_DESC get() = this.translatable("uhc.advancements.thatsEmbarrassing.desc")
    val ADVANCEMENT_BUSTED get() = this.translatable("uhc.advancements.busted")
    val ADVANCEMENT_BUSTED_DESC get() = this.translatable("uhc.advancements.busted.desc")
    val ADVANCEMENT_DEMOLITION_EXPERT get() = this.translatable("uhc.advancements.demolitionExpert")
    val ADVANCEMENT_DEMOLITION_EXPERT_DESC get() = this.translatable("uhc.advancements.demolitionExpert.desc")
    val ADVANCEMENT_WE_BELIEVE get() = this.translatable("uhc.advancements.okWeBelieveYouNow")
    val ADVANCEMENT_WE_BELIEVE_DESC get() = this.translatable("uhc.advancements.okWeBelieveYouNow.desc")
    val ADVANCEMENT_FALLING_BLOCK get() = this.translatable("uhc.advancements.fallingBlock")
    val ADVANCEMENT_FALLING_BLOCK_DESC get() = this.translatable("uhc.advancements.fallingBlock.desc")
    val ADVANCEMENT_DREAM_LUCK get() = this.translatable("uhc.advancements.dreamLuck")
    val ADVANCEMENT_DREAM_LUCK_DESC get() = this.translatable("uhc.advancements.dreamLuck.desc")
    val ADVANCEMENT_BROKEN_ANKLES get() = this.translatable("uhc.advancements.brokenAnkles")
    val ADVANCEMENT_BROKEN_ANKLES_DESC get() = this.translatable("uhc.advancements.brokenAnkles.desc")
    val ADVANCEMENT_ON_THE_EDGE get() = this.translatable("uhc.advancements.onTheEdge")
    val ADVANCEMENT_ON_THE_EDGE_DESC get() = this.translatable("uhc.advancements.onTheEdge.desc")
    val ADVANCEMENT_SKILL_ISSUE get() = this.translatable("uhc.advancements.skillIssue")
    val ADVANCEMENT_SKILL_ISSUE_DESC get() = this.translatable("uhc.advancements.skillIssue.desc")
    val ADVANCEMENT_SOLOIST get() = this.translatable("uhc.advancements.soloist")
    val ADVANCEMENT_SOLOIST_DESC get() = this.translatable("uhc.advancements.soloist.desc")
    val ADVANCEMENT_NOT_NOW get() = this.translatable("uhc.advancements.notNow")
    val ADVANCEMENT_NOT_NOW_DESC get() = this.translatable("uhc.advancements.notNow.desc")
    val ADVANCEMENT_KATIE get() = this.translatable("uhc.advancements.ldap")
    val ADVANCEMENT_KATIE_DESC get() = this.translatable("uhc.advancements.ldap.desc")
    val ADVANCEMENT_BORED get() = this.translatable("uhc.advancements.officiallyBored")
    val ADVANCEMENT_BORED_DESC get() = this.translatable("uhc.advancements.officiallyBored.desc")
    val ADVANCEMENT_DISTRACTED get() = this.translatable("uhc.advancements.distracted")
    val ADVANCEMENT_DISTRACTED_DESC get() = this.translatable("uhc.advancements.distracted.desc")
    val ADVANCEMENT_FIND_THE_BUTTON get() = this.translatable("uhc.advancements.findTheButton")
    val ADVANCEMENT_FIND_THE_BUTTON_DESC get() = this.translatable("uhc.advancements.findTheButton.desc")
    val ADVANCEMENT_UH_OH get() = this.translatable("uhc.advancements.uhOh")
    val ADVANCEMENT_UH_OH_DESC get() = this.translatable("uhc.advancements.uhOh.desc")
    val ADVANCEMENT_BASICALLY get() = this.translatable("uhc.advancements.basically")
    val ADVANCEMENT_BASICALLY_DESC get() = this.translatable("uhc.advancements.basically.desc")
    val ADVANCEMENT_TEAM_PLAYER get() = this.translatable("uhc.advancements.teamPlayer")
    val ADVANCEMENT_TEAM_PLAYER_DESC get() = this.translatable("uhc.advancements.teamPlayer.desc")
    val ADVANCEMENT_LAST_MAN get() = this.translatable("uhc.advancements.lastManStanding")
    val ADVANCEMENT_LAST_MAN_DESC get() = this.translatable("uhc.advancements.lastManStanding.desc")

    val BROADCAST_HOSTED_BY get() = this.translatable("uhc.broadcast.hostedBy")
    val BROADCAST_BORDER_WARNING get() = this.translatable("uhc.broadcast.borderWarning")
    val BROADCAST_SPECTATING get() = this.translatable("uhc.broadcast.spectator")
    val BROADCAST_NO_POTIONS get() = this.translatable("uhc.broadcast.noPotions")
    val BROADCAST_GLOBAL_CHAT get() = this.translatable("uhc.broadcast.globalChat")
    val BROADCAST_NIGHT_VISION get() = this.translatable("uhc.broadcast.nighVision")
    val BROADCAST_TEAM_GLOW get() = this.translatable("uhc.broadcast.teamGlow")
    val BROADCAST_MINESWEEPER get() = this.translatable("uhc.broadcast.minesweeper")
    val BROADCAST_RULES get() = this.translatable("uhc.broadcast.rules")
    val BROADCAST_S_COMMAND get() = this.translatable("uhc.broadcast.specCommand")
    val BROADCAST_PLAYER_HEADS get() = this.translatable("uhc.broadcast.playerHeads")
    val BROADCAST_REMOVE_ME get() = this.translatable("uhc.broadcast.joke")
    
    val MINESWEEPER_EXIT get() = this.translatable("uhc.minesweeper.exit")
    val MINESWEEPER_DESC_1 get() = this.translatable("uhc.minesweeper.desc.1")
    val MINESWEEPER_DESC_2 get() = this.translatable("uhc.minesweeper.desc.2")
    val MINESWEEPER_DESC_3 get() = this.translatable("uhc.minesweeper.desc.3")
    val MINESWEEPER_DESC_4 get() = this.translatable("uhc.minesweeper.desc.4")
    val MINESWEEPER_FLAGS get() = this.translatable("uhc.minesweeper.flags")
    val MINESWEEPER_MINE get() = this.translatable("uhc.minesweeper.mine")
    val MINESWEEPER_FLAG get() = this.translatable("uhc.minesweeper.flag")
    val MINESWEEPER_TIMER get() = this.translatable("uhc.minesweeper.timer")
    val MINESWEEPER_PLAY_AGAIN get() = this.translatable("uhc.minesweeper.playAgain")
    val MINESWEEPER_WON = this.generator("uhc.minesweeper.won")
    val MINESWEEPER_LOST get() = this.translatable("uhc.minesweeper.lost")
    val MINESWEEPER_RECORD = this.generator("uhc.minesweeper.record")

    val UHC_DIRECTION get() = this.generator("uhc.game.direction")
    val UHC_DISTANCE_TO_WB get() = this.generator("uhc.game.distance")
    val UHC_WB_RADIUS get() = this.generator("uhc.game.radius")
    val UHC_GRACE_FIRST get() = this.translatable("uhc.game.grace.first")
    val UHC_GRACE_GENERIC = this.generator("uhc.game.grace.generic")
    val UHC_GRACE_OVER get() = this.translatable("uhc.game.grace.over")
    val UHC_ELIMINATED = this.generator("uhc.game.eliminated")
    val UHC_OUTSIDE_BORDER = this.generator("uhc.game.outsideBorder")
    val UHC_NORTH = this.translatable("uhc.game.north")
    val UHC_NORTH_EAST = this.translatable("uhc.game.northEast")
    val UHC_EAST = this.translatable("uhc.game.east")
    val UHC_SOUTH_EAST = this.translatable("uhc.game.southEast")
    val UHC_SOUTH = this.translatable("uhc.game.south")
    val UHC_SOUTH_WEST = this.translatable("uhc.game.southWest")
    val UHC_WEST = this.translatable("uhc.game.west")
    val UHC_NORTH_WEST = this.translatable("uhc.game.northWest")
    val UHC_ADDED_TO_TEAM = this.generator("uhc.game.addedToTeam")
    val UHC_GOLDEN_HEAD get() = this.translatable("uhc.game.goldenHead")
    val UHC_WON = this.generator("uhc.game.won")

    val SPECTATOR_NOT_SPECTATING get() = this.translatable("uhc.spectator.notDead")
    val SPECTATOR_NOT_ONLINE = this.generator("uhc.spectator.notOnline")
    val SPECTATOR_SCREEN get() = this.translatable("uhc.spectator.screen")
    val SPECTATOR_PREVIOUS get() = this.translatable("uhc.spectator.previous")
    val SPECTATOR_SPECTATORS get() = this.translatable("uhc.spectator.spectators")
    val SPECTATOR_TEAMS get() = this.translatable("uhc.spectator.teams")
    val SPECTATOR_NEXT get() = this.translatable("uhc.spectator.next")

    val UHC_TEAM_GLOW = this.toggleGenerator("uhc.teamglow")
    val UHC_FULL_BRIGHT = this.toggleGenerator("uhc.fullbright")
    val UHC_DISPLAY get() = this.toggleGenerator("uhc.display")
    val UHC_DISPLAY_COORDS get() = this.toggleGenerator("uhc.display.coords")
    val UHC_DISPLAY_DIRECTION get() = this.toggleGenerator("uhc.display.direction")
    val UHC_DISPLAY_DISTANCE get() = this.toggleGenerator("uhc.display.distance")
    val UHC_DISPLAY_RADIUS get() = this.toggleGenerator("uhc.display.radius")
    val UHC_TOGGLE_ENABLED get() = this.translatable("uhc.toggle.enabled").green()
    val UHC_TOGGLE_DISABLED get() = this.translatable("uhc.toggle.disabled").crimson()

    fun translatable(key: String): MutableComponent {
        return Component.translatableWithFallback(key, this.lang[key])
    }

    fun translatable(key: String, args: Array<out Any?>): MutableComponent {
        return Component.translatableWithFallback(key, this.lang[key], *args)
    }

    fun generator(key: String): ComponentGenerator {
        return ComponentGenerator {
            this.translatable(key, it)
        }
    }

    fun toggle(boolean: Boolean): MutableComponent {
        return if (boolean) UHC_TOGGLE_ENABLED else UHC_TOGGLE_DISABLED
    }

    fun toggleGenerator(key: String): ToggleComponentGenerators {
        return ToggleComponentGenerators { this.translatable(key, arrayOf(this.toggle(it))) }
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

    internal fun registerEvents() {
        EventHandler.register<UHCConfigLoadedEvent> {
            this.loadLang()
        }
    }

    private fun loadLang() {

    }

    fun interface ComponentGenerator {
        fun generate(vararg args: Any?): MutableComponent
    }

    fun interface ToggleComponentGenerators {
        fun generate(boolean: Boolean): MutableComponent
    }
}