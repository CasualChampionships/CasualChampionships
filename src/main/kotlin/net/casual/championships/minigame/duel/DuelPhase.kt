package net.casual.championships.minigame.duel

import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.MinigameUtils.countdown
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.CasualMod
import net.casual.championships.common.ui.ActiveBossBar
import net.casual.championships.common.util.CommonComponents
import net.minecraft.ChatFormatting
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.Vec2

const val INITIALIZING_ID = "initializing"
const val COUNTDOWN_ID = "countdown"
const val DUELING_ID = "dueling"
const val COMPLETE_ID = "complete"

enum class DuelPhase(
    override val id: String
): MinigamePhase<DuelMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: DuelMinigame) {
            // Un-lazy the minigame level
            minigame.level
            minigame.setGameRules {
                resetToDefault()
                if (!minigame.duelSettings.naturalRegen) {
                    set(GameRules.RULE_NATURAL_REGENERATION, false)
                }
            }

            minigame.ui.addBossbar(ActiveBossBar(minigame))

            val borderRadius = minigame.duelSettings.borderRadius
            minigame.level.worldBorder.size = borderRadius * 2

            val playing = minigame.getPlayingPlayers()
            PlayerUtils.spread(
                minigame.level,
                Vec2(0.0F, 0.0F),
                borderRadius / playing.size,
                borderRadius,
                minigame.duelSettings.teams,
                playing
            )
            for (player in playing) {
                minigame.setPlayingPlaying(player)
            }

            GlobalTickedScheduler.later {
                minigame.setPhase(Countdown)
            }
        }
    },
    Countdown(COUNTDOWN_ID) {
        override fun start(minigame: DuelMinigame) {
            minigame.settings.freezeEntities.set(true)
            minigame.ui.countdown.countdown(minigame).then {
                minigame.setPhase(Dueling)
            }
        }
    },
    Dueling(DUELING_ID) {
        override fun start(minigame: DuelMinigame) {
            minigame.settings.freezeEntities.set(false)
        }
    },
    Complete(COMPLETE_ID) {
        override fun start(minigame: DuelMinigame) {
            var winner = if (minigame.duelSettings.teams) {
                minigame.teams.getPlayingTeams().firstOrNull()?.formattedDisplayName
            } else {
                minigame.getPlayingPlayers().firstOrNull()?.displayName
            }
            if (winner == null) {
                CasualMod.logger.warn("Couldn't find winner for duel!")
                winner = "Unknown".literal().withStyle(ChatFormatting.OBFUSCATED)
            }

            val title = CommonComponents.GAME_WON_MESSAGE.generate(winner)
            for (player in minigame.getAllPlayers()) {
                player.sendTitle(title)
            }

            minigame.scheduler.schedule(20.Seconds) {
                minigame.complete()
            }
        }
    }
}