package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class PosCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> pos = literal("pos");
        pos.requires(player -> true).executes(PosCommand::sendPosition);

        dispatcher.register(pos);
    }

    public static int sendPosition(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        CommandManager manager = source.getMinecraftServer().getCommandManager();

        Entity entity = source.getEntityOrThrow();
        Vec3d pos = entity.getPos();

        Team team = (Team) entity.getScoreboardTeam();

        if (team == null){
            manager.execute(source, String.format("/me is @ %.0f, %.0f, %.0f", pos.x, pos.y, pos.z));
        }else{
            manager.execute(source, String.format("/tm I'm @ %.0f, %.0f, %.0f", pos.x, pos.y, pos.z));
        }
        return 0;
    }
}
