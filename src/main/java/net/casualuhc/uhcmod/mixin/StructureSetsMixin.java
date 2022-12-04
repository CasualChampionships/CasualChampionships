package net.casualuhc.uhcmod.mixin;

import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.structure.StructureSets;
import net.minecraft.tag.BiomeTags;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StructureSets.class)
public interface StructureSetsMixin {
	@Shadow
	static RegistryEntry<StructureSet> register(RegistryKey<StructureSet> key, StructureSet structureSet) {
		throw new AssertionError();
	}

	/**
	 * @author Sensei
	 * @reason Cannot redirect in interface
	 */
	@Overwrite
	static RegistryEntry<StructureSet> register(RegistryKey<StructureSet> key, RegistryEntry<Structure> structure, StructurePlacement placement) {
		if (key == StructureSetKeys.STRONGHOLDS) {
			placement = new ConcentricRingsStructurePlacement(10, 3, 128, BuiltinRegistries.BIOME.getOrCreateEntryList(BiomeTags.STRONGHOLD_BIASED_TO));
		}
		return register(key, new StructureSet(structure, placement));
	}
}
