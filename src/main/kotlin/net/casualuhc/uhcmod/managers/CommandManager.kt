package net.casualuhc.uhcmod.managers

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerRegisterCommandEvent
import net.casualuhc.uhcmod.commands.*

object CommandManager {
    internal fun registerEvents() {
        EventHandler.register<ServerRegisterCommandEvent> { event ->
            val dispatcher = event.dispatcher
            DisplayCommand.register(dispatcher)
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