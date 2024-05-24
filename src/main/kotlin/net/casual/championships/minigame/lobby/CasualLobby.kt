package net.casual.championships.minigame.lobby

import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.Location
import net.casual.championships.minigame.CasualMinigames
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.FireworkExplosion.Shape
import net.minecraft.world.item.component.Fireworks
import kotlin.random.Random

class CasualLobby(
    override val area: PlaceableArea,
    override val spawn: Location,
    private val bossbar: TimerBossBarTemplate,
    private val countdown: CountdownTemplate,
    private val podium: Location,
    private val podiumView: Location,
    private val fireworks: List<Location>
): Lobby {
    override fun getSpawn(player: ServerPlayer): Location {
        if (CasualMinigames.isWinner(player)) {
            return this.podium
        } else if (CasualMinigames.hasWinner()) {
            return this.podiumView
        }
        return super.getSpawn(player)
    }

    override fun createBossbar(): TimerBossBar {
        return this.bossbar.create()
    }

    override fun getCountdown(): Countdown {
        return this.countdown.create()
    }

    fun spawnFireworks(scheduler: MinecraftScheduler) {
        val colours = IntList.of(0xFFE577, 0x7ABDE5)
        val fade = IntList.of(0xFDA63A, 0x688FE5)

        val shapes = listOf(Shape.LARGE_BALL, Shape.STAR, Shape.BURST)

        val stacks = ArrayList<ItemStack>()
        for (i in 0..4) {
            val stack = ItemStack(Items.FIREWORK_ROCKET)

            val explosions = shapes.asSequence().shuffled().take(Random.nextInt(1, 4)).mapTo(ArrayList()) {
                val index = Random.nextInt(0, colours.size)
                FireworkExplosion(
                    it,
                    IntList.of(colours.getInt(index)),
                    IntList.of(fade.getInt(index)),
                    true,
                    true
                )
            }

            stack.set(DataComponents.FIREWORKS, Fireworks(Random.nextInt(1, 3), explosions))
            stacks.add(stack)
        }

        for (location in this.fireworks) {
            scheduler.scheduleInLoop(MinecraftTimeDuration.ZERO, 10.Ticks, 10.Seconds) {
                val firework = FireworkRocketEntity(
                    location.level,
                    location.x + Random.nextDouble(0.0, 0.5),
                    location.y + Random.nextDouble(0.0, 0.5),
                    location.z + Random.nextDouble(0.0, 0.5),
                    stacks.random()
                )
                location.level.addFreshEntity(firework)
            }
        }
    }
}