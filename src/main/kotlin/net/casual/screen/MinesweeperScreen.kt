package net.casual.screen

import it.unimi.dsi.fastutil.ints.IntArraySet
import it.unimi.dsi.fastutil.ints.IntSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.screen.InterfaceScreen
import net.casual.arcade.utils.ItemUtils.literalNamed
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.minigame.uhc.advancement.UHCAdvancements
import net.casual.extensions.PlayerStat.MinesweeperRecord
import net.casual.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casual.items.MinesweeperItem
import net.casual.items.MinesweeperItem.Companion.EMPTY
import net.casual.items.MinesweeperItem.Companion.MINE
import net.casual.items.MinesweeperItem.Companion.UNKNOWN
import net.casual.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW
import java.util.*
import kotlin.math.floor

class MinesweeperScreen(
    player: Player,
    syncId: Int
): InterfaceScreen(player, syncId, 6) {
    private val guessed = IntArraySet()
    private val flags = IntArraySet()
    private val grid = Grid(9, 9)
    private val flagItem = Items.MANGROVE_SIGN.defaultInstance.setHoverName(Texts.MINESWEEPER_FLAGS)
    private val clockItem: ItemStack = Items.CLOCK.defaultInstance.setHoverName(Texts.MINESWEEPER_TIMER)
    private var complete = false

    init {
        this.flagItem.count = Grid.mineCount
        for (i in 0..80) {
            this.slots[i].set(UNKNOWN_TILE)
        }

        this.slots[81].set(EXIT_TILE)
        this.slots[82].set(DESC_TILE_1)
        this.slots[83].set(DESC_TILE_2)
        this.slots[84].set(DESC_TILE_3)
        this.slots[85].set(DESC_TILE_4)
        this.slots[86].set(this.flagItem)
        this.slots[87].set(this.clockItem)
        this.slots[88].set(BLANK_TILE)
        this.slots[89].set(PLAY_AGAIN_TILE)

        // TODO: Fix memory leak
        GlobalEventHandler.register<ServerTickEvent> { _ ->
            if (this.grid.startTime != 0L && !this.complete) {
                val seconds = floor((System.nanoTime() - this.grid.startTime) / 1000000000.0).toInt()
                this.clockItem.count = Mth.clamp(seconds, 1, 127)
            }
        }
    }

    override fun onClick(slotId: Int, button: Int, type: ClickType, player: ServerPlayer) {
        if (slotId >= 0 && slotId < this.grid.capacity) {
            if (type !== ClickType.PICKUP || this.complete) {
                return
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !this.flags.contains(slotId)) {
                leftClickTile(slotId, player)
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                rightClickTile(slotId)
            }
        }
        if (slotId == 81) {
            player.closeContainer()
        }
        if (slotId == 89) {
            player.openMenu(createScreenFactory())
        }
    }

    private fun leftClickTile(index: Int, player: Player) {
        this.grid.checkGenerated(index)
        this.recursiveClickTile(index, player, null)
    }

    private fun recursiveClickTile(index: Int, player: Player, checked: IntSet?) {
        val tile = this.grid.getTile(index)
        if (tile == -1) {
            onLose(player)
            return
        }
        if (tile == Int.MIN_VALUE) {
            return
        }
        this.guessed.add(index)
        val stack = getTileStack(tile)
        this.slots[index].set(stack)
        this.checkWin(player)
        if (tile == 0) {
            val set = checked ?: IntArraySet()
            set.add(index)
            for (surrounding in this.grid.getSurroundingIndices(index).iterator()) {
                if (this.flags.contains(surrounding)) {
                    this.rightClickTile(surrounding)
                }
                if (set.add(surrounding)) {
                    this.recursiveClickTile(surrounding, player, set)
                }
            }
        }
    }

    private fun rightClickTile(index: Int) {
        val current = this.slots[index]
        if (current.item !== UNKNOWN_TILE && !this.flags.contains(index)) {
            return
        }
        val stack = if (this.flags.contains(index)) {
            this.flags.remove(index)
            UNKNOWN_TILE
        } else {
            this.flags.add(index)
            Items.MANGROVE_SIGN.defaultInstance.setHoverName(Texts.MINESWEEPER_FLAG)
        }
        this.flagItem.count = (12 - flags.size).coerceAtLeast(1)
        current.set(stack)
    }

    private fun checkWin(player: Player) {
        val possibleGuesses: Int = grid.capacity - Grid.mineCount
        if (this.guessed.size == possibleGuesses) {
            this.onWin(player)
        }
    }

    private fun onWin(player: Player) {
        player as ServerPlayer
        this.complete = true
        val seconds = (System.nanoTime() - grid.startTime) / 1000000000.0
        if (seconds <= 40) {
            player.grantAdvancement(UHCAdvancements.OFFICIALLY_BORED)
        }
        val time = String.format("%.2f", seconds)
        player.sendSystemMessage(Texts.MINESWEEPER_WON.generate(time))
        if (seconds < record) {
            record = seconds
            PlayerUtils.broadcast(Texts.MINESWEEPER_RECORD.generate(player.scoreboardName, time))
        }
        val stats = player.uhcStats
        val current = stats[MinesweeperRecord]
        if (current.isNaN() || current > seconds) {
            stats[MinesweeperRecord] = seconds
        }
    }

    private fun onLose(player: Player) {
        this.complete = true
        for (i in 0 until grid.capacity) {
            this.slots[i].set(this.getTileStack(this.grid.getTile(i)))
        }
        player.sendSystemMessage(Texts.MINESWEEPER_LOST)
    }

    private fun getTileStack(tile: Int): ItemStack {
        check(!(tile > 8 || tile < -1)) { "Invalid tile: $tile" }
        return when (tile) {
            -1 -> MinesweeperItem.STATES.createStack(MINE).setHoverName(Texts.MINESWEEPER_MINE)
            0 -> MinesweeperItem.STATES.createStack(EMPTY).literalNamed("")
            else -> MinesweeperItem.STATES.createStack(tile).literalNamed(tile.toString())
        }
    }

    private class Grid(private val width: Int, height: Int) {
        val tiles: IntArray
        val capacity: Int
        var startTime: Long = 0

        init {
            this.capacity = width * height
            this.tiles = IntArray(capacity)
        }

        fun checkGenerated(first: Int) {
            if (this.startTime == 0L) {
                generate(first)
                this.startTime = System.nanoTime()
            }
        }

        fun getTile(index: Int): Int {
            return if (index < 0 || index >= this.capacity) Int.MIN_VALUE else this.tiles[index]
        }

        private fun generate(first: Int) {
            var count = mineCount
            while (count > 0) {
                val index = RANDOM.nextInt(this.capacity)
                if (index != first && !isMine(index)) {
                    setMine(index)
                    // Super jank way of checking but it works :P
                    if (countMines(first) > 0) {
                        this.tiles[index] = 0
                        continue
                    }
                    count--
                }
            }
            for (i in 0 until this.capacity) {
                this.tiles[i] = countMines(i)
            }
        }

        private fun countMines(index: Int): Int {
            // If it's a mine we don't count the mines.
            if (isMine(index)) {
                return -1
            }
            var mines = 0
            for (surrounding in this.getSurroundingIndices(index).iterator()) {
                if (isMine(surrounding)) {
                    mines++
                }
            }
            return mines
        }

        fun getSurroundingIndices(index: Int): IntSet {
            // Eliminates the indexes we need to check.
            val isLeft = index % this.width == 0
            val isRight = (index + 1) % this.width == 0
            val isTop = index - this.width < 0
            val isBottom = index + this.width > this.capacity
            val surrounding: IntSet = IntArraySet()
            // Could probably improve this but eh.
            if (!isLeft) {
                surrounding.add(index - 1)
                if (!isTop) {
                    surrounding.add(index - this.width - 1)
                }
                if (!isBottom) {
                    surrounding.add(index + this.width - 1)
                }
            }
            if (!isRight) {
                surrounding.add(index + 1)
                if (!isTop) {
                    surrounding.add(index - this.width + 1)
                }
                if (!isBottom) {
                    surrounding.add(index + this.width + 1)
                }
            }
            if (!isTop) {
                surrounding.add(index - this.width)
            }
            if (!isBottom) {
                surrounding.add(index + this.width)
            }
            return surrounding
        }

        private fun isMine(index: Int): Boolean {
            return index in 0 until this.capacity && this.tiles[index] == -1
        }

        private fun setMine(index: Int) {
            tiles[index] = -1
        }

        companion object {
            private val RANDOM = Random()
            const val mineCount = 12
        }
    }

    companion object {
        private val UNKNOWN_TILE = MinesweeperItem.STATES.createStack(UNKNOWN).literalNamed("?")
        private val EXIT_TILE = Items.RED_STAINED_GLASS.defaultInstance.setHoverName(Texts.MINESWEEPER_EXIT)
        private val DESC_TILE_1 = Items.OAK_SIGN.defaultInstance.setHoverName(Texts.MINESWEEPER_DESC_1)
        private val DESC_TILE_2 = Items.OAK_SIGN.defaultInstance.setHoverName(Texts.MINESWEEPER_DESC_2)
        private val DESC_TILE_3 = Items.OAK_SIGN.defaultInstance.setHoverName(Texts.MINESWEEPER_DESC_3)
        private val DESC_TILE_4 = Items.OAK_SIGN.defaultInstance.setHoverName(Texts.MINESWEEPER_DESC_4)
        private val BLANK_TILE = Items.GRAY_STAINED_GLASS.literalNamed("")
        private val PLAY_AGAIN_TILE = Items.GREEN_STAINED_GLASS.defaultInstance.setHoverName(Texts.MINESWEEPER_PLAY_AGAIN)

        private var record = 127.0

        fun createScreenFactory(): SimpleMenuProvider {
            return SimpleMenuProvider(
                { syncId, _, player -> MinesweeperScreen(player, syncId) },
                Component.literal("Minesweeper")
            )
        }
    }
}
