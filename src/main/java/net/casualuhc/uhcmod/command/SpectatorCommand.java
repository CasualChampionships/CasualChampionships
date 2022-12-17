package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.utils.screen.SpectatorScreen;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpectatorCommand {
    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType(Text.translatable("uhc.spectator.notDead"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(literal("s").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (!player.isSpectator()) {
                throw NOT_SPECTATOR.create();
            }
            player.openHandledScreen(SpectatorScreen.createScreenFactory(0, false));
            return 1;
        }));
    }
}
