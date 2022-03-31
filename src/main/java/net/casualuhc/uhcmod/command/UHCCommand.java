package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.managers.WorldBoarderManager;
import net.casualuhc.uhcmod.utils.GameSetting.GameSetting;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UHCCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("uhc").requires(source -> source.hasPermissionLevel(4) || source.getName().equals("senseiwells"))
			.then(literal("team")
				.then(literal("reload")
					.executes(context -> {
						TeamManager.createTeams();
						return 1;
					})
				)
				.then(literal("forceadd")
					.then(argument("player", EntityArgumentType.entity())
						.then(argument("team", TeamArgumentType.team())
							.executes(TeamUtils::forceAddPlayer)
						)
					)
				)
				.then(literal("download")
					.executes(context -> {
						UHCDataBase.INSTANCE.downloadTeamFromDataBase();
						return 1;
					})
				)
			)
			.then(literal("phase")
				.then(literal("cancel")
					.executes(context -> {
						GameManager.setCurrentPhase(Phase.LOBBY);
						return 1;
					})
				)
				.then(literal("current")
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						player.sendMessage(new LiteralText("Current phase is: %d".formatted(GameManager.getCurrentPhase().phaseNumber())), false);
						return 1;
					})
				)
			)
			.then(literal("setup")
				.executes(context -> {
					Phase.SETUP.run();
					return 1;
				})
			)
			.then(literal("lobby")
				.executes(context -> {
					Phase.LOBBY.run();
					return 1;
				})
			)
			.then(literal("start")
				.executes(context -> {
					Phase.READY.run();
					return 1;
				})
				.then(literal("force")
					.executes(context -> {
						Phase.START.run();
						return 1;
					})
				)
				.then(literal("quiet")
					.executes(context -> {
						GameManager.setCurrentPhase(Phase.ACTIVE);
						WorldBoarderManager.startWorldBorders(Phase.getPhaseThreadGroup(), true);
						GameManager.setUHCGamerules();
						return 1;
					})
				)
			)
			.then(literal("endgame")
				.executes(context -> {
					Phase.END.run();
					return 1;
				})
			)
			.then(literal("config")
				.then(literal("worldborder")
					.then(getWorldBorderStagesStart())
					.then(getWorldBorderStagesEnd())
					.then(literal("stop")
						.executes(context -> {
							if (!GameManager.isPhase(Phase.ACTIVE)) {
								throw CANNOT_MODIFY_WB;
							}
							Phase.getPhaseThreadGroup().interrupt();
							context.getSource().getServer().getWorlds().forEach(serverWorld -> serverWorld.getWorldBorder().setSize(serverWorld.getWorldBorder().getSize()));
							context.getSource().sendFeedback(new LiteralText("Border stopped"), false);
							return 1;
						})
					)
				)
				.then(literal("pvp")
					.then(literal("true").executes(context -> {
						context.getSource().getServer().setPvpEnabled(true);
						return 1;
					}))
					.then(literal("false").executes(context -> {
						context.getSource().getServer().setPvpEnabled(false);
						return 1;
					}))
				)
				.then(literal("tab")
					.then(literal("toggle").executes(context -> {
						PlayerUtils.displayTab = !PlayerUtils.displayTab;
						return 1;
					}))
				)
				.then(literal("floodgate")
					.then(literal("open")
						.executes(context -> changeOpJoin(context.getSource(), true))
					)
					.then(literal("close")
						.executes(context -> changeOpJoin(context.getSource(), false))
					)
				)
				.then(literal("removeglow")
					.executes(context -> {
						PlayerUtils.forEveryPlayer(player -> player.setGlowing(false));
						return 1;
					})
				)
				.then(literal("sethealth")
					.then(argument("player", EntityArgumentType.player())
						.executes(context -> {
							ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
							player.getHungerManager().setSaturationLevel(20F);
							EntityAttributeInstance instance = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
							if (instance != null) {
								instance.removeModifier(GameManager.HEALTH_BOOST);
								instance.addPersistentModifier(new EntityAttributeModifier(GameManager.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
							}
							player.setHealth(player.getMaxHealth());
							return 1;
						})
					)
				)
				.then(getGameRuleCommand())
			)
		);
	}

	private static int changeOpJoin(ServerCommandSource source, boolean enable) {
		GameManager.nonOpJoin = enable;
		source.sendFeedback(new LiteralText("The floodgates are %s".formatted(enable ? "open" : "closed")), false);
		return 1;
	}

	private static final CommandSyntaxException CANNOT_MODIFY_WB = new SimpleCommandExceptionType(new LiteralText("Cannot change world border now")).create();

	private static LiteralArgumentBuilder<ServerCommandSource> getWorldBorderStagesStart() {
		LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("forcestart");
		for (WorldBoarderManager.Stage stage : WorldBoarderManager.Stage.values()) {
			commandBuilder.then(literal(stage.name())
				.executes(context -> {
					if (GameManager.isPhase(Phase.ACTIVE)) {
						Phase.getPhaseThreadGroup().interrupt();
						WorldBoarderManager.moveWorldBorders(stage.getStartSize(), 0);
						WorldBoarderManager.startWorldBorders(Phase.getPhaseThreadGroup(), true);
						return 1;
					}
					throw CANNOT_MODIFY_WB;
				})
			);
		}
		return commandBuilder;
	}

	private static LiteralArgumentBuilder<ServerCommandSource> getWorldBorderStagesEnd() {
		LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("forceend");
		for (WorldBoarderManager.Stage stage : WorldBoarderManager.Stage.values()) {
			commandBuilder.then(literal(stage.name())
				.executes(context -> {
					if (GameManager.isPhase(Phase.ACTIVE)) {
						Phase.getPhaseThreadGroup().interrupt();
						WorldBoarderManager.moveWorldBorders(stage.getEndSize(), 0);
						WorldBoarderManager.startWorldBorders(Phase.getPhaseThreadGroup(), true);
						return 1;
					}
					throw CANNOT_MODIFY_WB;
				})
			);
		}
		return commandBuilder;
	}

	private static LiteralArgumentBuilder<ServerCommandSource> getGameRuleCommand() {
		LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("gamerule");
		for (Map.Entry<String, GameSetting<?>> gameSettingEntry : GameSettings.gameSettingMap.entrySet()) {
			String settingName = gameSettingEntry.getKey();
			LiteralArgumentBuilder<ServerCommandSource> commandArgument = literal(settingName);
			for (String argument : gameSettingEntry.getValue().getOptions().keySet()) {
				commandArgument.then(literal(argument).executes(context -> {
					ServerPlayerEntity playerEntity = context.getSource().getPlayer();
					gameSettingEntry.getValue().setValueFromOption(argument);
					playerEntity.sendMessage(new LiteralText("Set %s for %s".formatted(argument, settingName)).formatted(Formatting.GREEN), false);
					return 1;
				}));
			}
			commandBuilder.then(commandArgument);
		}
		return commandBuilder;
	}
}
