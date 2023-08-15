package net.casualuhc.uhc.mixin.feature;

import net.casualuhc.uhc.managers.PlayerManager;
import net.casualuhc.uhc.screen.CustomScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@Redirect(
		method = "handleContainerClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"
		)
	)
	private boolean canClick(ServerPlayer instance) {
		return !(instance.containerMenu instanceof CustomScreen) && instance.isSpectator();
	}

	@ModifyVariable(
		method = "send(Lnet/minecraft/network/protocol/Packet;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	private Packet<?> onSendPacket(Packet<?> packet) {
		if (packet instanceof ClientboundSetEntityDataPacket dataUpdate) {
			Entity glowing = this.player.serverLevel().getEntity(dataUpdate.id());
			if (glowing instanceof ServerPlayer target) {
				packet = PlayerManager.handleTrackerUpdatePacketForTeamGlowing(target, this.player, dataUpdate);
			}
		}

		return packet;
	}
}
