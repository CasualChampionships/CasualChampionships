package net.casualuhc.uhc.managers

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerRegisterCommandEvent
import net.casualuhc.uhc.commands.*

object CommandManager {
    internal fun registerEvents() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> { (dispatcher, _) ->
            FullbrightCommand.register(dispatcher)
            LookCommand.register(dispatcher)
            MinesweeperCommand.register(dispatcher)
            PosCommand.register(dispatcher)
            ReadyCommand.register(dispatcher)
            SpectateCommand.register(dispatcher)
            TeamGlowCommand.register(dispatcher)
            UHCCommand.register(dispatcher)
        }
    }
}