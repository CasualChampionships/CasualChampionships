package net.casual.championships.common.mixin.event;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.portal.PortalCreateValidPositionEvent;
import net.casual.championships.common.event.portal.PortalFindValidPositionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;
import java.util.stream.Stream;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
	@Shadow @Final private ServerLevel level;

	@Redirect(
		method = "createPortal",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean onCreatePortal(WorldBorder instance, BlockPos pos) {
		PortalCreateValidPositionEvent event = new PortalCreateValidPositionEvent(this.level, pos, instance.isWithinBounds(pos));
		GlobalEventHandler.Server.broadcast(event);
		return event.getValid();
	}

	@ModifyReceiver(
		method = "findClosestPortalPosition",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/stream/Stream;min(Ljava/util/Comparator;)Ljava/util/Optional;"
		)
	)
	private Stream<BlockPos> onFindClosetPortal(Stream<BlockPos> instance, Comparator<BlockPos> comparator) {
		return instance.filter(pos -> {
			PortalFindValidPositionEvent event = new PortalFindValidPositionEvent(this.level, pos, true);
			GlobalEventHandler.Server.broadcast(event);
			return event.getValid();
		});
	}
}
