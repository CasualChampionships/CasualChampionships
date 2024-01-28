package net.casual.championships.minigame.duel

import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.PlayerUtils
import net.casual.championships.minigame.uhc.ui.ActiveBossBar
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
            val borderRadius = minigame.duelSettings.borderRadius
            minigame.level.worldBorder.size = borderRadius * 2

            val playing = minigame.getPlayingPlayers()
            PlayerUtils.spread(
                minigame.level,
                Vec2(0.0F, 0.0F),
                borderRadius / playing.size,
                borderRadius,
                true,
                playing
            )
            for (player in playing) {
                minigame.setPlayingPlaying(player)
            }

            GlobalTickedScheduler.later {
                minigame.setPhase(Countdown)
            }
        }

        override fun initialize(minigame: DuelMinigame) {
            minigame.setGameRules {
                resetToDefault()
                if (!minigame.duelSettings.naturalRegen) {
                    set(GameRules.RULE_NATURAL_REGENERATION, false)
                }
            }

            minigame.ui.addBossbar(ActiveBossBar(minigame))
        }
    },
    Countdown(COUNTDOWN_ID) {
        override fun start(minigame: DuelMinigame) {
            super.start(minigame)
        }
    },
    Dueling(DUELING_ID) {
        override fun start(minigame: DuelMinigame) {
            super.start(minigame)
        }
    },
    Complete(COMPLETE_ID) {
        override fun start(minigame: DuelMinigame) {
            minigame.complete()
        }
    }
}