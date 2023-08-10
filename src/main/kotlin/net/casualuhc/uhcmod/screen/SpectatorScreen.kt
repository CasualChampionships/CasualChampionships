package net.casualuhc.uhcmod.screen

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.utils.ComponentUtils.unItalicise
import net.casualuhc.arcade.utils.ItemUtils
import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.casualuhc.arcade.utils.TeamUtils.getServerPlayers
import net.casualuhc.uhcmod.extensions.TeamFlag.Ignored
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.util.HeadUtils
import net.casualuhc.uhcmod.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.PlayerHeadItem
import net.minecraft.world.scores.Team

class SpectatorScreen(
    inventory: Inventory,
    syncId: Int,
    private val page: Int,
    private val spectators: Boolean
): CustomScreen(inventory, syncId, 6) {
    init {
        if (this.spectators) {
            createSpectatorsList()
        } else {
            createTeamList()
        }
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        if (slotId == 0 && page > 0) {
            player.openMenu(createScreenFactory(page - 1, spectators))
            return
        }
        if (slotId == 7) {
            player.openMenu(createScreenFactory(0, !spectators))
            return
        }
        if (slotId == 8) {
            player.openMenu(createScreenFactory(page + 1, spectators))
            return
        }
        if (slotId < 9 || slotId > 53 || !spectators && slotId % 9 == 0) {
            return
        }
        val clickedStack = this.slots[slotId].item
        if (clickedStack.isEmpty) {
            return
        }

        val compound = clickedStack.tag ?: return
        val owner = compound.getCompound(PlayerHeadItem.TAG_SKULL_OWNER) ?: return
        val playerName: String = owner.getString("Name")
        val target = Arcade.server.playerList.getPlayerByName(playerName)
        if (target == null) {
            player.sendSystemMessage(Texts.SPECTATOR_NOT_ONLINE.generate(playerName))
            return
        }
        (player as ServerPlayer).teleportTo(
            target.serverLevel(),
            target.x,
            target.y,
            target.z,
            target.xRot,
            target.yRot
        )
    }

    private fun createTeamList() {
        this.modifyInventory { inventory ->
            val teamList = Arcade.server.scoreboard.playerTeams
                .stream()
                .filter { !it.flags.has(Ignored) && it.getServerPlayers().isNotEmpty() }
                .skip(5L * page)
                .toList()

            for (i in 1..5) {
                if (teamList.size <= i - 1) {
                    break
                }
                var startSlot = i * 9
                val team = teamList[i - 1]
                inventory.setItem(startSlot, getDisplay(team))
                startSlot += 1
                val itemStacks: List<ItemStack> = team.getServerPlayers()
                    .stream()
                    .limit(7)
                    .map { HeadUtils.generateHead(it).setHoverName(Component.literal(it.scoreboardName).unItalicise()) }
                    .toList()
                val options = itemStacks.size
                for (j in 0 until options) {
                    inventory.setItem(startSlot + j, itemStacks[j])
                }
            }
            for (i in 1..6) {
                inventory.setItem(i, Items.GRAY_STAINED_GLASS.literalNamed(""))
            }

            inventory.setItem(0, Items.RED_STAINED_GLASS.defaultInstance.setHoverName(Texts.SPECTATOR_PREVIOUS))
            inventory.setItem(7, Items.BLUE_STAINED_GLASS.defaultInstance.setHoverName(Texts.SPECTATOR_SPECTATORS))
            inventory.setItem(8, Items.GREEN_STAINED_GLASS.defaultInstance.setHoverName(Texts.SPECTATOR_NEXT))
        }
    }

    private fun createSpectatorsList() {
        this.modifyInventory { inventory ->
            val itemStacks: List<ItemStack> = Arcade.server.scoreboard.playerTeams
                .stream()
                .filter { it.flags.has(Ignored) }
                .flatMap { it.getServerPlayers().stream() }
                .skip(45L * page)
                .limit(45)
                .map { HeadUtils.generateHead(it).setHoverName(Component.literal(it.scoreboardName).unItalicise()) }
                .toList()
            for (i in itemStacks.indices) {
                val stack: ItemStack = itemStacks[i]
                inventory.setItem(i + 9, stack)
            }
            for (i in 1..6) {
                inventory.setItem(i, Items.GRAY_STAINED_GLASS.literalNamed(""))
            }

            inventory.setItem(0, Items.RED_STAINED_GLASS.defaultInstance.setHoverName(Texts.SPECTATOR_PREVIOUS))
            inventory.setItem(7, Items.BLUE_STAINED_GLASS.defaultInstance.setHoverName(Texts.SPECTATOR_TEAMS))
            inventory.setItem(8, Items.GREEN_STAINED_GLASS.defaultInstance.setHoverName(Texts.SPECTATOR_NEXT))
        }
    }

    companion object {
        private fun getDisplay(team: Team): ItemStack {
            val texture = when (team.color) {
                ChatFormatting.BLACK -> HeadUtils.BLACK
                ChatFormatting.DARK_BLUE -> HeadUtils.DARK_BLUE
                ChatFormatting.DARK_GREEN -> HeadUtils.DARK_GREEN
                ChatFormatting.DARK_AQUA -> HeadUtils.DARK_AQUA
                ChatFormatting.DARK_RED -> HeadUtils.DARK_RED
                ChatFormatting.DARK_PURPLE -> HeadUtils.DARK_PURPLE
                ChatFormatting.GOLD -> HeadUtils.GOLD
                ChatFormatting.GRAY -> HeadUtils.GRAY
                ChatFormatting.DARK_GRAY -> HeadUtils.DARK_GRAY
                ChatFormatting.BLUE -> HeadUtils.BLUE
                ChatFormatting.GREEN -> HeadUtils.GREEN
                ChatFormatting.AQUA -> HeadUtils.AQUA
                ChatFormatting.RED -> HeadUtils.RED
                ChatFormatting.LIGHT_PURPLE -> HeadUtils.LIGHT_PURPLE
                ChatFormatting.YELLOW -> HeadUtils.YELLOW
                else -> HeadUtils.WHITE
            }
            return ItemUtils.generatePlayerHead("Dummy", texture).setHoverName(Component.literal(team.name).unItalicise())
        }

        fun createScreenFactory(page: Int, spectators: Boolean): SimpleMenuProvider? {
            return if (page < 0) {
                null
            } else SimpleMenuProvider(
                { syncId, inv, player -> SpectatorScreen(inv, syncId, page, spectators) },
                Texts.SPECTATOR_SCREEN
            )
        }
    }
}
