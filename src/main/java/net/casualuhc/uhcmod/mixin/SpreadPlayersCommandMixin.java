package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.TeamManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.SpreadPlayersCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(SpreadPlayersCommand.class)
public class SpreadPlayersCommandMixin {
	@Redirect(method = "getMinDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z"))
	private static boolean onTeleport(Entity entity, ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch) {
		if (entity instanceof ServerPlayerEntity && !entity.isSpectator() && !TeamManager.shouldIgnoreTeam(entity.getScoreboardTeam())) {
			return entity.teleport(world, destX, destY, destZ, flags, yaw, pitch);
		}
		return false;
	}
}
