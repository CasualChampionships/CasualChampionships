package net.casual.championships.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.coroutine.launch
import net.casual.championships.CasualChampionships
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

object CasualCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("casual") {
            requiresAdminOrPermission()
            literal("reload") {
                executes(::reloadAll)
                literal("teams") {
                    executes(::reloadTeams)
                    literal("force") {
                        executes(::createTeams)
                    }
                }
                literal("resources") {
                    executes(::reloadResources)
                }
                literal("minigame") {
                    executes(::reloadMinigame)
                }
            }
            literal("lobby") {
                executes(::returnToLobby)
            }
            literal("floodgates") {
                literal("open") {
                    executes { floodgates(it, true) }
                }
                literal("close") {
                    executes { floodgates(it, false) }
                }
            }
        }
    }

    private fun reloadAll(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.reload(context.source.server)
        return context.source.success("Successfully reloaded config", true)
    }

    private fun reloadTeams(context: CommandContext<CommandSourceStack>): Int {
        val server = context.source.server
        context.source.success("Reloading teams...")
        server.launch {
            CasualChampionships.minigames.reloadTeams()
            context.source.success("Successfully reloaded teams")
        }
        return Command.SINGLE_SUCCESS
    }

    private fun createTeams(context: CommandContext<CommandSourceStack>): Int {
        val server = context.source.server
        context.source.success("Creating teams...")
        server.launch {
            CasualChampionships.minigames.createTeams()
            context.source.success("Successfully created teams")
        }
        return Command.SINGLE_SUCCESS
    }

    private fun reloadResources(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.reloadPlayerResources()
        return context.source.success("Reloading resources...")
    }

    private fun reloadMinigame(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.reloadMinigame()
        return context.source.success("Successfully reloaded minigame")
    }

    private fun returnToLobby(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.returnToLobby()
        return context.source.success("Successfully returned to lobby")
    }

    private fun floodgates(context: CommandContext<CommandSourceStack>, open: Boolean): Int {
        CasualChampionships.minigames.floodgates = open
        return context.source.success("Successfully ${if (open) "opened" else "closed"} the floodgates", true)
    }
}