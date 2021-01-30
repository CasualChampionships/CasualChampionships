package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.event.ChatMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;
import static net.casualuhc.uhcmod.event.ChatMode.Mode;

public class ChatCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> chat = literal("chat").
                requires(player -> true);
        for(Mode mode: Mode.values()){
            chat.then(literal(mode.toString()).executes( context -> changeMode(context, mode)));
        }

        dispatcher.register(chat);
    }

    private static int changeMode (CommandContext<ServerCommandSource> context, Mode mode) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity playerEntity = source.getPlayer();
        ChatMode.setMode(playerEntity, mode);
        return 0;
    }
}
