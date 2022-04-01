package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.Event.Events;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.MovingArea.class)
public abstract class MovingAreaMixin {
	@Shadow
	public abstract long getSizeLerpTime();

	@Inject(method = "getAreaInstance", at = @At("HEAD"))
	private void onGetInstance(CallbackInfoReturnable<WorldBorder.Area> cir) {
		if (this.getSizeLerpTime() <= 0) {
			Events.WORLD_BORDER_FINISH_SHRINKING.trigger();
		}
	}
}
