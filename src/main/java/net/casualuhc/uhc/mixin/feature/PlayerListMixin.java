package net.casualuhc.uhc.mixin.feature;

import com.mojang.authlib.GameProfile;
import net.casualuhc.uhc.managers.UHCManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.LevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow public abstract boolean isOp(GameProfile profile);

	@Redirect(
		method = "placeNewPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/LevelData;isHardcore()Z"
		)
	)
	private boolean isHardcore(LevelData instance) {
		return true;
	}

	@Inject(
		method = "canPlayerLogin",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canPlayerJoin(SocketAddress socketAddress, GameProfile gameProfile, CallbackInfoReturnable<Component> cir) {
		if (!UHCManager.INSTANCE.isReadyForPlayers() && !this.isOp(gameProfile)) {
			cir.setReturnValue(Component.literal("UHC isn't quite ready yet..."));
		}
	}
}
