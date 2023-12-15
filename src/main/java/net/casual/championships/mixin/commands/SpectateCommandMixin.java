package net.casual.championships.mixin.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.SpectateCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectateCommand.class)
public class SpectateCommandMixin {
	@Inject(
		method = "register",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void onRegisterSpectateCommand(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
		ci.cancel();
	}
}
