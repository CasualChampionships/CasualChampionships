package net.casual.championships.common.mixins.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.championships.common.util.CasualComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.block.ChestBlock$2$1")
public class ChestBlockMixin {
	@ModifyReturnValue(
		method = "getDisplayName()Lnet/minecraft/network/chat/Component;",
		at = @At("RETURN")
	)
	private Component onGetChestTitle(Component title) {
		return CasualComponents.Gui.createDoubleChestGui(title);
	}
}
