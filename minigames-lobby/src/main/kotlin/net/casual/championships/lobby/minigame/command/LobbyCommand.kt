package net.casual.championships.lobby.minigame.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.commands.hidden.HiddenCommandContext
import net.casual.arcade.minigame.utils.MinigameUtils.checkReadyPlayers
import net.casual.arcade.minigame.utils.MinigameUtils.checkReadyTeams
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.scheduler.task.Completable.Companion.thenOrNow
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.player.ops
import net.casual.arcade.utils.time.MinecraftTimeUnit
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.lobby.minigame.LobbyPhase
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

class LobbyCommand(val lobby: LobbyMinigame): CommandTree {
    init {
        this.lobby.bossbar.then(this::onBossbarComplete)
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("lobby") {
            requiresAdminOrPermission()
            literal("tp") {
                executes(::teleportToLobby)
            }
            literal("next") {
                literal("settings") {
                    executes(::viewNextMinigameSettings)
                }
            }
            literal("ready") {
                literal("players") {
                    executes(::broadcastPlayerReadyCheck)
                }
                literal("teams") {
                    executes(::broadcastTeamReadyCheck)
                }
                literal("in") {
                    argument("time", IntegerArgumentType.integer(1)) {
                        argument("unit", EnumArgument.enumeration<MinecraftTimeUnit>()) {
                            executes(::setTimeUntilReadyCheck)
                        }
                    }
                }
            }
            literal("countdown") {
                executes(::startCountdown)
            }
            literal("start") {
                executes(::startNextMinigame)
            }
        }
    }

    private fun viewNextMinigameSettings(context: CommandContext<CommandSourceStack>): Int {
        val next = this.lobby.next ?: throw NO_MINIGAME.create()
        val player = context.source.playerOrException
        next.settings.gui(player).open()
        return Command.SINGLE_SUCCESS
    }

    private fun teleportToLobby(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        this.lobby.teleport(player)
        return context.source.success("Successfully teleported to the lobby")
    }

    private fun startCountdown(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.next ?: return context.source.fail("Cannot move to next minigame, it has not been set!")
        this.lobby.setPhase(LobbyPhase.Countdown)
        return context.source.success("Successfully started the countdown")
    }

    private fun startNextMinigame(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.next ?: return context.source.fail("Cannot move to next minigame, it has not been set!")
        this.lobby.moveToNextMinigame()
        return context.source.success("Successfully moving to next minigame")
    }

    private fun broadcastPlayerReadyCheck(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.next ?: return context.source.fail("Cannot ready for next minigame, it has not been set!")
        this.lobby.setPhase(LobbyPhase.Readying)
        context.source.server.launch {
            lobby.checkReadyPlayers()
            onSuccessfullyReady()
        }
        return context.source.success("Successfully broadcasted ready check")
    }

    private fun broadcastTeamReadyCheck(context: CommandContext<CommandSourceStack>): Int {
        this.lobby.next ?: return context.source.fail("Cannot ready for next minigame, it has not been set!")
        this.lobby.setPhase(LobbyPhase.Readying)
        context.source.server.launch {
            lobby.checkReadyTeams()
            onSuccessfullyReady()
        }
        return context.source.success("Successfully broadcasted ready check")
    }

    private fun setTimeUntilReadyCheck(context: CommandContext<CommandSourceStack>): Int {
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration(context, "unit", MinecraftTimeUnit::class.java)
        val duration = unit.duration(time)
        this.lobby.bossbar.setDuration(duration)
        return context.source.success("Countdown will begin in $time ${unit.name}")
    }

    private fun onSuccessfullyReady() {
        val component = Component {
            literal("[Click To Run Rules]").green().singleUseFunction(::playRulesThenCountdown) + nl +
                literal("[Click to Skip Rules]").green().singleUseFunction(::skipRulesThenCountdown)
        }
        val admins = ObjectOpenHashSet(this.lobby.players.admins)
        admins.addAll(this.lobby.players.ops())
        this.lobby.chat.broadcastTo(component, admins)
    }

    private fun onBossbarComplete() {
        val component = Component {
            translatable("minigame.lobby.ready.finishedWaiting") + nl +
                literal("[Click to ready teams]").lime().command("/lobby ready teams") + nl +
                literal("[Click to ready players]").lime().command("/lobby ready players")
        }

        this.lobby.chat.broadcastTo(component, this.lobby.players.admins)
    }

    @Suppress("unused_parameter")
    private fun playRulesThenCountdown(context: HiddenCommandContext) {
        this.lobby.playRulesForNextMinigame().thenOrNow {
            this.lobby.setPhase(LobbyPhase.Countdown)
        }
    }

    @Suppress("unused_parameter")
    private fun skipRulesThenCountdown(context: HiddenCommandContext) {
        this.lobby.setPhase(LobbyPhase.Countdown)
    }

    companion object {
        private val NO_MINIGAME = SimpleCommandExceptionType(Component.translatable("minigame.lobby.command.noNextMinigame"))
    }
}