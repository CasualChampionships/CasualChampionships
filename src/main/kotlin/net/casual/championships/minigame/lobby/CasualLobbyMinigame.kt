package net.casual.championships.minigame.lobby

import com.google.common.collect.ImmutableList
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.chat.ChatFormatter
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.PlayerFallEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.tab.ArcadePlayerListDisplay
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.impl.PlayerTask
import net.casual.arcade.utils.CommandUtils
import net.casual.arcade.utils.CommandUtils.argument
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.literal
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.ResourcePackUtils.afterPacksLoad
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.CasualMod
import net.casual.championships.common.event.MinesweeperWonEvent
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.minigame.rules.RulesProvider
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonSounds
import net.casual.championships.common.util.CommonUI
import net.casual.championships.common.util.CommonUI.broadcastGame
import net.casual.championships.common.util.CommonUI.broadcastWithSound
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.duel.DuelRequester
import net.casual.championships.duel.DuelSettings
import net.casual.championships.duel.ui.DuelConfigurationGui
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.util.CasualConfig
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.AABB
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.time.Duration.Companion.seconds

class CasualLobbyMinigame(
    server: MinecraftServer,
    private val casualLobby: CasualLobby
): LobbyMinigame(server, casualLobby) {
    override val settings: MinigameSettings = CasualSettings(this)

    private val duels = ArrayList<DuelMinigame>()
    private val hasSeenFireworks = HashSet<UUID>()
    private var shouldWelcomePlayers = true

    override val id: ResourceLocation = ID

    fun isDueling(player: ServerPlayer): Boolean {
        for (duel in this.duels) {
            if (duel.players.has(player)) {
                return true
            }
        }
        return false
    }

    fun getAllTeams(): Collection<PlayerTeam> {
        val teams = HashSet<PlayerTeam>()
        for (duel in this.duels) {
            for (team in duel.teams.getAllNonSpectatorOrAdminTeams()) {
                if (!this.teams.isTeamIgnored(team)) {
                    teams.add(team)
                }
            }
        }
        teams.addAll(this.teams.getAllNonSpectatorOrAdminTeams())
        return teams
    }

    override fun initialize() {
        super.initialize()

        this.registerCommands()

        val display = ArcadePlayerListDisplay(CasualLobbyPlayerListEntries(this))
        CommonUI.addCasualFooterAndHeader(this, display)
        this.ui.setPlayerListDisplay(display)

        this.advancements.addAll(LobbyAdvancements.getAllAdvancements())
    }

    @Listener
    private fun onMinigameAddNewPlayer(event: MinigameAddNewPlayerEvent) {
        this.casualLobby.forceTeleportToSpawn(event.player)
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
        } else if (CasualConfig.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        } else {
            player.sendSystemMessage(Component.literal("Minigames are NOT in dev mode!").red())
        }

        player.grantAdvancement(LobbyAdvancements.ROOT)

        val team = player.team
        event.spectating = team == null || this.teams.isTeamIgnored(team)
        if (team != null) {
            team.collisionRule = Team.CollisionRule.NEVER
        }

        if (!this.hasSeenFireworks.contains(player.uuid) && CasualMinigames.hasWinner()) {
            player.afterPacksLoad {
                this.hasSeenFireworks.add(player.uuid)
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
        this.hasSeenFireworks.clear()
    }

    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        if (this.bossbar.getRemainingDuration() == 25.Seconds) {
            for (player in this.players) {
                player.sendSound(CommonSounds.WAITING, SoundSource.MASTER)
            }
        }
    }

    @Listener
    private fun onPhaseSet(event: MinigameSetPhaseEvent<LobbyMinigame>) {
        if (event.phase >= LobbyPhase.Readying) {
            for (duel in ImmutableList.copyOf(this.duels)) {
                duel.close()
            }
        }
    }

    @Listener
    private fun onPlayerFall(event: PlayerFallEvent) {
        if (!AABB.of(this.lobby.area.getBoundingBox()).intersects(event.player.boundingBox)) {
            event.player.grantAdvancement(LobbyAdvancements.UH_OH)
        }
    }

    @Listener
    private fun onMinesweeperWon(event: MinesweeperWonEvent) {
        if (event.time < 40.seconds) {
            event.player.grantAdvancement(LobbyAdvancements.OFFICIALLY_BORED)
        }
    }

    @Listener
    private fun onSequentialMinigameStart(event: SequentialMinigameStartEvent) {
        this.resetLobbyTime()
    }

    private fun resetLobbyTime() {
        this.casualLobby.area.level.dayTime = 12_500L
    }

    override fun startNextMinigame() {
        val minigame = this.nextMinigame
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

    private fun registerCommands() {
        this.commands.register(CommandUtils.buildLiteral("duel") {
            literal("start") {
                executes(::startDuel)
            }
            literal("view") {
                argument("player", EntityArgument.player()) {
                    suggests { _, builder ->
                        val players = duels.flatMap { it.players.playing }.map(ServerPlayer::getScoreboardName)
                        SharedSuggestionProvider.suggest(players, builder)
                    }
                    executes(::viewDueler)
                }
            }
        })
    }

    private fun startDuel(context: CommandContext<CommandSourceStack>): Int {
        if (this.phase >= LobbyPhase.Readying) {
            return context.source.fail(Component.translatable("casual.duel.cannotDuelNow"))
        }
        val player = context.source.playerOrException
        val settings = DuelSettings(this.casualLobby.duelArenaTemplates)
        val gui = DuelConfigurationGui(player, settings, this.players::all, this::requestDuelWith)
        return gui.open().commandSuccess()
    }

    private fun viewDueler(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException

        val dueler = EntityArgument.getPlayer(context, "player")
        val minigame = dueler.getMinigame()
        if (minigame !is DuelMinigame) {
            return context.source.fail(Component.translatable("casual.duel.playerNotDueling"))
        }

        if (!this.duels.contains(minigame)) {
            return context.source.fail("This shouldn't happen, please tell sensei!")
        }

        minigame.players.add(player, true, this.players.isAdmin(player))
        player.teleportTo(dueler.location)
        return context.source.success(Component.translatable("casual.duel.teleportingToDuel"))
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
            requester.broadcastTo(Component.translatable("casual.duel.notEnoughPlayers").mini().red(), initiator)
            return
        }

        val unready = requester.arePlayersReady(requesting) {
            started = startDuelWith(started, initiator, duelers, setOf(), requester, settings, false)
        }
        val startAnyways = Component.translatable("casual.duel.clickToStart").mini().green().function {
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
                requester.broadcastTo(Component.translatable("casual.duel.alreadyStarted").mini().red(), initiator)
            }
            return true
        }
        if (!this.players.has(initiator) || this.phase >= LobbyPhase.Readying) {
            requester.broadcastTo(Component.translatable("casual.duel.cannotDuelNow").mini().red(), initiator)
            return false
        }

        val ready = HashSet(duelers)
        if (!this.players.isAdmin(initiator)) {
            ready.removeAll(unready.toSet())
        }
        ready.removeIf { !this.players.has(it) }

        if (ready.size <= 1) {
            requester.broadcastTo(Component.translatable("casual.duel.notEnoughPlayers").mini().red(), initiator)
            return false
        }

        val duel = CasualMinigames.createDuelMinigame(initiator.server, settings)
        this.duels.add(duel)
        duel.events.register<MinigameCloseEvent> { this.duels.remove(duel) }

        duel.commands.register(CommandUtils.buildLiteral("duel") {
            literal("leave") {
                executes { context ->
                    val player = context.source.playerOrException
                    duel.players.transferTo(this@CasualLobbyMinigame, player, keepSpectating = false)
                    context.source.success("Returning to Lobby...")
                }
            }
        })

        this.players.transferTo(duel, ready, keepSpectating = false)
        duel.chat.broadcastGame(Component.translatable("casual.duel.starting").mini().green())
        duel.start()

        val players = if (ready.size > 4) {
            ready.take(4).joinToString(" & ") { it.scoreboardName }
        } else {
            ready.joinToString(" & ") { it.scoreboardName }
        }
        val aboutToDuel = Component.translatable("casual.duel.aboutToDuel", players).mini()
        for (player in this.players) {
            if (ready.contains(player)) {
                continue
            }
            requester.broadcastTo(aboutToDuel, player)

            val clickToSpectate = Component.empty().append("[")
                .append(Component.translatable("casual.duel.clickToSpectate"))
                .append("]").command("/duel view ${ready.first().scoreboardName}").lime().mini()
            requester.broadcastTo(clickToSpectate, player)
        }

        return true
    }

    companion object {
        val ID = CasualMod.id("lobby")
    }
}