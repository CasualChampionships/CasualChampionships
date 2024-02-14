package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

interface Command {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {

    }

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, context: CommandBuildContext) {
        this.register(dispatcher)
    }
}