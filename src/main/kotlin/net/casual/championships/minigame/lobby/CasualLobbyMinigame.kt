package net.casual.championships.minigame.lobby

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.elements.GuiElement
import net.casual.arcade.chat.ChatFormatter
import net.casual.arcade.events.minigame.LobbyMoveToNextMinigameEvent
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.screen.SelectionGuiBuilder
import net.casual.arcade.gui.screen.SelectionGuiStyle
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.impl.PlayerTask
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.MinigameUtils.transferPlayersTo
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.ResourcePackUtils.afterPacksLoad
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.minigame.rule.RulesProvider
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonScreens
import net.casual.championships.common.util.CommonSounds
import net.casual.championships.common.util.CommonUI.broadcastGame
import net.casual.championships.common.util.CommonUI.broadcastWithSound
import net.casual.championships.duel.DuelComponents
import net.casual.championships.duel.DuelRequester
import net.casual.championships.duel.DuelSettings
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType

class CasualLobbyMinigame(
    server: MinecraftServer,
    private val casualLobby: CasualLobby
): LobbyMinigame(server, casualLobby) {
    override val settings: MinigameSettings = CasualSettings(this)

    private var shouldWelcomePlayers = true

    override fun initialize() {
        super.initialize()

        this.commands.register(this.createDuelCommand())
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val player = event.player
        if (this.shouldWelcomePlayers) {
            player.setTitleAnimation(stay = 5.Seconds)
            player.sendTitle(
                Component.empty().append(CommonComponents.Text.WELCOME_TO_CASUAL_CHAMPIONSHIPS.shadowless())
            )
        }

        if (!this.players.isAdmin(player)) {
            player.setGameMode(GameType.ADVENTURE)
        } else if (Config.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        }

        val team = player.team
        event.spectating = team == null || this.teams.isTeamIgnored(team)

        if (CasualMinigames.hasWinner()) {
            player.afterPacksLoad {
                player.sendSound(CommonSounds.GAME_WON)
                this.scheduler.schedule(10.Seconds, PlayerTask(player) {
                    this.casualLobby.spawnFireworksFor(player, this.scheduler)
                })
            }
        }
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
        this.shouldWelcomePlayers = false
    }

    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        if (this.bossbar.getRemainingDuration() == 25.Seconds) {
            for (player in this.players) {
                player.sendSound(CommonSounds.WAITING, SoundSource.MASTER)
            }
        }
    }

    override fun startNextMinigame() {
        val minigame = this.getNextMinigame()
        if (minigame !is RulesProvider) {
            super.startNextMinigame()
            return
        }

        this.settings.isChatMuted.set(true)
        val rules = minigame.getRules()
        var delay = MinecraftTimeDuration.ZERO
        for (rule in rules) {
            for (entry in rule.entries) {
                val formatter = ChatFormatter.createAnnouncement(rule.title)
                this.scheduler.schedulePhased(delay) {
                    val message = entry.lines.fold(Component.empty()) { a, b -> a.append("\n\n").append(b) }
                    this.chat.broadcastWithSound(message, formatter = formatter)
                }
                delay += entry.duration
            }
        }
        this.scheduler.schedulePhased(delay) {
            super.startNextMinigame()
        }
        this.scheduler.schedulePhasedCancellable(delay) {
            this.settings.isChatMuted.set(false)
        }.runIfCancelled()
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

        val duel = CasualMinigames.createDuelMinigame(initiator.server, settings)
        this.transferPlayersTo(duel, ready, transferSpectatorStatus = false)

        duel.chat.broadcastGame(DuelComponents.STARTING_DUEL.mini().green())
        duel.start()
        return true
    }
}