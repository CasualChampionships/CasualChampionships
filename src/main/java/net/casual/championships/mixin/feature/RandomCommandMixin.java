package net.casual.championships.mixin.feature;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.RandomCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomCommand.class)
public class RandomCommandMixin {
	@ModifyExpressionValue(
		method = "register",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/commands/Commands;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
			ordinal = 0
		)
	)
	private static LiteralArgumentBuilder<CommandSourceStack> onRegisterRandom(LiteralArgumentBuilder<CommandSourceStack> original) {
		return original.requires(s -> s.hasPermission(2));
	}
}
