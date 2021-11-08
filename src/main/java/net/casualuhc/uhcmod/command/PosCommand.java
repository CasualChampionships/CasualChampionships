package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class PosCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> pos = literal("pos");
        pos.requires(player -> true).executes(PosCommand::sendPosition);

        dispatcher.register(pos);
    }

    public static int sendPosition(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        CommandManager manager = source.getMinecraftServer().getCommandManager();

        Entity entity = source.getEntityOrThrow();
        Vec3d pos = entity.getPos();

        AbstractTeam abstractTeam = entity.getScoreboardTeam();

        if (abstractTeam != null) {
            manager.execute(source, String.format("/tm I'm at %.0f, %.0f, %.0f", pos.x, pos.y, pos.z));
        }
        else {
            throw new SimpleCommandExceptionType(new LiteralText("§c[ERROR] You can only run this command if you are alive and in a team!")).create();
        }
        return 0;
    }
}
