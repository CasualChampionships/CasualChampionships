package net.casual.championships.common.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.championships.common.util.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("MixinAnnotationTarget")
@Mixin(targets = "net.minecraft.world.level.block.ChestBlock$2$1")
public class ChestBlockMixin {
	@SuppressWarnings("UnresolvedMixinReference")
	@ModifyReturnValue(
		method = "getDisplayName*",
		at = @At("RETURN")
	)
	private Component onGetChestTitle(Component title) {
		return CommonComponents.Gui.createDoubleChestGui(title);
	}
}
