package net.casualuhc.uhcmod.mixin;

import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ConcentricRingsStructurePlacement.class)
public class ConcentricRingsStructurePlacementMixin {
	/**
	 * This is an awful Mixin but this is called from within the StructureSets
	 * interface and even Overwriting the function doesn't seem to be working.
	 * <p>
	 * This is the next best option - no structure apart from Strongholds use
	 * this class (at least as of 1.19.3).
	 *
	 * @author Sensei
	 */
	@ModifyVariable(method = "<init>(Lnet/minecraft/util/math/Vec3i;Lnet/minecraft/world/gen/chunk/placement/StructurePlacement$FrequencyReductionMethod;FILjava/util/Optional;IIILnet/minecraft/registry/entry/RegistryEntryList;)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
	private static int onDistance(int value) {
		return 10;
	}
}
