package net.casualuhc.uhcmod.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipeS2CPacketMixin {
	@Shadow @Final private List<Recipe<?>> recipes;

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void onWrite(PacketByteBuf buf, CallbackInfo ci) {
		buf.writeCollection(this.recipes.stream().filter(r -> r.getSerializer() != null).toList(), SynchronizeRecipesS2CPacket::writeRecipe);
		ci.cancel();
	}
}
