package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.screen.CustomScreen;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Redirect(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean canClick(ServerPlayerEntity instance) {
		return !(instance.currentScreenHandler instanceof CustomScreen) && instance.isSpectator();
	}

	@ModifyVariable(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), argsOnly = true)
	private Packet<?> onSendPacket(Packet<?> packet) {
		if (packet instanceof EntityTrackerUpdateS2CPacket trackerUpdate) {
			Entity glowingEntity = this.player.getWorld().getEntityById(trackerUpdate.id());
			if (glowingEntity instanceof ServerPlayerEntity glowingPlayer) {
				packet = PlayerManager.handleTrackerUpdatePacketForTeamGlowing(glowingPlayer, this.player, trackerUpdate);
			}
		}

		return packet;
	}
}
