package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.uhc.TickSyncedBorderExtent;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public class WorldBorderMixin {
	@Shadow
	private WorldBorder.Area area;

	@Inject(method = "interpolateSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;getListeners()Ljava/util/List;"))
	private void getExtent(double d, double e, long l, CallbackInfo ci) {
		this.area = new TickSyncedBorderExtent((WorldBorder) (Object) this, l, d, e);
	}
}
