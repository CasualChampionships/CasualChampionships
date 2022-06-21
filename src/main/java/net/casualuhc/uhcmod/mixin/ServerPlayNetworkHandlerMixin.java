package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow @Final static Logger LOGGER;
	@Shadow public ServerPlayerEntity player;
	@Shadow @Final private MinecraftServer server;

	@Shadow protected abstract void checkForSpam();

	@Redirect(method = "handleMessage", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", remap = false), allow = 1)
	private CompletableFuture<Void> customHandleMessage(
			CompletableFuture<FilteredMessage<SignedMessage>> future,
			Consumer<? super FilteredMessage<SignedMessage>> action,
			Executor executor,
			ChatMessageC2SPacket packet
	) {
		Team team = (Team) this.player.getScoreboardTeam();
		boolean isGlobalChat = !GameManager.INSTANCE.isPhase(Phase.ACTIVE) || TeamUtils.isNonTeam(team) || packet.getChatMessage().startsWith("!");

		// do not apply async like vanilla, that's a race condition
		// also vanilla has a race condition because mojang doesn't know how to multithread
		return future.thenApply(message -> {
			if (!message.raw().verify(this.player)) {
				LOGGER.warn("{} sent message with invalid signature: '{}'", this.player.getName().getString(), message.raw().signedContent().getString());
				return null;
			}

			if (isGlobalChat) {
				this.server.getPlayerManager().broadcast(message, this.player, MessageType.CHAT);
			} else {
				PlayerUtils.forEveryPlayer(playerEntity -> {
					if (playerEntity == this.player) {
						playerEntity.sendMessage(Text.translatable(
								"chat.type.team.sent",
								team.getFormattedName(),
								playerEntity.getDisplayName(),
								message.raw().getContent()
						));
					} else if (team.isEqual(playerEntity.getScoreboardTeam())) {
						playerEntity.sendChatMessage(
								message.raw(),
								this.player.asMessageSender().withTeamName(team.getFormattedName()),
								MessageType.TEAM_MSG_COMMAND
						);
					}
				});
			}
			this.checkForSpam();
			return null;
		});
	}

	@Redirect(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
	private boolean canClick(ServerPlayerEntity instance) {
		return !(instance.currentScreenHandler instanceof GameSettings.FakeScreen) && instance.isSpectator();
	}

	@ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), argsOnly = true)
	private Packet<?> onSendPacket(Packet<?> packet) {
		if (packet instanceof EntityTrackerUpdateS2CPacket trackerUpdate) {
			Entity glowingEntity = this.player.getWorld().getEntityById(trackerUpdate.id());
			if (glowingEntity instanceof ServerPlayerEntity glowingPlayer) {
				packet = PlayerUtils.handleTrackerUpdatePacketForTeamGlowing(glowingPlayer, this.player, trackerUpdate);
			}
		}

		return packet;
	}
}
