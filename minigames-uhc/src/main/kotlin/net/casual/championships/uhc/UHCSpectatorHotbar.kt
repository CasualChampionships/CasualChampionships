package net.casual.championships.uhc

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.HotbarGui
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.visuals.screen.setSlot
import net.casual.championships.common.util.CommonItems
import net.casual.championships.common.util.CommonUI
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

class UHCSpectatorHotbar(
    player: ServerPlayer,
    private val uhc: UHCMinigame
): HotbarGui(player) {
    private val maps = ArrayList<GuiElement>()

    init {
        for ((i, map) in this.uhc.mapRenderer.getMaps().withIndex()) {
            val element = this.createMapGuiElement(map, i)
            this.maps.add(i, element)
            this.addSlot(element)
        }
        val players = ItemStack(CommonItems.FORWARD_FACING_PLAYER_HEAD)
            .named(Component.translatable("casual.spectator.teleport"))
        players.set(DataComponents.PROFILE, ResolvableProfile(player.gameProfile))
        this.setSlot(8, players) { ->
            val gui = CommonUI.createTeamSelectionGui(this.uhc, this.player)
            gui.setParent(this)
            gui.open()
        }
    }

    override fun onClickBlock(result: BlockHitResult): Boolean {
        val state = this.player.serverLevel().getBlockState(result.blockPos)
        val menu = state.getMenuProvider(this.player.serverLevel(), result.blockPos)
        if (menu != null) {
            this.player.openMenu(menu)
            return false
        }
        return super.onClickBlock(result)
    }

    override fun onClickEntity(id: Int, type: EntityInteraction, sneaking: Boolean, pos: Vec3?): Boolean {
        val entity = this.player.serverLevel().getEntity(id)
        if (entity != null && type == EntityInteraction.ATTACK) {
            this.player.camera = entity
            return false
        }
        return super.onClickEntity(id, type, sneaking, pos)
    }

    override fun canPlayerClose(): Boolean {
        return false
    }

    private fun createMapGuiElement(map: ItemStack, index: Int): GuiElement {
        return GuiElement(map) { _, type, _, _ ->
            if (type == ClickType.OFFHAND_SWAP) {
                val current = this.maps[index]
                val offhand = this.getSlot(9)?.itemStack ?: ItemStack.EMPTY
                if (current.itemStack.isEmpty) {
                    current.itemStack = offhand
                    this.setSlot(9, ItemStack.EMPTY)
                } else if (offhand.isEmpty) {
                    this.setSlot(9, current.itemStack)
                    current.itemStack = ItemStack.EMPTY
                }
            }
        }
    }
}