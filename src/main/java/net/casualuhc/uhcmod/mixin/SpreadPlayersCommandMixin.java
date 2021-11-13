package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.SpreadPlayersCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpreadPlayersCommand.class)
public class SpreadPlayersCommandMixin {
	@Redirect(method = "getMinDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleport(DDD)V"))
	private static void onTeleport(Entity entity, double x, double y, double z) {
		if (entity instanceof ServerPlayerEntity && !entity.isSpectator() && !TeamUtils.isNonTeam(entity.getScoreboardTeam())) {
			entity.teleport(x, y, z);
		}
	}
}
