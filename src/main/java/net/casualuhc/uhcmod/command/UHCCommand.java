package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.casualuhc.uhcmod.utils.Event.Events;
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
						GameManager.INSTANCE.setCurrentPhase(Phase.LOBBY);
						return 1;
					})
				)
				.then(literal("current")
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						player.sendMessage(new LiteralText("Current phase is: %d".formatted(GameManager.INSTANCE.getCurrentPhase().ordinal())), false);
						return 1;
					})
				)
			)
			.then(literal("setup")
				.executes(context -> {
					Events.ON_SETUP.trigger();
					return 1;
				})
			)
			.then(literal("lobby")
				.executes(context -> {
					Events.ON_LOBBY.trigger();
					return 1;
				})
			)
			.then(literal("start")
				.executes(context -> {
					Events.ON_READY.trigger();
					return 1;
				})
				.then(literal("force")
					.executes(context -> {
						Events.ON_START.trigger();
						return 1;
					})
				)
				.then(literal("quiet")
					.executes(context -> {
						GameManager.INSTANCE.setCurrentPhase(Phase.ACTIVE);
						Events.GRACE_PERIOD_FINISH.trigger();
						GameManager.INSTANCE.setUHCGamerules();
						return 1;
					})
				)
			)
			.then(literal("endgame")
				.executes(context -> {
					Events.ON_END.trigger();
					return 1;
				})
			)
			.then(literal("config")
				.executes(context -> {
					context.getSource().getPlayer().openHandledScreen(GameSettings.createScreenFactory(0));
					return 1;
				})
				.then(literal("worldborder")
					.then(getWorldBorderStagesStart())
					.then(getWorldBorderStagesEnd())
					.then(literal("stop")
						.executes(context -> {
							if (!GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
								throw CANNOT_MODIFY_WB;
							}
							context.getSource().getServer().getWorlds().forEach(serverWorld -> serverWorld.getWorldBorder().setSize(serverWorld.getWorldBorder().getSize()));
							context.getSource().sendFeedback(new LiteralText("Border stopped"), false);
							return 1;
						})
					)
				)
				.then(literal("pvp")
					.then(literal("true").executes(context -> {
						GameSettings.PVP.setValue(true);
						return 1;
					}))
					.then(literal("false").executes(context -> {
						GameSettings.PVP.setValue(false);
						return 1;
					}))
				)
				.then(literal("tab")
					.then(literal("toggle").executes(context -> {
						GameSettings.DISPLAY_TAB.setValue(!GameSettings.DISPLAY_TAB.getValue());
						return 1;
					}))
				)
				.then(literal("floodgate")
					.then(literal("open")
						.executes(context -> {
							GameSettings.FLOODGATE.setValue(true);
							return 1;
						})
					)
					.then(literal("close")
						.executes(context -> {
							GameSettings.FLOODGATE.setValue(false);
							return 1;
						})
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
								instance.removeModifier(PlayerUtils.HEALTH_BOOST);
								instance.addPersistentModifier(new EntityAttributeModifier(PlayerUtils.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
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

	private static final CommandSyntaxException CANNOT_MODIFY_WB = new SimpleCommandExceptionType(new LiteralText("Cannot change world border now")).create();

	private static LiteralArgumentBuilder<ServerCommandSource> getWorldBorderStagesStart() {
		LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("forcestart");
		for (WorldBorderManager.Stage stage : WorldBorderManager.Stage.values()) {
			commandBuilder.then(literal(stage.name())
				.executes(context -> {
					if (GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
						WorldBorderManager.moveWorldBorders(stage.getStartSize(), 0);
						WorldBorderManager.startWorldBorders();
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
		for (WorldBorderManager.Stage stage : WorldBorderManager.Stage.values()) {
			commandBuilder.then(literal(stage.name())
				.executes(context -> {
					if (GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
						WorldBorderManager.moveWorldBorders(stage.getEndSize(), 0);
						WorldBorderManager.startWorldBorders();
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
			for (GameSetting.NamedItemStack argument : gameSettingEntry.getValue().getOptions().keySet()) {
				commandArgument.then(literal(argument.name).executes(context -> {
					ServerPlayerEntity playerEntity = context.getSource().getPlayer();
					gameSettingEntry.getValue().setValueFromOption(argument.name);
					playerEntity.sendMessage(new LiteralText("Set %s for %s".formatted(argument, settingName)).formatted(Formatting.GREEN), false);
					return 1;
				}));
			}
			commandBuilder.then(commandArgument);
		}
		return commandBuilder;
	}
}
