package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(World.class)
public class WorldMixin {
	@Shadow @Mutable @Final private WorldBorder border;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateWorld(
		MutableWorldProperties properties,
		RegistryKey<World> registryRef,
		DynamicRegistryManager registryManager,
		RegistryEntry<DimensionType> dimensionEntry,
		Supplier<Profiler> profiler,
		boolean isClient,
		boolean debugWorld,
		long biomeAccess,
		int maxChainedNeighborUpdates,
		CallbackInfo ci
	) {
		this.border = WorldBorderManager.GLOBAL_BORDER;
	}
}
