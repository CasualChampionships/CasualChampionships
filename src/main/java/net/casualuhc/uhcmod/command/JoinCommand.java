package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class JoinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> join = literal("join").
            requires(player -> true).
            then(argument("team", TeamArgumentType.team())
                    .executes(JoinCommand::joinTeam));

        dispatcher.register(join);
    }

    private static int joinTeam(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        CommandManager manager = source.getMinecraftServer().getCommandManager();
        ServerPlayerEntity player = source.getPlayer();
        Team team = TeamArgumentType.getTeam(context, "team");

        manager.execute(source.withLevel(2), String.format("/team join %s %s", team.getName(), player.getEntityName()));
        return 0;
    }
}
