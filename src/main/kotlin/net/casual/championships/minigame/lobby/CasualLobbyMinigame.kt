package net.casual.championships.minigame.lobby

import com.google.common.collect.ImmutableList
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import eu.pb4.sgui.api.GuiHelpers
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.casual.arcade.commands.*
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerFallEvent
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.events.server.player.PlayerTryAttackEvent
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.minigame.lobby.LobbyPhase
import net.casual.arcade.minigame.ready.ReadyChecker
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.isMinigameAdminOrHasPermission
import net.casual.arcade.resources.utils.ResourcePackUtils.afterPacksLoad
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.set
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.elements.UniversalElement
import net.casual.arcade.visuals.entity.firework.VirtualFirework
import net.casual.arcade.visuals.sidebar.DynamicSidebar
import net.casual.arcade.visuals.sidebar.Sidebar
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.sidebar.SidebarComponents
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.championships.CasualMod
import net.casual.championships.commands.MinesweeperCommand
import net.casual.championships.common.event.MinesweeperWonEvent
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.minigame.rules.RulesProvider
import net.casual.championships.common.ui.bossbar.LobbyBossbar
import net.casual.championships.common.ui.elements.TeammatesSidebarElements
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonSounds
import net.casual.championships.common.util.CommonUI
import net.casual.championships.common.util.CommonUI.broadcastGame
import net.casual.championships.common.util.CommonUI.broadcastWithSound
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.duel.DuelMinigameFactory
import net.casual.championships.duel.DuelRequester
import net.casual.championships.duel.DuelSettings
import net.casual.championships.duel.arena.DuelArenasTemplate
import net.casual.championships.duel.ui.DuelConfigurationGui
import net.casual.championships.minigame.CasualMinigames
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.component.FireworkExplosion.Shape
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.text.DecimalFormat
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class CasualLobbyMinigame(
    server: MinecraftServer,
    uuid: UUID,
    spawn: LocationWithLevel<ServerLevel>,
    private val duelArenaTemplates: List<DuelArenasTemplate>,
    private val modules: MinigameDataModules,
    private val factory: CasualLobbyMinigameFactory
): LobbyMinigame(server, uuid, EmptyLobbyArea(spawn.level), spawn) {
    override val settings: MinigameSettings = CasualSettings(this)

    private val parkourers = Object2IntOpenHashMap<UUID>()
    private val duels = ArrayList<DuelMinigame>()
    private val hasSeenFireworks = HashSet<UUID>()
    private var shouldWelcomePlayers = true

    private var minesweeperRecord = 127 * 1_000

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

    override fun factory(): MinigameFactory {
        return this.factory
    }

    @Listener(priority = 1100)
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.registerCommands()

        val display = PlayerListDisplay(CasualLobbyPlayerListEntries(this))
        CommonUI.addCasualFooterAndHeader(this, display)
        this.ui.setPlayerListDisplay(display)

        this.advancements.addAll(LobbyAdvancements)

        this.setBossbar(LobbyBossbar())

        this.ui.setSidebar(this.createSidebar())

        this.settings.canAttackEntities.set(true)

        this.levels.setGameRules {
            set(GameRules.RULE_LOCATOR_BAR, false)
        }

        val parkour = this.modules.get<CasualLobbyParkourData>()
        if (parkour != null) {
            this.events.register<ServerTickEvent> {
                this.tickParkour(parkour)
            }
        }
    }

    @Listener
    private fun onMinigameAddNewPlayer(event: MinigameAddNewPlayerEvent) {
        this.teleportToSpawn(event.player)
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val player = event.player
        if (this.shouldWelcomePlayers) {
            player.setTitleAnimation(stay = 5.Seconds)
            player.sendTitle(
                CommonComponents.Text.WELCOME_TO_CASUAL_CHAMPIONSHIPS.copy().shadowless()
            )
        }

        if (!this.players.isAdmin(player)) {
            player.setGameMode(GameType.ADVENTURE)
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
                this.scheduler.schedule(10.Seconds) {
                    this.spawnFireworkDisplays(player, this.scheduler)
                }
            }
        }
    }

    @Listener
    private fun onMinigameAddAdmin(event: MinigameAddAdminEvent) {
        val player = event.player
        if (CasualMod.config.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        } else {
            player.sendSystemMessage(Component.literal("Minigames are NOT in dev mode!").red())
        }
        val location = if (CasualMod.config.dev) "${CasualMod.config.database.name}_debug" else CasualMod.config.database.name
        player.sendSystemMessage(Component.literal("Minigames are using $location database!").red())
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

        if (this.players.onlinePlayerCount > 0) {
            if (this.uptime.Ticks > 60.Seconds && this.uptime % 30.Seconds.ticks == 0) {
                CasualMinigames.reloadTeams(event.server)
            }
        }
    }

    @Listener
    private fun onPlayerTick(event: PlayerTickEvent) {
        val player = event.player
        val pb = this.stats.getOrCreateStat(player, LobbyStats.MINESWEEPER_RECORD)
        val held = this.stats.getOrCreateStat(player, LobbyStats.MINESWEEPER_RECORD_HELD)
        if (pb.value == this.minesweeperRecord) {
            held.increment()
            if (held.value.Ticks >= 10.Minutes) {
                player.grantAdvancement(LobbyAdvancements.GAMER)
            }
        } else {
            held.modify { 0 }
        }
    }

    @Listener
    private fun onPlayerAttack(event: PlayerTryAttackEvent) {
        val (player, target) = event
        if (target is ServerPlayer && this.players.isAdmin(target)) {
            val stat = this.stats.getOrCreateStat(player, LobbyStats.ATTACK_ADMIN)
            stat.increment()
            if (stat.value >= 50) {
                player.grantAdvancement(LobbyAdvancements.ADMIN_ABUSE)
            }
        }
        event.cancel()
    }

    @Listener
    private fun onPhaseSet(event: MinigameSetPhaseEvent) {
        if (event.phase >= LobbyPhase.Readying) {
            for (duel in ImmutableList.copyOf(this.duels)) {
                duel.close()
            }
        }
    }

    @Listener
    private fun onPlayerFall(event: PlayerFallEvent) {
        val minY = this.area.getEntityBoundingBox().minY + 5
        if (event.player.y < minY) {
            event.player.grantAdvancement(LobbyAdvancements.UH_OH)
            val stat = this.stats.getOrCreateStat(event.player, LobbyStats.LEFT_LOBBY)
            stat.increment()
            if (stat.value >= 20) {
                // event.player.grantAdvancement(LobbyAdvancements.YOU_SHALL_NOT_LEAVE)
            }
        }
    }

    @Listener
    private fun onMinesweeperWon(event: MinesweeperWonEvent) {
        val (player, duration) = event
        if (duration < 40.seconds) {
            event.player.grantAdvancement(LobbyAdvancements.OFFICIALLY_BORED)
        }

        val millis = duration.inWholeMilliseconds.toInt()
        val stat = this.stats.getOrCreateStat(player, LobbyStats.MINESWEEPER_RECORD)
        if (millis < stat.value) {
            stat.modify { millis }
        }

        val formatted = FORMAT.format(duration.toDouble(DurationUnit.SECONDS))
        player.sendSystemMessage(CommonComponents.MINESWEEPER_WON.generate(formatted))
        if (millis < this.minesweeperRecord) {
            this.minesweeperRecord = millis
            val message = CommonComponents.MINESWEEPER_RECORD.generate(player.scoreboardName, formatted)
            this.chat.broadcast(message)
        }
    }

    private fun tickParkour(data: CasualLobbyParkourData) {
        for (player in this.players) {
            val isParkouring = data.isWithinParkourArea(player.position())
            val wasParkouring = this.parkourers.containsKey(player.uuid)
            if (!isParkouring) {
                if (wasParkouring) {
                    this.parkourers.removeInt(player.uuid)
                    val gui = GuiHelpers.getCurrentGui(player) as? CasualLobbyParkourHotbarGui
                    gui?.close()
                }
                continue
            }

            if (GuiHelpers.getCurrentGui(player) == null) {
                CasualLobbyParkourHotbarGui(player, data.exit).open()
            }

            val currentCheckpointIndex = this.parkourers.getInt(player.uuid)
            val updatedCheckpointIndex = data.getIntersectingCheckpoint(player.position(), currentCheckpointIndex)
            val checkpoint = data.checkpoints[updatedCheckpointIndex]
            if (currentCheckpointIndex != updatedCheckpointIndex || !wasParkouring) {
                this.parkourers.put(player.uuid, updatedCheckpointIndex)
                player.sendTitle(checkpoint.title)
                player.sendSound(SoundEvents.ARROW_HIT_PLAYER)
                if (updatedCheckpointIndex == data.checkpoints.lastIndex) {
                    player.grantAdvancement(LobbyAdvancements.PARKOUR_MASTER)
                }
            }

            val yTeleportThreshold = checkpoint.collision.minY - 15
            if (player.y < yTeleportThreshold) {
                player.teleportTo(checkpoint.spawn)
            }
        }
    }

    override fun startNextMinigame() {
        val minigame = this.next
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

    override fun teleportToSpawn(player: ServerPlayer) {
        val data = this.modules.get<CasualLobbyData>()!!

        val location = if (CasualMinigames.isWinner(player)) {
            data.podium.get().with(this.area.level)
        } else if (CasualMinigames.hasWinner()) {
            data.podiumView.get().with(this.area.level)
        } else {
            this.spawn
        }
        player.teleportTo(location)
    }

    private fun registerCommands() {
        this.commands.register(CommandTree.buildLiteral("minesweeper") {
            executes(::minesweeper)
        })
        this.commands.register(CommandTree.buildLiteral("duel") {
            executes(::startDuel)
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

    private fun minesweeper(context: CommandContext<CommandSourceStack>): Int {
        if (this.phase < LobbyPhase.Readying) {
            return MinesweeperCommand.execute(context)
        }
        context.source.playerOrException.grantAdvancement(LobbyAdvancements.NOT_NOW)
        return context.source.fail(Component.translatable("casual.duel.cannotMinesweeperNow"))
    }

    private fun startDuel(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (this.phase >= LobbyPhase.Readying) {
            player.grantAdvancement(LobbyAdvancements.NOT_NOW)
            return context.source.fail(Component.translatable("casual.duel.cannotDuelNow"))
        }
        val settings = DuelSettings(this.duelArenaTemplates)
        val gui = DuelConfigurationGui(player, settings, this.players::all, this::requestDuelWith)
        gui.open()
        return Command.SINGLE_SUCCESS
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
        player.teleportTo(dueler.locationWithLevel)
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
        if (requesting.isEmpty() && !initiator.isMinigameAdminOrHasPermission(4)) {
            requester.broadcastTo(Component.translatable("casual.duel.notEnoughPlayers").withMiniFont().red(), initiator)
            return
        }

        val checker = ReadyChecker(requester)
        checker.arePlayersReady(requesting).then {
            started = startDuelWith(started, initiator, duelers, setOf(), requester, settings, false)
        }
        val startAnyways = Component.translatable("casual.duel.clickToStart").withMiniFont().green().function { context ->
            val unready = checker.getUnreadyPlayers(context.server)
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
                requester.broadcastTo(Component.translatable("casual.duel.alreadyStarted").withMiniFont().red(), initiator)
            }
            return true
        }
        if (!this.players.has(initiator) || this.phase >= LobbyPhase.Readying) {
            requester.broadcastTo(Component.translatable("casual.duel.cannotDuelNow").withMiniFont().red(), initiator)
            initiator.grantAdvancement(LobbyAdvancements.NOT_NOW)
            return false
        }

        val ready = HashSet(duelers)
        if (!this.players.isAdmin(initiator)) {
            ready.removeAll(unready.toSet())
        }
        ready.removeIf { !this.players.has(it) }

        if (ready.size <= 1 && !initiator.isMinigameAdminOrHasPermission(4)) {
            requester.broadcastTo(Component.translatable("casual.duel.notEnoughPlayers").withMiniFont().red(), initiator)
            return false
        }

        val duel = DuelMinigameFactory(settings).create(MinigameCreationContext(initiator.levelServer))
        this.duels.add(duel)
        duel.events.register<MinigameCloseEvent> { this.duels.remove(duel) }

        duel.commands.register(CommandTree.buildLiteral("duel") {
            literal("leave") {
                executes { context ->
                    val player = context.source.playerOrException
                    duel.players.transferTo(this@CasualLobbyMinigame, player, keepSpectating = false)
                    context.source.success("Returning to Lobby...")
                }
            }
        })

        this.players.transferTo(duel, ready, keepSpectating = false)
        duel.chat.broadcastGame(Component.translatable("casual.duel.starting").withMiniFont().green())
        duel.start()

        val players = if (ready.size > 4) {
            ready.take(4).joinToString(" & ") { it.scoreboardName }
        } else {
            ready.joinToString(" & ") { it.scoreboardName }
        }
        val aboutToDuel = Component.translatable("casual.duel.aboutToDuel", players).withMiniFont()
        for (player in this.players) {
            if (ready.contains(player)) {
                continue
            }
            requester.broadcastTo(aboutToDuel, player)

            val clickToSpectate = Component.empty().append("[")
                .append(Component.translatable("casual.duel.clickToSpectate"))
                .append("]").command("/duel view ${ready.first().scoreboardName}").lime().withMiniFont()
            requester.broadcastTo(clickToSpectate, player)
        }

        return true
    }

    private fun spawnFireworkDisplays(player: ServerPlayer, scheduler: MinecraftScheduler) {
        scheduler.scheduleInLoop(MinecraftTimeDuration.ZERO, 10.Ticks, 10.Seconds) {
            if (player.level() === this.area.level) {
                this.spawnFireworkDisplay(player)
            }
        }
    }

    private fun spawnFireworkDisplay(player: ServerPlayer) {
        val data = this.modules.get<CasualLobbyData>()!!
        for (template in data.fireworkLocations) {
            val firingLocation = template.get().with(this.area.level)
            val firework = VirtualFirework.build(this.area.level) {
                location = firingLocation
                duration = Random.nextInt(20, 30).Ticks

                SHAPES.asSequence().shuffled().take(Random.nextInt(1, 4)).forEach { shape ->
                    val index = Random.nextInt(data.fireworkColors.size)
                    explosion {
                        shape(shape)
                        addPrimaryColours(data.fireworkColors[index])
                        addFadeColours(data.fireworkColors[index])
                        trail()
                        twinkle()
                    }
                }
            }
            firework.sendTo(player)
        }
    }

    @Suppress("UnstableApiUsage")
    private fun createSidebar(): Sidebar {
        val name = CasualMinigames.getMinigames().event.name.replace('_', ' ')
        val title = Component.literal("Casual Championships").withMiniFont().bold().gold()
        val sidebar = DynamicSidebar(ComponentElements.of(title))
        val event = SidebarComponent.withCustomScore(
            Component.literal(" Event:").withMiniFont().red().bold(),
            Component.literal("$name ").withMiniFont().gold()
        )

        val teammates = TeammatesSidebarElements(Component.literal(" "), Component.literal(" "), false)
        val players = UniversalElement.cached { server ->
            val playing = server.scoreboard.playerTeams.filter {
                !this.teams.isTeamIgnored(it)
            }
            val online = playing.sumOf { it.getOnlineCount() }
            val expected = playing.sumOf { it.players.size }
            SidebarComponent.withCustomScore(
                Component.literal(" Players: ").withMiniFont().lime().bold(),
                Component.literal("$online/$expected ").withMiniFont().yellow()
            )
        }
        sidebar.setRows(PlayerSpecificElement.composed(players) { player ->
            val components = SidebarComponents.of(SidebarComponent.EMPTY)
            components.addRow(event)
            components.addRow(SidebarComponent.EMPTY)

            val team = player.team
            if (team != null && !this.teams.isTeamIgnored(team)) {
                teammates.addTeammates(player, components)
                components.addRow(SidebarComponent.EMPTY)
            }

            components.addRow(players.get(player))
                .addRow(SidebarComponent.EMPTY)
        })
        return sidebar
    }

    companion object {
        private val SHAPES = listOf(Shape.LARGE_BALL, Shape.STAR, Shape.BURST)

        private val FORMAT = DecimalFormat("#.00")

        val ID = CasualMod.id("lobby")
    }
}