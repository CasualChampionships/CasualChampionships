package net.casual.championships.minigame.lobby

import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.entity.firework.VirtualFirework
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.impl.PlayerTask
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.location.template.LocationTemplate
import net.casual.championships.duel.arena.DuelArenasTemplate
import net.casual.championships.minigame.CasualMinigames
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.FireworkExplosion.Shape
import kotlin.random.Random

class CasualLobby(
    override val area: PlaceableArea,
    private val spawnTemplate: LocationTemplate,
    private val bossbarTemplate: TimerBossBarTemplate,
    private val countdownTemplate: CountdownTemplate,
    private val podiumTemplate: LocationTemplate,
    private val podiumViewTemplate: LocationTemplate,
    private val fireworksLocations: List<LocationTemplate>,
    val duelArenaTemplates: List<DuelArenasTemplate>
): Lobby {
    override val spawn: Location
        get() = this.spawnTemplate.get(this.area.level)

    override fun getSpawn(player: ServerPlayer): Location {
        if (CasualMinigames.isWinner(player)) {
            return this.podiumTemplate.get(this.area.level)
        } else if (CasualMinigames.hasWinner()) {
            return this.podiumViewTemplate.get(this.area.level)
        }
        return super.getSpawn(player)
    }

    override fun createBossbar(): TimerBossBar {
        return this.bossbarTemplate.create()
    }

    override fun getCountdown(): Countdown {
        return this.countdownTemplate.create()
    }

    fun spawnFireworksFor(player: ServerPlayer, scheduler: MinecraftScheduler) {
        scheduler.scheduleInLoop(MinecraftTimeDuration.ZERO, 10.Ticks, 10.Seconds, PlayerTask(player) {
            if (it.level() !== this.area.level) {
                return@PlayerTask
            }
            for (template in this.fireworksLocations) {
                val firingLocation = template.get(this.area.level)
                val firework = VirtualFirework.build {
                    location = firingLocation
                    duration = Random.nextInt(20, 30).Ticks

                    SHAPES.asSequence().shuffled().take(Random.nextInt(1, 4)).forEach { shape ->
                        val index = Random.nextInt(PRIMARY.size)
                        explosion {
                            shape(shape)
                            addPrimaryColours(PRIMARY.getInt(index))
                            addFadeColours(FADE.getInt(index))
                            trail()
                            twinkle()
                        }
                    }
                }
                firework.sendTo(it)
            }
        })
    }

    companion object {
        private val PRIMARY = IntList.of(0xea3323, 0xff8b00, 0xfebb26, 0x1eb253, 0x017cf3, 0x9c78fe)
        private val FADE = IntList.of(0xde324c, 0xf4895f, 0xf8e16f, 0x95cf92, 0x369acc, 0x9656a2)

        private val SHAPES = listOf(Shape.LARGE_BALL, Shape.STAR, Shape.BURST)
    }
}