package net.casualuhc.uhcmod.mixin.feature;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Redirect(
		method = "createLevels",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V"
		)
	)
	private void onSyncListener(WorldBorder instance, BorderChangeListener listener) {
		// Do nothing - we don't want to add delegate listener as we have 1 singular world border
	}
}
