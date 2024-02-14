package net.casual.championships.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.championships.commands.*

object CommandManager {
    internal fun registerEvents() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> { (dispatcher, context) ->
            FullbrightCommand.register(dispatcher, context)
            LookCommand.register(dispatcher, context)
            MinesweeperCommand.register(dispatcher, context)
            PosCommand.register(dispatcher, context)
            SpectateCommand.register(dispatcher, context)
            TeamGlowCommand.register(dispatcher, context)
            CasualCommand.register(dispatcher, context)
            DuelCommand.register(dispatcher, context)
        }
    }
}