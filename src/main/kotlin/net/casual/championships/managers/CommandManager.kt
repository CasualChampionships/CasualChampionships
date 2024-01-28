package net.casual.championships.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.championships.commands.*

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
            DuelCommand.register(dispatcher)
        }
    }
}