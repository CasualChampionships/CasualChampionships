package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import eu.pb4.polymer.core.api.item.PolymerItem
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.putIntElement
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.championships.extensions.PlayerFlag
import net.casual.championships.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.championships.items.CasualItems
import net.casual.championships.managers.TeamManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.Config
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack

object CasualCommand: Command {
    private val NOT_CUSTOM_ITEM_ERROR = SimpleCommandExceptionType("Item was not custom!".literal())

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>, context: CommandBuildContext) {
        dispatcher.register(
            Commands.literal("casual").requiresAdminOrPermission().then(
                Commands.literal("team").then(
                    Commands.literal("reload").executes {
                        reloadTeams()
                    }
                )
            ).then(
                Commands.literal("player").then(
                    Commands.literal("modify").then(
                        Commands.argument("player", EntityArgument.player()).then(
                            Commands.literal("flags").then(
                                Commands.argument("flag", EnumArgument.enumeration<PlayerFlag>()).then(
                                    Commands.argument("value", BoolArgumentType.bool()).executes(this::setPlayerFlag)
                                )
                            )
                        )
                    )
                ).then(
                    Commands.literal("get").then(
                        Commands.argument("player", EntityArgument.player()).then(
                            Commands.literal("flags").executes(this::getPlayerFlags)
                        )
                    )
                )
            ).then(
                Commands.literal("config").then(
                    Commands.literal("reload").executes(this::reloadConfig)
                )
            ).then(
                Commands.literal("resources").then(
                    Commands.literal("regenerate").executes(this::regenerateResources)
                ).then(
                    Commands.literal("reload").executes(this::reloadResources)
                )
            ).then(
                Commands.literal("items").then(
                    Commands.argument("item", ItemArgument.item(context)).suggests { _, b ->
                        SharedSuggestionProvider.suggestResource(CasualItems.all().map { BuiltInRegistries.ITEM.getKey(it) }, b)
                    }.then(
                        Commands.argument("model", ResourceLocationArgument.id()).suggests { c, b ->
                            val item = ItemArgument.getItem(c, "item").item
                            if (item !is PolymerItem) {
                                throw NOT_CUSTOM_ITEM_ERROR.create()
                            }
                            SharedSuggestionProvider.suggestResource(CasualResourcePack.pack.getModelsFor(item.getPolymerReplacement(c.source.playerOrException)).map { it.modelPath() }, b)
                        }.executes(this::giveCustomItem)
                    )
                )
            ).then(
                Commands.literal("lobby").executes(this::returnToLobby)
            ).then(
                Commands.literal("floodgates").then(
                    Commands.literal("open").executes { this.floodgates(it, true) }
                ).then(
                    Commands.literal("close").executes { this.floodgates(it, false) }
                )
            )
        )
    }

    private fun reloadTeams(): Int {
        TeamManager.createTeams()
        return 1
    }

    private fun setPlayerFlag(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val flag = EnumArgument.getEnumeration<PlayerFlag>(context, "flag")
        val value = BoolArgumentType.getBool(context, "value")
        player.flags.set(flag, value)
        val message = "${flag.name} has been set to $value for ".literal().append(player.displayName!!)
        return context.source.success(message, true)
    }

    private fun getPlayerFlags(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val message = player.displayName!!.copy().append(" has the following flags enabled: ${player.flags.get().joinToString()}")
        return context.source.success(message)
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        Config.reload()
        return context.source.success("Successfully reloaded config", true)
    }

    private fun regenerateResources(context: CommandContext<CommandSourceStack>): Int {
        if (CasualResourcePack.generate()) {
            return context.source.success("Successfully regenerated resources, reload resources to refresh clients")
        }
        return context.source.fail("Failed to regenerate resources...")
    }

    private fun reloadResources(context: CommandContext<CommandSourceStack>): Int {
        CasualResourcePackHost.reload().thenAcceptAsync({
            if (it) {
                context.source.success("Successfully reloaded resources, resending pack...")
                CasualMinigames.minigame.getResources().sendTo(CasualMinigames.minigame.getAllPlayers())
            } else {
                context.source.fail("Failed to reload resources...")
            }
        }, context.source.server)
        return context.source.success("Reloading resources...")
    }

    private fun giveCustomItem(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val item = ItemArgument.getItem(context, "item").item
        if (item !is PolymerItem) {
            throw NOT_CUSTOM_ITEM_ERROR.create()
        }

        val model = ResourceLocationArgument.getId(context, "model")
        val data = CasualResourcePack.pack.requestModel(item.getPolymerReplacement(player), model)
        val stack = ItemStack(item)
        stack.putIntElement("arcade_packed_custom_model", data.value())
        player.inventory.add(stack)
        return context.source.success("Successfully given custom item $item")
    }

    private fun returnToLobby(context: CommandContext<CommandSourceStack>): Int {
        CasualMinigames.event.returnToLobby(context.source.server)
        return context.source.success("Returning to lobby...")
    }

    private fun floodgates(context: CommandContext<CommandSourceStack>, open: Boolean): Int {
        CasualMinigames.floodgates = open
        return context.source.success("Successfully ${if (open) "opened" else "closed"} the floodgates")
    }
}