package net.casualuhc.uhcmod.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.KillCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KillCommand.class)
public class KillCommandMixin {
	@Redirect(method = "method_13432", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"))
	private static boolean canExecuteCommand(ServerCommandSource instance, int level) {
		return GameSettings.TESTING.getValue() || instance.hasPermissionLevel(level);
	}

	@Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;"))
	private static <T> RequiredArgumentBuilder<ServerCommandSource, T> onArgument(String name, ArgumentType<T> type) {
		RequiredArgumentBuilder<ServerCommandSource, T> argumentBuilder = CommandManager.argument(name, type);
		argumentBuilder.requires(s -> s.hasPermissionLevel(2));
		return argumentBuilder;
	}
}
