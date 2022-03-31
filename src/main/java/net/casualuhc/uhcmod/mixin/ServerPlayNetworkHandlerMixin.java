package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.network.MessageType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;
import java.util.function.Function;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

	@Unique
	private String rawString;

	@Shadow
	public ServerPlayerEntity player;

	@ModifyVariable(method = "handleMessage", at = @At("STORE"), ordinal = 0)
	private String onGetRawString(String rawString) {
		this.rawString = rawString;
		return rawString;
	}

	@Redirect(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
	private void onBroadcastMessage(PlayerManager instance, Text serverMessage, Function<ServerPlayerEntity, Text> playerMessageFactory, MessageType type, UUID sender) {
		Team team = (Team) this.player.getScoreboardTeam();
		if (!GameManager.isPhase(Phase.ACTIVE) || TeamUtils.isNonTeam(team)) {
			instance.broadcast(serverMessage, playerMessageFactory, type, sender);
			return;
		}
		if (!this.rawString.startsWith("!")) {
			PlayerUtils.forEveryPlayer(playerEntity -> {
				if (team.isEqual(playerEntity.getScoreboardTeam())) {
					playerEntity.sendSystemMessage(new TranslatableText(
						"chat.type.team.text",
						team.getFormattedName().fillStyle(Style.EMPTY.withHoverEvent(
							new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.type.team.hover"))
						)),
						this.player.getDisplayName(), new LiteralText(this.rawString)), sender
					);
				}
			});
			return;
		}
		TranslatableText text = new TranslatableText("chat.type.text", this.player.getDisplayName(), this.rawString.substring(1));
		instance.broadcast(text, MessageType.CHAT, sender);
	}
}
