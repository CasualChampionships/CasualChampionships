package net.casual.championships.uhc.minigame.phase

import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension.Companion.clockExtension
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter.Companion.teleport
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.level.resetToDefault
import net.casual.arcade.utils.level.set
import net.casual.arcade.utils.math.location.asLocation
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension.Companion.sharedHealthExtension
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.utils.UHCSpreadTeleporter
import net.minecraft.resources.Identifier
import net.minecraft.world.clock.ClockState
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.phys.Vec3

class InitializingRoutine: MinigameRoutine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() = step {
        minigame.levels.setGameRules {
            resetToDefault()
            set(GameRules.IMMEDIATE_RESPAWN, true, minigame.server)
            //set(GameRules.LOCATOR_BAR, false, minigame.server)
            set(GameRules.NATURAL_HEALTH_REGENERATION, false)
            set(GameRules.SPAWN_PHANTOMS, false)
        }

        minigame.settings.canPvp.set(false)
        minigame.settings.tickFreezeOnPause.set(true)
        minigame.overworld.clockExtension.set(ClockState(0, 0.0F, 1.0F, false))
        minigame.boundary.reset()

        val (level, _) = when (minigame.settings.startingDimension) {
            VanillaDimension.Overworld -> minigame.overworld to null
            VanillaDimension.Nether -> minigame.nether to 120
            VanillaDimension.End -> minigame.end to null
        }
        UHCSpreadTeleporter.teleport(level, minigame.players.playing, true)

        for (team in minigame.teams.getPlayingTeams()) {
            val extension = team.sharedHealthExtension
            extension.enabled = minigame.settings.sharingIsCaring
            if (minigame.settings.sharingIsCaring) {
                extension.set(extension.maxHealth)
            }
        }
        for (player in minigame.players.spectating) {
            if (player.level() != level) {
                player.teleportTo(level.asLocation(Vec3(0.0, 200.0, 0.0)))
            }
        }
    }

    companion object: CodecProvider<InitializingRoutine> {
        override val id: Identifier = casual("uhc_initializing")
        override val codec: MapCodec<out InitializingRoutine> = MapCodec.unit(::InitializingRoutine)
    }
}