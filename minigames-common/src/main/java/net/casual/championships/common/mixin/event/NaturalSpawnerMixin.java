package net.casual.championships.common.mixin.event;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.ChunkGenerationMobSpawnEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
    @ModifyExpressionValue(
        method = "spawnMobsForChunkGeneration",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/biome/MobSpawnSettings;getCreatureProbability()F"
        )
    )
    private static float getSpawningProbability(
        float original,
        ServerLevelAccessor level,
        Holder<Biome> biome,
        ChunkPos chunkPos,
        @Local MobSpawnSettings settings
    ) {
        ChunkGenerationMobSpawnEvent event = new ChunkGenerationMobSpawnEvent(level, biome, chunkPos, settings, original);
        GlobalEventHandler.Server.broadcast(event);
        return event.getProbability();
    }
}
