package net.casual.championships.duel.minigame

import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.restrictMovement
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.unrestrictMovement
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter.Companion.teleport
import net.casual.arcade.minigame.utils.MinigameUtils.countdown
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.TeamUtils.color
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.resetToDefault
import net.casual.arcade.utils.set
import net.casual.championships.common.ui.bossbar.ActiveBossbar
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualStats
import net.casual.championships.common.util.CasualUtils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.level.GameRules

internal const val INITIALIZING_ID = "initializing"
internal const val COUNTDOWN_ID = "countdown"
internal const val DUELING_ID = "dueling"
internal const val COMPLETE_ID = "complete"

enum class DuelPhase(
    override val id: String
): Phase<DuelMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            minigame.levels.setGameRules {
                resetToDefault()
                set(GameRules.RULE_DO_IMMEDIATE_RESPAWN, true)
                set(GameRules.RULE_COMMANDBLOCKOUTPUT, false)
                set(GameRules.RULE_RANDOMTICKING, 0)
                set(GameRules.RULE_LOCATOR_BAR, false)
                if (!minigame.duelSettings.naturalRegen) {
                    set(GameRules.RULE_NATURAL_REGENERATION, false)
                }
            }

            minigame.ui.addBossbar(ActiveBossbar(minigame))

            minigame.duelArena.data.teleporter.teleport(minigame.level, minigame.players.playing, minigame.duelSettings.teams)

            minigame.settings.canInteractAll = false
            minigame.settings.canAttackEntities.set(false)

            GlobalTickedScheduler.later {
                minigame.setPhase(Countdown)
            }
        }
    },
    Countdown(COUNTDOWN_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            for (player in minigame.players.playing) {
                player.restrictMovement()
            }
            minigame.ui.countdown.countdown(minigame).then {
                minigame.setPhase(Dueling)
            }
        }
    },
    Dueling(DUELING_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            minigame.settings.canInteractAll = true
            minigame.settings.canAttackEntities.set(true)

            for (player in minigame.players.playing) {
                player.unrestrictMovement()
            }
        }
    },
    Complete(COMPLETE_ID) {
        override fun start(minigame: DuelMinigame, previous: Phase<DuelMinigame>) {
            var winner = if (minigame.duelSettings.teams) {
                val winners = minigame.teams.getPlayingTeams().firstOrNull()
                if (winners != null) {
                    for (winner in winners.getOnlinePlayers()) {
                        minigame.stats.getOrCreateStat(winner, CasualStats.WON).modify { true }
                    }
                }
                winners?.formattedDisplayName
            } else {
                val winner = minigame.players.playing.firstOrNull()
                if (winner != null) {
                    minigame.stats.getOrCreateStat(winner, CasualStats.WON).modify { true }
                    val named = Component.literal(winner.scoreboardName)
                    val team = winner.team
                    if (team != null) {
                        named.color(team)
                    }
                    named
                } else {
                    null
                }
            }
            if (winner == null) {
                CasualUtils.logger.warn("Couldn't find winner for duel!")
                winner = Component.literal("Unknown").withStyle(ChatFormatting.OBFUSCATED)
            }

            val title = CasualComponents.GAME_WON.generate(winner)
            for (player in minigame.players) {
                player.sendTitle(title)
            }

            minigame.scheduler.schedule(10.Seconds) {
                minigame.complete()
            }

            minigame.stats.freeze()
        }
    }
}