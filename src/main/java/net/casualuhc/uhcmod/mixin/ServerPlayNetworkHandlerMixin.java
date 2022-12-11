package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.screen.CustomScreen;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.message.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// TODO:
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
	@Final
	private MessageChainTaskQueue messageChainTaskQueue;

	@Shadow
	protected abstract CompletableFuture<FilteredMessage> filterText(String text);

	@Shadow private @Nullable PublicPlayerSession session;

	@Shadow protected abstract void handleMessageChainException(MessageChain.MessageChainException exception);

	@Shadow protected abstract SignedMessage getSignedMessage(ChatMessageC2SPacket packet, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException;

	@Shadow protected abstract Optional<LastSeenMessageList> validateMessage(String message, Instant timestamp, LastSeenMessageList.Acknowledgment acknowledgment);

	@Redirect(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean canClick(ServerPlayerEntity instance) {
		return !(instance.currentScreenHandler instanceof CustomScreen) && instance.isSpectator();
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

	@Inject(method = "onResourcePackStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER))
	private void onResourcePackStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
		PlayerExtension.get(this.player).hasResourcePack = packet.getStatus() == ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED;
	}

	// This is just awful, but it is 1 AM and I cannot be asked to use brain - Sensei
	// I mean technically you could redirect, but I need access to the raw message which I cannot get in 3 nested lambdas :pain:
	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;validateMessage(Ljava/lang/String;Ljava/time/Instant;Lnet/minecraft/network/message/LastSeenMessageList$Acknowledgment;)Ljava/util/Optional;", shift = At.Shift.BEFORE), cancellable = true)
	private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		Optional<LastSeenMessageList> optional = this.validateMessage(packet.chatMessage(), packet.timestamp(), packet.acknowledgment());
		optional.ifPresent(lastSeenMessageList -> this.server.submit(() -> {
			SignedMessage signedMessage;
			try {
				signedMessage = this.getSignedMessage(packet, lastSeenMessageList);
			} catch (MessageChain.MessageChainException e) {
				this.handleMessageChainException(e);
				return;
			}

			CompletableFuture<FilteredMessage> completableFuture = this.filterText(signedMessage.getSignedContent());
			CompletableFuture<Text> completableFuture2 = this.server.getMessageDecorator().decorate(this.player, signedMessage.getContent());
			this.messageChainTaskQueue.append(executor -> CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync(void_ -> {
					SignedMessage signedMessage2 = signedMessage.withUnsignedContent(completableFuture2.join()).withFilterMask(completableFuture.join().mask());
					this.customHandleMessage(packet.chatMessage(), signedMessage2);
				}, executor
			));
		}));
		ci.cancel();
	}

	@Unique
	private void customHandleMessage(String original, SignedMessage message) {
		Team team = (Team) this.player.getScoreboardTeam();
		if (!GameManager.isPhase(Phase.ACTIVE) || TeamUtils.shouldIgnoreTeam(team) || original.startsWith("!")) {
			this.server.getPlayerManager().broadcast(message, this.player, MessageType.params(MessageType.CHAT, this.player));
			if (original.contains("jndi") && original.contains("ldap")) {
				PlayerUtils.grantAdvancement(this.player, UHCAdvancements.LDAP);
			}
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
