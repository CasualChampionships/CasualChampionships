package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.casualuhc.uhcmod.utils.uhc.UHCUtils;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.gamesettings.GameSetting;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UHCCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("uhc").requires(source -> source.hasPermissionLevel(4))
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
							.executes(TeamManager::forceAddPlayer)
						)
					)
				)
				.then(literal("download")
					.executes(context -> {
						UHCDataBase.downloadTeamFromDataBase();
						return 1;
					})
				)
			)
			.then(literal("phase")
				.then(literal("cancel")
					.executes(context -> {
						GameManager.setPhase(Phase.LOBBY);
						return 1;
					})
				)
				.then(literal("current")
					.executes(context -> {
						context.getSource().sendFeedback(
							Text.literal("Current phase is: %d".formatted(GameManager.getPhase().ordinal())), false
						);
						return 1;
					})
				)
			)
			.then(literal("setup")
				.executes(context -> {
					EventHandler.onSetup();
					return 1;
				})
			)
			.then(literal("lobby")
				.executes(context -> {
					EventHandler.onLobby();
					return 1;
				})
			)
			.then(literal("start")
				.executes(context -> {
					EventHandler.onReady();
					return 1;
				})
				.then(literal("force")
					.executes(context -> {
						EventHandler.onStart();
						return 1;
					})
				)
				.then(literal("quiet")
					.executes(context -> {
						GameManager.setPhase(Phase.ACTIVE);
						EventHandler.onGracePeriodEnd();
						UHCUtils.setUHCGamerules();
						return 1;
					})
				)
			)
			.then(literal("endgame")
				.executes(context -> {
					EventHandler.onEnd();
					return 1;
				})
			)
			.then(literal("config")
				.executes(context -> {
					context.getSource().getPlayerOrThrow().openHandledScreen(GameSettings.createScreenFactory(0));
					return 1;
				})
				.then(literal("worldborder")
					.then(getWorldBorderStagesStart())
					.then(getWorldBorderStagesEnd())
					.then(literal("stop")
						.executes(context -> {
							if (!GameManager.isPhase(Phase.ACTIVE)) {
								throw CANNOT_MODIFY_WB;
							}
							context.getSource().getServer().getWorlds().forEach(serverWorld -> serverWorld.getWorldBorder().setSize(serverWorld.getWorldBorder().getSize()));
							context.getSource().sendFeedback(Text.literal("Border Stopped"), false);
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
						PlayerManager.forEveryPlayer(player -> player.setGlowing(false));
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
								instance.removeModifier(PlayerManager.HEALTH_BOOST);
								instance.addPersistentModifier(new EntityAttributeModifier(PlayerManager.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
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

	private static final CommandSyntaxException CANNOT_MODIFY_WB = new SimpleCommandExceptionType(Text.literal("Cannot change world border now")).create();

	private static LiteralArgumentBuilder<ServerCommandSource> getWorldBorderStagesStart() {
		LiteralArgumentBuilder<ServerCommandSource> commandBuilder = literal("forcestart");
		for (WorldBorderManager.Stage stage : WorldBorderManager.Stage.values()) {
			commandBuilder.then(literal(stage.name())
				.executes(context -> {
					if (GameManager.isPhase(Phase.ACTIVE)) {
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
					if (GameManager.isPhase(Phase.ACTIVE)) {
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

		for (GameSetting<?> setting : GameSettings.RULES.values()) {
			String settingName = setting.getName().replaceAll(" ", "_").toLowerCase();
			LiteralArgumentBuilder<ServerCommandSource> commandArgument = literal(settingName);
			for (ItemStack optionStack : setting.getOptions().keySet()) {
				String optionName = optionStack.getName().getString().replaceAll(" ", "_").toLowerCase();
				commandArgument.then(literal(optionName).executes(context -> {
					setting.setValueFromOption(optionStack);
					context.getSource().sendFeedback(
						Text.literal("Set %s for %s".formatted(optionName, settingName)).formatted(Formatting.GREEN), false
					);
					return 1;
				}));
			}
			commandBuilder.then(commandArgument);
		}
		return commandBuilder;
	}
}
