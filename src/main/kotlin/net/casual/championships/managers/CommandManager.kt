package net.casual.championships.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.championships.commands.CasualCommand
import net.casual.championships.commands.MinesweeperCommand

object CommandManager {
    internal fun registerEvents() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> { (dispatcher, context) ->
            MinesweeperCommand.register(dispatcher, context)
            CasualCommand.register(dispatcher, context)
        }
    }
}