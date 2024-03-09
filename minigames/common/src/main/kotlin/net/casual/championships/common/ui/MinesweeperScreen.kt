package net.casual.championships.common.ui

import it.unimi.dsi.fastutil.ints.IntArraySet
import it.unimi.dsi.fastutil.ints.IntSet
import net.casual.arcade.gui.screen.ArcadeGenericScreen
import net.casual.arcade.gui.screen.FrozenUsableScreen
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.championships.common.item.MinesweeperItem
import net.casual.championships.common.item.MinesweeperItem.Companion.EMPTY
import net.casual.championships.common.item.MinesweeperItem.Companion.MINE
import net.casual.championships.common.item.MinesweeperItem.Companion.UNKNOWN
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonStats
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
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
): ArcadeGenericScreen(player, syncId, 6), FrozenUsableScreen {
    private val guessed = IntArraySet()
    private val flags = IntArraySet()
    private val grid = Grid(9, 9)
    private val flagItem = Items.MANGROVE_SIGN.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_FLAGS)
    private val clockItem: ItemStack = Items.CLOCK.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_TIMER)
    private var complete = false

    init {
        this.flagItem.count = Grid.MINE_COUNT
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
    }

    override fun isFrozenUsable(player: ServerPlayer): Boolean {
        return true
    }

    override fun onTick(server: MinecraftServer) {
        if (this.grid.startTime != 0L && !this.complete) {
            val seconds = floor((System.nanoTime() - this.grid.startTime) / 1000000000.0).toInt()
            this.clockItem.count = Mth.clamp(seconds, 1, 127)
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

    private fun leftClickTile(index: Int, player: ServerPlayer) {
        this.grid.checkGenerated(index)
        this.recursiveClickTile(index, player, null)
    }

    private fun recursiveClickTile(index: Int, player: ServerPlayer, checked: IntSet?) {
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
            Items.MANGROVE_SIGN.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_FLAG)
        }
        this.flagItem.count = (12 - flags.size).coerceAtLeast(1)
        current.set(stack)
    }

    private fun checkWin(player: ServerPlayer) {
        val possibleGuesses: Int = grid.capacity - Grid.MINE_COUNT
        if (this.guessed.size == possibleGuesses) {
            this.onWin(player)
        }
    }

    private fun onWin(player: ServerPlayer) {
        this.complete = true
        val seconds = (System.nanoTime() - grid.startTime) / 1000000000.0
        if (seconds <= 40) {
            // TODO:
            // player.grantAdvancement(UHCAdvancements.OFFICIALLY_BORED)
        }
        val time = String.format("%.2f", seconds)
        player.sendSystemMessage(CommonComponents.MINESWEEPER_WON.generate(time))
        if (seconds < record) {
            record = seconds
            PlayerUtils.broadcast(CommonComponents.MINESWEEPER_RECORD.generate(player.scoreboardName, time))
        }

        val minigame = player.getMinigame() ?: return
        val stat = minigame.stats.getOrCreateStat(player, CommonStats.MINESWEEPER_RECORD)
        if (stat.value.isNaN() || stat.value > seconds) {
            stat.modify { seconds }
        }
    }

    private fun onLose(player: ServerPlayer) {
        this.complete = true
        for (i in 0 until grid.capacity) {
            this.slots[i].set(this.getTileStack(this.grid.getTile(i)))
        }
        player.sendSystemMessage(CommonComponents.MINESWEEPER_LOST)
    }

    private fun getTileStack(tile: Int): ItemStack {
        check(tile <= 8 && tile >= -1) { "Invalid tile: $tile" }
        return when (tile) {
            -1 -> MINE.setHoverName(CommonComponents.MINESWEEPER_MINE)
            0 -> EMPTY.named("")
            else -> MinesweeperItem.MODELLER.create(tile).named(tile.toString())
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
            var count = MINE_COUNT
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
            // If it's a mine we ignore; we don't count the mines.
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
            // Could probably improve this, but eh.
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
            const val MINE_COUNT = 12
        }
    }

    companion object {
        private val UNKNOWN_TILE = UNKNOWN.named("?")
        private val EXIT_TILE = Items.RED_STAINED_GLASS.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_EXIT)
        private val DESC_TILE_1 = Items.OAK_SIGN.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_DESC_1)
        private val DESC_TILE_2 = Items.OAK_SIGN.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_DESC_2)
        private val DESC_TILE_3 = Items.OAK_SIGN.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_DESC_3)
        private val DESC_TILE_4 = Items.OAK_SIGN.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_DESC_4)
        private val BLANK_TILE = Items.GRAY_STAINED_GLASS.named("")
        private val PLAY_AGAIN_TILE = Items.GREEN_STAINED_GLASS.defaultInstance.setHoverName(CommonComponents.MINESWEEPER_PLAY_AGAIN)

        private var record = 127.0

        fun createScreenFactory(): SimpleMenuProvider {
            return SimpleMenuProvider(
                { syncId, _, player -> MinesweeperScreen(player, syncId) },
                Component.literal("Minesweeper")
            )
        }
    }
}
