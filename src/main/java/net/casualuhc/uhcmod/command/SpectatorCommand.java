package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.screen.SpectatorScreen;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SpectatorCommand {
    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType(Text.translatable("uhc.spectator.notPlaying"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(literal("s").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (PlayerManager.isPlayerPlaying(player)) {
                throw NOT_SPECTATOR.create();
            }
            player.interactionManager.changeGameMode(GameMode.SPECTATOR);
            player.openHandledScreen(SpectatorScreen.createScreenFactory(0, false));
            return 1;
        }));
    }
}
