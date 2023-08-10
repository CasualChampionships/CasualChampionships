package net.casualuhc.uhcmod.mixin.performance;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casualuhc.uhcmod.util.PerformanceUtils;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@Shadow @Final
	static int MAGIC_NUMBER;

	@WrapOperation(
		method = "spawnForChunk",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategory(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"
		)
	)
	private static boolean isBelowMobcap(NaturalSpawner.SpawnState instance, MobCategory category, ChunkPos pos, Operation<Boolean> original) {
		int i = category.getMaxInstancesPerChunk() * instance.getSpawnableChunkCount() / MAGIC_NUMBER;
		if (instance.getMobCategoryCounts().getInt(category) >= i * PerformanceUtils.MOBCAP_MULTIPLIER) {
			return false;
		}
		return original.call(instance, category, pos);
	}
}
