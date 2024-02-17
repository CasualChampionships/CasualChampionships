package net.casual.championships.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.championships.commands.*

object CommandManager {
    internal fun registerEvents() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> { (dispatcher, context) ->
            MinesweeperCommand.register(dispatcher, context)
            CasualCommand.register(dispatcher, context)
            DuelCommand.register(dispatcher, context)
        }
    }
}