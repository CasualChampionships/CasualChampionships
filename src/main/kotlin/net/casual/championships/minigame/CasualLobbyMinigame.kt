package net.casual.championships.minigame

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenStyle
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.common.item.MenuItem
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonUI
import net.casual.championships.duel.DuelRequester
import net.casual.championships.duel.DuelSettings
import net.casual.championships.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType

class CasualLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame(server, lobby) {
    override val settings: MinigameSettings = CasualSettings(this)

    override fun initialize() {
        super.initialize()

        this.commands.register(this.createDuelCommand())

        this.ui.setPlayerListDisplay(CommonUI.createTabDisplay(this))
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val player = event.player
        player.setTitleAnimation(stay = 5.Seconds)
        player.sendTitle(
            Component.empty().append(CommonComponents.Bitmap.WELCOME_TO_CASUAL_CHAMPIONSHIPS.shadowless())
        )
        if (!this.isAdmin(player)) {
            player.setGameMode(GameType.ADVENTURE)
        } else if (Config.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        }

        val team = player.team
        if (team == null || this.teams.isTeamIgnored(team)) {
            GlobalTickedScheduler.later {
                this.makeSpectator(player)
            }
        } else {
            GlobalTickedScheduler.later {
                this.removeSpectator(player)
            }
        }
    }

    @Listener
    private fun onPlayerTeamJoin(event: PlayerTeamJoinEvent) {
        val (player, team) = event
        if (!this.teams.isTeamIgnored(team)) {
            this.removeSpectator(player)
        } else {
            this.makeSpectator(player)
        }
    }

    private fun createDuelCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("duel").then(
            Commands.literal("with").then(
                Commands.argument("players", EntityArgument.players()).executes(this::duelWith)
            )
        )
    }

    private fun duelWith(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val settings = DuelSettings()
        val menu = SelectionScreenBuilder().apply {
            style = SelectionScreenStyle.centered(3)
            selection(Items.MAP.named("Configure")) {
                it.openMenu(settings.menu(build()))
            }
            selection(MenuItem.TICK.named("Confirm")) {
                it.closeContainer()
                val players = EntityArgument.getPlayers( context,"players")
                requestDuelWith(player, players, settings)
            }
            selection(MenuItem.CROSS.named("Cancel")) {
                it.closeContainer()
            }
        }
        player.openMenu(menu.build())
        return Command.SINGLE_SUCCESS
    }

    private fun requestDuelWith(
        initiator: ServerPlayer,
        players: Collection<ServerPlayer>,
        settings: DuelSettings
    ) {
        var started = false

        val duelers = HashSet(players)
        duelers.removeIf { !this.hasPlayer(it) }
        duelers.add(initiator)

        val requesting = duelers.filter { it !== initiator }

        val requester = DuelRequester(initiator, duelers)
        if (requesting.isEmpty()) {
            requester.broadcastTo(NOT_ENOUGH_PLAYERS_FOR_DUEL, initiator)
            return
        }

        val unready = requester.arePlayersReady(requesting) {
            started = startDuelWith(started, initiator, duelers, setOf(), requester, settings, false)
        }
        val startAnyways = "Click here to start duel with accepted players".literal().green().function {
            started = startDuelWith(started, initiator, duelers, unready, requester, settings, true)
        }
        requester.broadcastTo(startAnyways, initiator)
    }

    private fun startDuelWith(
        started: Boolean,
        initiator: ServerPlayer,
        duelers: HashSet<ServerPlayer>,
        unready: Collection<ServerPlayer>,
        requester: DuelRequester,
        settings: DuelSettings,
        forced: Boolean
    ): Boolean {
        if (started) {
            if (forced) {
                requester.broadcastTo("Duel has already started!".literal().red(), initiator)
            }
            return true
        }
        val ready = HashSet(duelers)
        ready.removeAll(unready.toSet())
        ready.removeIf { !this.hasPlayer(it) }

        if (ready.size <= 1) {
            requester.broadcastTo(NOT_ENOUGH_PLAYERS_FOR_DUEL, initiator)
            return false
        }

        val duel = CasualMinigames.event.createDuelMinigame(initiator.server, settings)
        for (player in ready) {
            duel.addPlayer(player)
        }
        duel.start()
        return true
    }

    companion object {
        private val NOT_ENOUGH_PLAYERS_FOR_DUEL = "Not enough players for duel!".literal().red()
    }
}