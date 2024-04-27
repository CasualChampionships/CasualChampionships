package net.casual.championships.minigame

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.elements.GuiElement
import net.casual.arcade.events.minigame.LobbyMoveToNextMinigameEvent
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.gui.screen.SelectionGuiBuilder
import net.casual.arcade.gui.screen.SelectionGuiStyle
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.MinigameUtils.transferPlayersTo
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonScreens
import net.casual.championships.common.util.CommonUI.broadcastGame
import net.casual.championships.duel.DuelComponents
import net.casual.championships.duel.DuelRequester
import net.casual.championships.duel.DuelSettings
import net.casual.championships.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
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
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val player = event.player
        player.setTitleAnimation(stay = 5.Seconds)
        player.sendTitle(
            Component.empty().append(CommonComponents.Text.WELCOME_TO_CASUAL_CHAMPIONSHIPS.shadowless())
        )
        if (!this.players.isAdmin(player)) {
            player.setGameMode(GameType.ADVENTURE)
        } else if (Config.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        }

        val team = player.team
        event.spectating = team == null || this.teams.isTeamIgnored(team)
    }

    @Listener
    private fun onPlayerTeamJoin(event: PlayerTeamJoinEvent) {
        val (player, team) = event
        if (!this.teams.isTeamIgnored(team)) {
            this.players.setPlaying(player)
        } else {
            this.players.setSpectating(player)
        }
    }

    @Listener
    private fun onMoveToNextMinigame(event: LobbyMoveToNextMinigameEvent) {
        event.delay = 3.Seconds
    }

    private fun createDuelCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        val players = EntityArgument.players()
        return Commands.literal("duel").then(
            Commands.literal("with").then(
                Commands.argument("players", players).suggests { context, builder ->
                    val source = context.source.withPermission(2)
                    players.listSuggestions(context.copyFor(source), builder)
                }.executes(this::duelWith)
            )
        )
    }

    private fun duelWith(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val settings = DuelSettings()
        val builder = SelectionGuiBuilder(player, CommonScreens.named(DuelComponents.CONFIGURE_DUEL.mini()))
        builder.style = SelectionGuiStyle.centered(3)
        builder.element(GuiElement(Items.MAP.named(CommonComponents.CONFIGURE.mini())) { _, _, _, gui ->
            settings.gui(gui).open()
        })
        builder.element(GuiElement(MenuItem.TICK.named(CommonComponents.CONFIRM.mini())) { _, _, _, gui ->
            gui.close()
            val selector = context.getArgument("players", EntitySelector::class.java)
            val players = selector.findPlayers(context.source.withPermission(2))
            requestDuelWith(player, players, settings)
        })
        builder.element(GuiElement(MenuItem.CROSS.named(CommonComponents.CANCEL.mini())) { _, _, _, gui ->
            gui.close()
        })
        return builder.build().open().commandSuccess()
    }

    private fun requestDuelWith(
        initiator: ServerPlayer,
        players: Collection<ServerPlayer>,
        settings: DuelSettings
    ) {
        var started = false

        val duelers = HashSet(players)
        duelers.removeIf { !this.players.has(it) }
        duelers.add(initiator)

        val requesting = duelers.filter { it !== initiator }

        val requester = DuelRequester(initiator, duelers)
        if (requesting.isEmpty()) {
            requester.broadcastTo(DuelComponents.NOT_ENOUGH_PLAYERS.mini().red(), initiator)
            return
        }

        val unready = requester.arePlayersReady(requesting) {
            started = startDuelWith(started, initiator, duelers, setOf(), requester, settings, false)
        }
        val startAnyways = DuelComponents.START_NOW_MESSAGE.mini().green().function {
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
                requester.broadcastTo(DuelComponents.ALREADY_STARTED.mini().red(), initiator)
            }
            return true
        }
        val ready = HashSet(duelers)
        if (!this.players.isAdmin(initiator)) {
            ready.removeAll(unready.toSet())
        }
        ready.removeIf { !this.players.has(it) }

        if (ready.size <= 1) {
            requester.broadcastTo(DuelComponents.NOT_ENOUGH_PLAYERS.mini().red(), initiator)
            return false
        }

        val duel = CasualMinigames.event.createDuelMinigame(initiator.server, settings)
        this.transferPlayersTo(duel, ready)

        duel.chat.broadcastGame(DuelComponents.STARTING_DUEL.mini().green())
        duel.start()
        return true
    }
}