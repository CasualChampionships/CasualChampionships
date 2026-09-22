package net.casual.championships.uhc.minigame.phase

import com.mojang.serialization.MapCodec
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.launch
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.player.sendSound
import net.casual.arcade.utils.player.sendTitle
import net.casual.arcade.utils.scoreboard.color
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.serialization.codec.optionalOf
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualTags
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import java.util.*
import kotlin.jvm.optionals.getOrNull

class GameOverRoutine: MinigameRoutine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        val teamName = step(ComponentSerialization.CODEC.optionalOf()) {
            minigame.stats.freeze()

            minigame.settings.canPvp.set(false)
            minigame.settings.isChatGlobal = true
            minigame.settings.canTakeDamage.set(false)

            val teams = minigame.teams.getPlayingTeams()
            if (teams.size != 1) {
                return@step Optional.empty()
            }

            val team = teams.first()
            minigame.uhcAdvancements.grantFinalAdvancements(team.getOnlinePlayers())

            for (player in team.getOnlinePlayers()) {
                minigame.tags.add(player, CasualTags.WON)
            }
            Optional.of(Component.literal(team.name).color(team))
        }.getOrNull()

        if (teamName == null) {
            val message = "No valid winning team could be found!"
            minigame.chat.broadcastTo(Component.literal(message), minigame.players.admins)
            awaitCancellation()
        }

        step {
            minigame.scopes.current.launch { playWinScreen(minigame, teamName) }
        }

        delay(20.Seconds)
        minigame.complete()
    }

    private suspend fun playWinScreen(minigame: Minigame, teamName: Component) = coroutineScope {
        for (player in minigame.players) {
            player.sendTitle(CasualComponents.GAME_WON.generate(teamName).withMiniFont())
        }

        // TODO: Better winning screen
        launch {
            repeat(25) {
                for (player in minigame.players) {
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST_FAR, volume = 0.5F)
                }
                launch {
                    delay(6.Ticks)
                    for (player in minigame.players) {
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_SHOOT, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, volume = 0.5F)
                    }
                }
                delay(4.Ticks)
            }
        }
    }

    companion object: CodecProvider<GameOverRoutine> {
        override val id: Identifier = casual("uhc_game_over")
        override val codec: MapCodec<out GameOverRoutine> = MapCodec.unit(::GameOverRoutine)
    }
}