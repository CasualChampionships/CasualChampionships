package net.casual.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.commands.*

object CommandManager {
    internal fun registerEvents() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> { (dispatcher, _) ->
            FullbrightCommand.register(dispatcher)
            LookCommand.register(dispatcher)
            MinesweeperCommand.register(dispatcher)
            PosCommand.register(dispatcher)
            SpectateCommand.register(dispatcher)
            TeamGlowCommand.register(dispatcher)
            CasualCommand.register(dispatcher)
        }
    }
}