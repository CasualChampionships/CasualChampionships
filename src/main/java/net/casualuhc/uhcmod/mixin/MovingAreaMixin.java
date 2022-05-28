package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.Event.Events;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.MovingArea.class)
public abstract class MovingAreaMixin {
	/**
	 * This is like a super jank way of doing it,
	 * but since there are 3 worlds (in vanilla)
	 * we just trigger once every 3 times...
	 */
	@Unique
	private static int counter = 0;

	@Shadow
	public abstract long getSizeLerpTime();

	@Inject(method = "getAreaInstance", at = @At("HEAD"))
	private void onGetInstance(CallbackInfoReturnable<WorldBorder.Area> cir) {
		if (this.getSizeLerpTime() <= 0 && ++counter == 3) {
			Events.WORLD_BORDER_FINISH_SHRINKING.trigger();
			counter = 0;
		}
	}
}
