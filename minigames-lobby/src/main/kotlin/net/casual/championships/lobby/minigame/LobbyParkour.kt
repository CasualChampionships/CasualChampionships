package net.casual.championships.lobby.minigame

import eu.pb4.sgui.api.GuiHelpers
import eu.pb4.sgui.api.gui.HotbarGui
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.guis.sgui.setSlot
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.player.grantAdvancement
import net.casual.arcade.utils.player.sendSound
import net.casual.arcade.utils.player.sendTitle
import net.casual.championships.lobby.advancement.LobbyAdvancements
import net.casual.championships.lobby.minigame.modules.LobbyParkourData
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.Items
import java.util.*

class LobbyParkour(
    private val lobby: LobbyMinigame
) {
    private val parkourers = Object2IntOpenHashMap<UUID>()

    fun initialize() {
        this.lobby.effects.setInvisiblePredicate(this::checkInvisibility, true)

        val data = this.lobby.modules.get<LobbyParkourData>() ?: return
        this.lobby.events.register<ServerTickEvent> { this.tick(data) }
    }

    private fun tick(data: LobbyParkourData) {
        for (player in this.lobby.players) {
            val isParkouring = data.isWithinParkourArea(player.position())
            val wasParkouring = this.parkourers.containsKey(player.uuid)
            if (!isParkouring) {
                if (wasParkouring) {
                    this.parkourers.removeInt(player.uuid)
                    val gui = GuiHelpers.getCurrentGui(player) as? ParkourHotbarGui
                    gui?.close()
                }
                continue
            }

            if (GuiHelpers.getCurrentGui(player) == null) {
                ParkourHotbarGui(player, data.exit).open()
            }

            val currentCheckpointIndex = this.parkourers.getInt(player.uuid)
            val updatedCheckpointIndex = data.getIntersectingCheckpoint(player.position(), currentCheckpointIndex)
            val checkpoint = data.checkpoints[updatedCheckpointIndex]
            if (currentCheckpointIndex != updatedCheckpointIndex || !wasParkouring) {
                this.parkourers.put(player.uuid, updatedCheckpointIndex)
                player.sendTitle(checkpoint.title)
                player.sendSound(SoundEvents.ARROW_HIT_PLAYER)
                if (updatedCheckpointIndex == data.checkpoints.lastIndex) {
                    player.grantAdvancement(LobbyAdvancements.PARKOUR_MASTER)
                }
            }

            val yTeleportThreshold = checkpoint.collision.minY - 15
            if (player.y < yTeleportThreshold) {
                player.teleportTo(checkpoint.spawn)
            }
        }
    }

    private fun checkInvisibility(observee: ServerPlayer, observer: ServerPlayer): Boolean {
        if (observee == observer) {
            return false
        }
        if (!this.parkourers.containsKey(observee.uuid) || !this.parkourers.containsKey(observer.uuid)) {
            return false
        }
        return observer.closerThan(observee, 5.0)
    }

    private class ParkourHotbarGui(
        player: ServerPlayer,
        private val exit: Location
    ): HotbarGui(player) {
        override fun onOpen() {
            val barrier = Items.BARRIER.named(Component.translatable("lobby.parkour.exit").bold().red()).hideTooltip()
            this.setSlot(8, barrier) { _ ->
                this.player.teleportTo(this.exit)
            }
        }
    }
}