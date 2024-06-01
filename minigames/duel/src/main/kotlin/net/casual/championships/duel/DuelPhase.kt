package net.casual.championships.duel

import net.casual.arcade.gui.shapes.Regular2DPolygonShape
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.MinigameUtils.countdown
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.location.teleporter.EntityTeleporter.Companion.teleport
import net.casual.championships.common.ui.ActiveBossBar
import net.casual.championships.common.util.CommonComponents
import net.minecraft.ChatFormatting
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.util.Mth
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.Vec2
import kotlin.math.atan2
import kotlin.math.min

const val INITIALIZING_ID = "initializing"
const val COUNTDOWN_ID = "countdown"
const val DUELING_ID = "dueling"
const val COMPLETE_ID = "complete"

enum class DuelPhase(
    override val id: String
): Phase<DuelMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            minigame.arena.area.replace()

            minigame.setGameRules {
                resetToDefault()
                if (!minigame.duelSettings.naturalRegen) {
                    set(GameRules.RULE_NATURAL_REGENERATION, false)
                }
            }

            minigame.ui.addBossbar(ActiveBossBar(minigame))

            minigame.arena.teleporter.teleport(minigame.level, minigame.players.playing, minigame.duelSettings.teams)

            GlobalTickedScheduler.later {
                minigame.setPhase(Countdown)
            }
        }
    },
    Countdown(COUNTDOWN_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            minigame.settings.freezeEntities.set(true)
            minigame.ui.countdown.countdown(minigame).then {
                minigame.setPhase(Dueling)
            }
        }
    },
    Dueling(DUELING_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            minigame.settings.freezeEntities.set(false)
        }
    },
    Complete(COMPLETE_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            var winner = if (minigame.duelSettings.teams) {
                minigame.teams.getPlayingTeams().firstOrNull()?.formattedDisplayName
            } else {
                minigame.players.playing.firstOrNull()?.displayName
            }
            if (winner == null) {
                CasualDuelMod.logger.warn("Couldn't find winner for duel!")
                winner = "Unknown".literal().withStyle(ChatFormatting.OBFUSCATED)
            }

            val title = CommonComponents.GAME_WON.generate(winner)
            for (player in minigame.players) {
                player.sendTitle(title)
            }

            minigame.scheduler.schedule(20.Seconds) {
                minigame.complete()
            }
        }
    }
}