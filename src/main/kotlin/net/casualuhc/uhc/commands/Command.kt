package net.casualuhc.uhc.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack

interface Command {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>)
}