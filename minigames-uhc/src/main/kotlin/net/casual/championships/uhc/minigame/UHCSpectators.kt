package net.casual.championships.uhc.minigame

import net.casual.arcade.events.server.player.PlayerAdvancementEvent
import net.casual.arcade.events.server.player.PlayerEntityInteractionEvent
import net.casual.arcade.events.server.player.PlayerSetSneakingEvent
import net.casual.arcade.events.server.player.PlayerSpectatorTeleportEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.events.server.player.PlayerVoidDamageEvent
import net.casual.arcade.guis.utils.setCustomInventory
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.events.MinigameLoadSpectatingEvent
import net.casual.arcade.minigame.events.MinigameSetSpectatingEvent
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.asLocation
import net.casual.arcade.utils.math.location.locationWithLevel
import net.casual.arcade.utils.player.hasPermission
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.CasualTags
import net.casual.championships.uhc.ui.gui.UHCSpectatorHotbarInventory
import net.casual.championships.uhc.utils.UHCMinigameRules
import net.casual.championships.uhc.utils.UHCStats
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

class UHCSpectators(
    private val uhc: UHCMinigame
): MinigameEventListener {
    @Listener
    private fun onSetSpectating(event: MinigameSetSpectatingEvent) {
        val (_, player) = event
        player.extendedGameMode = ExtendedGameMode.AdventureSpectator

        this.uhc.effects.addFullbright(player)
        this.uhc.tags.remove(player, CasualTags.HAS_TEAM_GLOW)

        if (!this.uhc.levels.has(player.level())) {
            player.teleportTo(this.uhc.overworld.asLocation(Vec3(0.0, 128.0, 0.0)))
        }

        this.uhc.scheduler.schedule(1.Ticks) {
            this.remindRules(player)
        }
    }

    @Listener
    private fun onLoadSpectating(event: MinigameLoadSpectatingEvent) {
        val player = event.player
        this.uhc.mapRenderer.startWatching(player)

        player.setCustomInventory(UHCSpectatorHotbarInventory(player, this.uhc))
    }

    @Listener(flags = ListenerFlags.IS_SPECTATOR, during = During(before = GAME_OVER_ID))
    private fun onPlayerTick(event: PlayerTickEvent) {
        val player = event.player
        if (player.isCreative) {
            return
        }
        val interval = 20.Minutes.ticks
        if (this.uhc.uptime % interval == interval - 1) {
            this.remindRules(player)
        }
    }

    @Listener
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        if (player.isSpectator) {
            event.cancel()
        }
    }

    @Listener
    private fun onPlayerAdvancement(event: PlayerAdvancementEvent) {
        if (event.player.isSpectator) {
            event.reward = false
        }
    }

    @Listener
    private fun onPlayerSpectatorTeleport(event: PlayerSpectatorTeleportEvent) {
        val (player, _) = event
        if (player.isSpectator && this.uhc.players.isSpectating(player)) {
            val target = event.getTarget()
            if (target is ServerPlayer) {
                player.teleportTo(target.locationWithLevel)
            }
        }
        event.cancel()
    }

    @Listener(flags = ListenerFlags.IS_SPECTATOR)
    private fun onPlayerSneak(event: PlayerSetSneakingEvent) {
        val (player, sneaking) = event
        if (!player.isShiftKeyDown && sneaking) {
            val last = this.uhc.stats.getOrCreateStat(player, UHCStats.LAST_SNEAK_TIME)
            if (abs(this.uhc.server.tickCount - last.value) < 7) {
                val mode = when (player.extendedGameMode) {
                    ExtendedGameMode.AdventureSpectator -> ExtendedGameMode.NoClipSpectator
                    else -> ExtendedGameMode.AdventureSpectator
                }
                player.extendedGameMode = mode
            } else {
                last.modify { this.uhc.server.tickCount }
            }
        }
    }

    @Listener(flags = ListenerFlags.IS_SPECTATOR)
    private fun onPlayerInteract(event: PlayerEntityInteractionEvent) {
        val (player, target) = event
        if (player.hasPermission(PermissionLevel.GAMEMASTERS) && target is ServerPlayer) {
            val gui = CasualGuiUtils.createPlayerInventoryViewGui(player, target)
            gui.open()
        }
    }

    private fun remindRules(player: ServerPlayer) {
        this.uhc.chat.broadcastInfo(UHCMinigameRules.getFormattedSpectatorRules(), listOf(player))
    }
}
