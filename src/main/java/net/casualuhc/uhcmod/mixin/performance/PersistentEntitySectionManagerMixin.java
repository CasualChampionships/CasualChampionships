package net.casualuhc.uhcmod.mixin.performance;

import net.casualuhc.uhcmod.util.PerformanceUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin {
	@Inject(
		method = "addEntity",
		at = @At("HEAD")
	)
	private void onAddEntity(EntityAccess entity, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof Mob mob && PerformanceUtils.isEntityAIDisabled(mob)) {
			mob.setNoAi(true);
		}
	}
}
