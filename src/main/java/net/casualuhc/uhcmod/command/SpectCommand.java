package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.utils.Spectator;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SpectCommand {

    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType(new TranslatableText("You need to be dead to spectate!"));

    // TODO This is bad because the command tree is never sent to the player
    // Client won't register this as a valid command unless they join in survival or get OP and vise versa
    // Either mixin to when player gets put in/out of spectator and send command tree there
    // Or just make this command available to everyone but throw when player is not in spectator

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> s = literal("s")
            .requires(SpectCommand::canSpectate)
            .executes(SpectCommand::spectateTeammate)
            .then(argument("player", EntityArgumentType.player())
                .executes(SpectCommand::spectatePlayer)
            );

        LiteralArgumentBuilder<ServerCommandSource> c = literal("c")
            .requires(SpectCommand::canSpectate)
            .executes(SpectCommand::spectateCameraman);

        dispatcher.register(s);
        dispatcher.register(c);
    }

    private static boolean canSpectate(ServerCommandSource source) {
        try {
            return source.getPlayer().isSpectator();
        }
        catch (CommandSyntaxException ignored) {
            return false;
        }
    }

    private static int spectatePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (!player.isSpectator()) {
            throw NOT_SPECTATOR.create();
        }

        ServerPlayerEntity entity = EntityArgumentType.getPlayer(context, "player");
        Spectator.spectate(player, entity);
        return 0;
    }

    private static int spectateTeammate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (!player.isSpectator()) {
            throw NOT_SPECTATOR.create();
        }

        Team team = (Team) player.getScoreboardTeam();
        spectate(player, team);
        return 0;
    }

    private static int spectateCameraman(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (!player.isSpectator()) {
            throw NOT_SPECTATOR.create();
        }

        Spectator.spectate(player);
        return 0;
    }

    private static void spectate(ServerPlayerEntity player, Team team){
        if (team == null) {
            Spectator.spectate(player);
        }
        else {
            Spectator.spectate(player, team);
        }
    }
}
