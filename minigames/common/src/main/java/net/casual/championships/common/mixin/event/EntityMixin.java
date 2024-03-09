package net.casual.championships.common.mixin.event;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.events.border.BorderEntityPortalEntryPointEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
	@Redirect(
		method = "findDimensionEntryPoint",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;clampToBounds(DDD)Lnet/minecraft/core/BlockPos;"
		)
	)
	private BlockPos clampWithinDistanceOfWorldBorder(WorldBorder border, double x, double y, double z, ServerLevel level) {
		BorderEntityPortalEntryPointEvent event = new BorderEntityPortalEntryPointEvent(border, level, (Entity) (Object) this, new Vec3(x, y, z));
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			return event.result();
		}
		return border.clampToBounds(x, y, z);
	}
}
