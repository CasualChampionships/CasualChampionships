package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.literal;

public class PosCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pos").requires(source -> source.getEntity() != null).executes(PosCommand::sendPosition));
    }

    public static int sendPosition(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        CommandManager manager = source.getServer().getCommandManager();

        Entity entity = source.getEntityOrThrow();
        Vec3d pos = entity.getPos();

        AbstractTeam abstractTeam = entity.getScoreboardTeam();

        if (abstractTeam != null) {
            manager.execute(source, String.format("tm I'm at %.0f, %.0f, %.0f", pos.x, pos.y, pos.z));
        }
        else {
            throw new SimpleCommandExceptionType(Text.literal("§c[ERROR] You can only run this command if you are alive and in a team!")).create();
        }
        return 0;
    }
}
