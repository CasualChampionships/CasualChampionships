package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class CoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("coords").requires((source) -> {
            return source.hasPermissionLevel(0);
        })).executes(CoordsCommand::run));
    }

    private static int run(CommandContext context) {
        Entity entity = ((ServerCommandSource) context.getSource()).getEntity();
        if (entity != null) {
            if (entity instanceof ServerPlayerEntity) {
                ((ServerPlayerMixinInterface) entity).setCoordsBoolean(!((ServerPlayerMixinInterface) entity).getCoordsBoolean());
            }
        }
        return 1;
    }
}