package net.casual.championships.common.extensions

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.casual.arcade.extensions.Extension
import net.casual.arcade.utils.LevelUtils.getExtension
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

class WorldBlockTrackerExtension: Extension {
    private val tracker = Array(TICKS_TO_STORE_CHANGES) { Long2ObjectOpenHashMap<BlockState>() }
    private var blockChangesIndex = 0

    fun tick() {
        this.blockChangesIndex = (this.blockChangesIndex + 1) % TICKS_TO_STORE_CHANGES
        this.tracker[this.blockChangesIndex].clear()
    }

    fun track(pos: BlockPos, oldState: BlockState) {
        this.tracker[blockChangesIndex].put(pos.asLong(), oldState)
    }

    fun getTrackedStatesInLastNTicks(pos: BlockPos, n: Int): List<BlockState> {
        require(n <= TICKS_TO_STORE_CHANGES) {
            "Cannot get block changes more than $TICKS_TO_STORE_CHANGES ticks ago"
        }

        val result = ArrayList<BlockState>(0)
        for (i in 0 until n) {
            val map = this.tracker[Math.floorMod(blockChangesIndex - i, TICKS_TO_STORE_CHANGES)]
            val state = map[pos.asLong()]
            if (state != null) {
                result.add(state)
            }
        }
        return result
    }

    companion object {
        private const val TICKS_TO_STORE_CHANGES = 20

        val ServerLevel.blockTracker
            get() = this.getExtension(WorldBlockTrackerExtension::class.java)
    }
}