package net.casualuhc.uhcmod.utils.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldExtension {
    private static final int TICKS_TO_STORE_BLOCK_CHANGES = 20;

    @SuppressWarnings("unchecked")
    private final Long2ObjectMap<BlockState>[] blockChanges = Util.make(new Long2ObjectMap[TICKS_TO_STORE_BLOCK_CHANGES], arr -> Arrays.setAll(arr, i -> new Long2ObjectOpenHashMap<>()));
    private int blockChangesIndex = 0;

    public static WorldExtension forWorld(ServerWorld world) {
        return ((Holder) world).getWorldExtension();
    }

    public void tick() {
        blockChangesIndex = (blockChangesIndex + 1) % TICKS_TO_STORE_BLOCK_CHANGES;
        blockChanges[blockChangesIndex].clear();
    }

    public void storeBlockChange(BlockPos pos, BlockState oldState) {
        blockChanges[blockChangesIndex].put(pos.asLong(), oldState);
    }

    public List<BlockState> getOldBlockStatesInLastNTicks(BlockPos pos, int n) {
        if (n > TICKS_TO_STORE_BLOCK_CHANGES) {
            throw new IllegalArgumentException("Cannot get block changes more than " + TICKS_TO_STORE_BLOCK_CHANGES + " ticks ago");
        }
        List<BlockState> result = new ArrayList<>(0);
        for (int i = 0; i < n; i++) {
            var map = blockChanges[Math.floorMod(blockChangesIndex - i, TICKS_TO_STORE_BLOCK_CHANGES)];
            BlockState state = map.get(pos.asLong());
            if (state != null) {
                result.add(state);
            }
        }
        return result;
    }

    public interface Holder {
        WorldExtension getWorldExtension();
    }
}
