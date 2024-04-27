package net.casual.championships.missilewars

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.level.VanillaLikeDimensions
import net.casual.arcade.level.VanillaLikeRuntimeLevel
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.phase.Phased
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.utils.FantasyUtils
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.impl.Location
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig

class ExampleMod: ModInitializer {
    private lateinit var minigame: ExampleMinigame

    override fun onInitialize() {
        GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
            minigame = ExampleMinigame(server)
        }
        GlobalEventHandler.register<PlayerJoinEvent> { (player) ->
            minigame.players.add(player, spectating = false)
        }
    }
}

class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    override val id: ResourceLocation = ResourceLocation("modid", "example")

    override fun getPhases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }

    override fun initialize() {
        super.initialize()
    }
}

enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun start(minigame: ExampleMinigame) {
            minigame.settings.canPvp.set(false)

            // In 10 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(10.Minutes, PhaseChangeTask(minigame, Active))
        }
    },
    Active("active") {
        override fun start(minigame: ExampleMinigame) {
            minigame.settings.canPvp.set(true)

            // In 30 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(30.Minutes, PhaseChangeTask(minigame, DeathMatch))
        }
    },
    DeathMatch("death_match") {
        override fun start(minigame: ExampleMinigame) {
            // Change to location of the arena
            val location = Location.of()
            for (player in minigame.players.playing) {
                player.teleportTo(location)
            }
        }
    }
}