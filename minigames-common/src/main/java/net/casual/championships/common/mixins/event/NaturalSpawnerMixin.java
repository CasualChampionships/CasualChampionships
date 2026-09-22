package net.casual.championships.common.mixins.event;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.ChunkGenerationMobSpawnEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
    @ModifyVariable(
        method = "spawnMobsForChunkGeneration",
        at = @At("STORE"),
        name = "creatureProbability"
    )
    private static float getSpawningProbability(
        float creatureProbability,
        @Local(argsOnly = true) ServerLevelAccessor level,
        @Local(argsOnly = true) ChunkPos chunkPos,
        @Local(name = "mobSettings") MobSpawnSettings mobSettings
    ) {
        ChunkGenerationMobSpawnEvent event = new ChunkGenerationMobSpawnEvent(level, chunkPos, mobSettings, creatureProbability);
        GlobalEventHandler.Server.broadcast(event);
        return event.getProbability();
    }
}
