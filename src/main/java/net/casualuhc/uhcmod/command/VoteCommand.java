package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.GameSetting.GameSetting;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

public class VoteCommand {
    public static final CommandSyntaxException CANNOT_VOTE_NOW = new SimpleCommandExceptionType(new LiteralText("You cannot vote now")).create();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("vote");
        LiteralArgumentBuilder<ServerCommandSource> commandInfo = literal("info");
        for (Map.Entry<String, GameSetting<?>> gameSettingEntry : GameSettings.gameSettingMap.entrySet()) {
            String settingName = gameSettingEntry.getKey();
            LiteralArgumentBuilder<ServerCommandSource> commandArgument = literal(settingName);
            for (String argument : gameSettingEntry.getValue().getOptions().keySet()) {
                commandArgument.then(literal(argument).executes(context -> {
                    ServerPlayerEntity playerEntity = context.getSource().getPlayer();
                    if (!GameManager.isPhase(Phase.VOTING) || TeamUtils.isNonTeam(playerEntity.getScoreboardTeam())) {
                        throw CANNOT_VOTE_NOW;
                    }
                    ServerPlayerMixinInterface IPlayer = (ServerPlayerMixinInterface) playerEntity;
                    IPlayer.getVoteManager().setVote(settingName, argument);
                    playerEntity.sendMessage(new LiteralText("You voted %s for %s".formatted(argument, settingName)).formatted(Formatting.GREEN), false);
                    return 1;
                }));
            }
            commandInfo.then(literal(settingName).executes(context -> {
                context.getSource().getPlayer().sendMessage(new LiteralText("%s: %s".formatted(settingName, gameSettingEntry.getValue().getDescription())), false);
                return 1;
            }));
            commandBuilder.then(commandArgument);
        }
        commandBuilder.then(commandInfo);
        dispatcher.register(commandBuilder);
    }
}
