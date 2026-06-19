package net.casual.championships.uhc.gui

import net.casual.arcade.guis.inventory.VirtualInventory
import net.casual.arcade.guis.utils.SlotInteractAction
import net.casual.arcade.guis.utils.isUse
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.AdventureSpectator
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.NoClipSpectator
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.player.StaticResolvableProfile
import net.casual.arcade.utils.player.sendSound
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import kotlin.random.Random

class UHCSpectatorHotbarInventory(
    player: ServerPlayer,
    private val uhc: UHCMinigame
): VirtualInventory(player) {
    private var ticks = 0

    init {
        this.initialize()
    }

    override fun tick() {
        super.tick()
        if (++this.ticks % 100 == 0 && Random.nextInt(0, 5) == 0) {
            this.setOpenTeleporter()
        }
    }

    private fun initialize() {
        this.setGamemodeSwitcher()
        this.setOpenTeleporter()

        for ((i, map) in this.uhc.mapRenderer.getMaps().withIndex()) {
            this.setMap(i, map)
        }
    }

    private fun setGamemodeSwitcher() {
        val switcher = CasualGuiItems.GAMEMODE_SWITCHER.named(Component.translatable("casual.spectator.gamemodeSwitcher"))
        this.setSlot(7, switcher, { this.switchGamemode() }) { action ->
            action.isUse() && this.switchGamemode()
        }
    }

    private fun setOpenTeleporter() {
        val players = CasualItems.FORWARD_FACING_PLAYER_HEAD.named(Component.translatable("casual.spectator.teleport"))
        val profile = this.uhc.players.allProfiles.random()
        players.set(DataComponents.PROFILE, StaticResolvableProfile(profile.name, profile.id))
        this.setSlot(8, players, { this.openTeleporter() }) { action ->
            action.isUse() && this.openTeleporter()
        }
    }

    private fun setMap(index: Int, map: ItemStack) {
        this.setSlot(index, map) { action ->
            this.swapMaps(index, action)
        }
    }

    private fun switchGamemode(): Boolean {
        val player = this.player()
        player.sendSound(SoundEvents.UI_BUTTON_CLICK)
        val mode = when (player.extendedGameMode) {
            AdventureSpectator -> NoClipSpectator
            else -> AdventureSpectator
        }
        player.extendedGameMode = mode
        return true
    }

    private fun openTeleporter(): Boolean {
        val player = this.player()
        val gui = CasualGuiUtils.createTeamSelectionGui(this.uhc, player)
        player.sendSound(SoundEvents.UI_BUTTON_CLICK)
        gui.open()
        return true
    }

    private fun swapMaps(index: Int, action: SlotInteractAction): Boolean {
        if (action != SlotInteractAction.SwapOffhand) {
            return false
        }

        val mainhand = this.getItem(index)
        val offhand = this.getItem(SLOT_OFFHAND)
        this.setSlot(index, offhand)
        this.setSlot(SLOT_OFFHAND, mainhand)
        return true
    }
}