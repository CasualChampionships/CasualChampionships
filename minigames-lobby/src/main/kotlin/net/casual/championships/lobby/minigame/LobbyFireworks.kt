package net.casual.championships.lobby.minigame

import net.casual.arcade.scheduler.task.impl.PlayerTask
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.entity.firework.VirtualFirework
import net.casual.championships.lobby.minigame.modules.LobbyData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.FireworkExplosion.Shape
import kotlin.random.Random

class LobbyFireworks(
    private val lobby: LobbyMinigame,
    private val data: LobbyData
) {
    fun spawnFireworkDisplayFor(player: ServerPlayer) {
        this.lobby.scheduler.scheduleInLoop(
            MinecraftTimeDuration.ZERO, 10.Ticks, 10.Seconds, PlayerTask(player, this::spawnFireworkDisplayBurstFor)
        )
    }

    private fun spawnFireworkDisplayBurstFor(player: ServerPlayer) {
        val level = player.level()
        if (level != this.lobby.level) {
            return
        }

        val data = this.data
        for (template in this.data.fireworkLocations) {
            val firework = VirtualFirework.build(level) {
                location = template.get().with(level)
                duration = Random.nextInt(20, 30).Ticks

                for (shape in getRandomShapes()) {
                    val index = Random.nextInt(data.fireworkColors.size)
                    explosion {
                        shape(shape)
                        addPrimaryColors(data.fireworkColors[index])
                        addFadeColors(data.fireworkColors[index])
                        trail()
                        twinkle()
                    }
                }
            }
            firework.sendTo(player)
        }
    }

    private fun getRandomShapes(): Sequence<Shape> {
        return SHAPES.asSequence().shuffled().take(Random.nextInt(1, 4))
    }

    companion object {
        private val SHAPES = listOf(Shape.LARGE_BALL, Shape.STAR, Shape.BURST)
    }
}