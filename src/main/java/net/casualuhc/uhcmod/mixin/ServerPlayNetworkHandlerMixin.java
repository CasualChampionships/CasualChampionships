package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.message.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	@Final
	static Logger LOGGER;
	@Shadow
	public ServerPlayerEntity player;
	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	protected abstract void checkForSpam();

	@Shadow
	protected abstract SignedMessage getSignedMessage(ChatMessageC2SPacket packet);

	@Shadow
	protected abstract boolean canAcceptMessage(SignedMessage message);

	@Shadow
	@Final
	private MessageChainTaskQueue messageChainTaskQueue;

	@Shadow
	protected abstract CompletableFuture<FilteredMessage> filterText(String text);

	@Redirect(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean canClick(ServerPlayerEntity instance) {
		return !GameSettings.isRuleScreen(instance.currentScreenHandler) && instance.isSpectator();
	}

	@ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), argsOnly = true)
	private Packet<?> onSendPacket(Packet<?> packet) {
		if (packet instanceof EntityTrackerUpdateS2CPacket trackerUpdate) {
			Entity glowingEntity = this.player.getWorld().getEntityById(trackerUpdate.id());
			if (glowingEntity instanceof ServerPlayerEntity glowingPlayer) {
				packet = PlayerUtils.handleTrackerUpdatePacketForTeamGlowing(glowingPlayer, this.player, trackerUpdate);
			}
		}

		return packet;
	}

	// This is just awful, but it is 1 AM and I cannot be asked to use brain - Sensei
	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;", shift = At.Shift.BEFORE), cancellable = true)
	private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		this.server.submit(() -> {
			SignedMessage signedMessage = this.getSignedMessage(packet);
			if (this.canAcceptMessage(signedMessage)) {
				this.messageChainTaskQueue.append(() -> {
					CompletableFuture<FilteredMessage> completableFuture = this.filterText(signedMessage.getSignedContent().plain());
					CompletableFuture<SignedMessage> completableFuture2 = this.server.getMessageDecorator().decorate(this.player, signedMessage);
					return CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync(void_ -> {
						FilterMask filterMask = completableFuture.join().mask();
						SignedMessage message = completableFuture2.join().withFilterMask(filterMask);
						this.customHandleMessage(packet.chatMessage(), message);
					}, this.server);
				});
			}
		});
		ci.cancel();
	}

	@Unique
	private void customHandleMessage(String original, SignedMessage message) {
		Team team = (Team) this.player.getScoreboardTeam();
		if (this.player.getPublicKey() != null && !message.verify(this.player.getPublicKey())) {
			LOGGER.warn("{} sent message with invalid signature: '{}'", this.player.getName().getString(), message.getSignedContent().plain());
			return;
		}

		if (!GameManager.isPhase(Phase.ACTIVE) || TeamUtils.shouldIgnoreTeam(team) || original.startsWith("!")) {
			this.server.getPlayerManager().broadcast(message, this.player, MessageType.params(MessageType.CHAT, this.player));
		} else {
			Text text = team.getFormattedName();
			MessageType.Parameters incoming = MessageType.params(MessageType.TEAM_MSG_COMMAND_INCOMING, this.player).withTargetName(text);
			MessageType.Parameters outgoing = MessageType.params(MessageType.TEAM_MSG_COMMAND_OUTGOING, this.player).withTargetName(text);
			PlayerUtils.forEveryPlayer(playerEntity -> {
				if (playerEntity == this.player) {
					playerEntity.sendChatMessage(
						SentMessage.of(message),
						this.player.shouldFilterText() || playerEntity.shouldFilterText(),
						outgoing
					);
				} else if (team.isEqual(playerEntity.getScoreboardTeam())) {
					playerEntity.sendChatMessage(
						SentMessage.of(message),
						this.player.shouldFilterText() || playerEntity.shouldFilterText(),
						incoming
					);
				}
			});
		}
		this.checkForSpam();
	}
}
