package net.casualuhc.uhcmod.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casualuhc.uhcmod.extensions.PlayerFlag
import net.casualuhc.uhcmod.extensions.PlayerFlag.*
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object DisplayCommand: Command {
    private val displayFlags = listOf(Coords, Facing, Distance, Radius)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val display = Commands.literal("display")
        for (flag in this.displayFlags) {
            display.then(Commands.literal(flag.name).executes {
                this.toggle(it, flag)
            })
        }
        display.executes {
            this.toggle(it)
        }
        display.then(Commands.literal("enable").executes {
            this.toggle(it, true)
        })
        display.then(Commands.literal("disable").executes {
            this.toggle(it, false)
        })
        // Legacy command -> /coords
        dispatcher.register(Commands.literal("coords").redirect(dispatcher.register(display)))
    }

    private fun toggle(context: CommandContext<CommandSourceStack>, flag: PlayerFlag): Int {
        val player = context.source.playerOrException
        val toggle = when (flag) {
            Coords -> Texts.UHC_DISPLAY_COORDS
            Distance -> Texts.UHC_DISPLAY_DISTANCE
            Facing -> Texts.UHC_DISPLAY_DIRECTION
            Radius -> Texts.UHC_DISPLAY_RADIUS
            else -> return 0
        }
        player.flags.toggle(flag)
        player.sendSystemMessage(toggle.generate(player.flags.has(flag)))
        return 1
    }

    private fun toggle(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        for (flag in this.displayFlags) {
            if (player.flags.has(flag)) {
                return this.toggle(context, false)
            }
        }
        return this.toggle(context, true)
    }

    private fun toggle(context: CommandContext<CommandSourceStack>, enabled: Boolean): Int {
        val player = context.source.playerOrException
        for (flag in this.displayFlags) {
            player.flags.set(flag, enabled)
        }
        player.sendSystemMessage(Texts.UHC_DISPLAY.generate(enabled))
        return 1
    }
}