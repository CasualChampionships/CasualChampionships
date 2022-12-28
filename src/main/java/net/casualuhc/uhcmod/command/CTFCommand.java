package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.gamesettings.GameSetting;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.screen.RuleScreen;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static net.casualuhc.uhcmod.managers.GameManager.*;
import static net.casualuhc.uhcmod.utils.scheduling.Scheduler.secondsToTicks;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CTFCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("ctf").requires(source -> source.hasPermissionLevel(2))
			.then(literal("setstart")
				.then(argument("time", StringArgumentType.greedyString())
					.executes(context -> {
						String time = context.getArgument("time", String.class);
						context.getSource().sendFeedback(
							Text.literal("Start starting time to: %s in UTC".formatted(time)), false
						);

						GameManager.setStartTime(LocalTime.parse(time).toEpochSecond(LocalDate.now(), ZoneOffset.UTC) * 1000);
						return 1;
					})
				)
			)
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
				.then(literal("spawnall")
					.executes(context -> {
						TeamManager.spawnAllPlayers();
						return 1;
					})
				)
				.then(literal("killall")
					.executes(context -> {
						TeamManager.killAllPlayers();
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
				.then(argument("first", TeamArgumentType.team())
					.then(literal("against")
						.then(argument("second", TeamArgumentType.team())
							.executes(context -> {
								Team first = TeamArgumentType.getTeam(context, "first");
								Team second = TeamArgumentType.getTeam(context, "second");
								GameManager.startCTF(first, second);
								return 1;
							})
						)
					)
				)
			)
			.then(literal("nextround")
				.executes(context -> {
					GameManager.resetCTF();
					return 1;
				})
			)
			// I got really lazy
			.then(literal("end")
				.then(argument("winner", TeamArgumentType.team())
					.executes(context -> {
						Team team = TeamArgumentType.getTeam(context, "winner");
						PlayerManager.forEveryPlayer(player -> {
							player.setGlowing(false);
							player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("uhc.game.wonRound", team.getName()).formatted(team.getColor())));
							if (
								(teamA != null && player.getScoreboardTeam() == teamA) ||
								(teamB != null && player.getScoreboardTeam() == teamB)
							) {
								Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
								player.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 90, 0);
							}
						});
						teamA = null;
						teamB = null;
						return 1;
					})
					.then(literal("finale")
						.executes(context -> {
							Team team = TeamArgumentType.getTeam(context, "winner");
							EventHandler.onEnd();
							PlayerManager.forEveryPlayer(player -> {
								if (player.getScoreboardTeam() == team) {
									PlayerManager.grantAdvancement(player, UHCAdvancements.WINNER);
								}
								player.setGlowing(false);
								player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("uhc.game.won", team.getName()).formatted(team.getColor())));
							});
							scheduleInLoopPhaseTask(0, 4, secondsToTicks(5), () -> {
								PlayerManager.forEveryPlayer(player -> {
									player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 0.5f, 1f);
									player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
									player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
								});
								schedulePhaseTask(6, () -> {
									PlayerManager.forEveryPlayer(player -> {
										player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.MASTER, 0.5f, 1f);
										player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 0.5f, 1f);
										player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundCategory.MASTER, 0.5f, 1f);
									});
								});
							});
							teamA = null;
							teamB = null;
							return 1;
						})
					)
				)
			)
			.then(literal("config")
				.executes(context -> {
					context.getSource().getPlayerOrThrow().openHandledScreen(RuleScreen.createScreenFactory(0));
					return 1;
				})
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
