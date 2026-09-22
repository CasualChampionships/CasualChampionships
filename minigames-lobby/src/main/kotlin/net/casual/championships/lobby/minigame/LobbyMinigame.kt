package net.casual.championships.lobby.minigame

import kotlinx.coroutines.awaitCancellation
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.player.PlayerVoidDamageEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.area.BoxedArea
import net.casual.arcade.minigame.data.MinigameDataSet
import net.casual.arcade.minigame.data.impl.MinigameWorldData
import net.casual.arcade.minigame.events.MinigameAddNewPlayerEvent
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.events.MinigameSetPhaseEvent
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.minigame.managers.MinigameLevelManager.SpawnLocation
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.utils.MinigameUtils.addEventListener
import net.casual.arcade.minigame.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.pack.utils.ResourcePackUtils.awaitPacks
import net.casual.arcade.scheduler.task.impl.PlayerTask
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.level.resetToDefault
import net.casual.arcade.utils.level.set
import net.casual.arcade.utils.player.*
import net.casual.arcade.virtual.visuals.tab.DynamicVirtualPlayerList
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.minigame.rules.MinigameRulesProvider
import net.casual.championships.common.util.*
import net.casual.championships.common.util.CasualGuiUtils.broadcastWithSound
import net.casual.championships.common.util.player.unboostHealth
import net.casual.championships.lobby.advancement.LobbyAdvancementManager
import net.casual.championships.lobby.advancement.LobbyAdvancements
import net.casual.championships.lobby.minigame.command.DuelCommand
import net.casual.championships.lobby.minigame.command.LobbyCommand
import net.casual.championships.lobby.minigame.command.MinesweeperCommand
import net.casual.championships.lobby.minigame.modules.LobbyData
import net.casual.championships.lobby.ui.bossbar.LobbyBossbar
import net.casual.championships.lobby.ui.tab.LobbyPlayerListEntries
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.reflect.KProperty0

class LobbyMinigame(
    server: MinecraftServer,
    uuid: UUID,
    next: KProperty0<Minigame?>,
    val modules: MinigameDataSet
): Minigame(server, uuid, ID, LobbyPhase.entries) {
    private val lobbyData: LobbyData = this.modules.get(LobbyData.type) ?: LobbyData.DEFAULT

    private val fireworks = LobbyFireworks(this, this.lobbyData)
    private val parkour = LobbyParkour(this)
    val duels = LobbyDuels(this)

    val level: ServerLevel = this.createLevel()
    val timer = TimerElement()
    val bossbar = LobbyBossbar.create(this.server, this.timer)
    val next by next

    override val settings: MinigameSettings = CasualSettings(this)

    fun teleport(player: ServerPlayer) {
        val winners = this.tags.getUUIDsFor(CasualTags.WON)
        val location = when {
            winners.isEmpty() -> this.lobbyData.spawn.get()
            winners.contains(player.uuid) -> this.lobbyData.podium.get()
            else -> this.lobbyData.podiumView.get()
        }
        player.teleportTo(location.with(this.level))
    }

    fun getAllTeams(): Set<PlayerTeam> {
        val teams = HashSet(this.duels.getAllTeams())
        teams.addAll(this.teams.getAllNonSpectatorOrAdminTeams())
        return teams
    }

    fun getAdditionalPacks(): List<String> {
        return this.lobbyData.packs
    }

    fun moveToNextMinigame() {
        val next = this.next
        if (next == null || next.closed) {
            CasualUtils.logger.error("Failed to move to next minigame, it was not specified or closed!")
            return
        }

        this.transferAdminAndSpectatorTeamsTo(next)
        this.players.transferTo(next, players)
        next.start()

        this.phases.request(LobbyPhase.Waiting)
    }

    suspend fun playRulesForNextMinigame() {
        val next = this.next
        if (next !is MinigameRulesProvider) {
            return
        }

        try {
            this.settings.isChatMuted.set(true)
            val rules = next.getRules()
            for (rule in rules) {
                for (entry in rule.entries) {
                    delay(entry.duration)
                    val formatter = ChatFormatter.createAnnouncement(rule.title)
                    val message = entry.lines.fold(Component.empty()) { a, b -> a.append("\n\n").append(b) }
                    this.chat.broadcastWithSound(message, formatter = formatter)
                }
            }
        } finally {
            this.settings.isChatMuted.set(false)
        }
    }

    override fun debug(output: ValueOutput) {
        super.debug(output)
        output.storeNullable("next_minigame_id", Identifier.CODEC, this.next?.id)
    }

    @Listener
    private fun onMinigameInitialize(event: MinigameInitializeEvent) {
        this.initializePhases()

        this.levels.spawn = SpawnLocation.global(this.lobbyData.spawn.get().with(this.level))

        this.parkour.initialize()

        this.commands.register(DuelCommand(this))
        this.commands.register(LobbyCommand(this))
        this.commands.register(MinesweeperCommand(this))

        val display = DynamicVirtualPlayerList(this.server, LobbyPlayerListEntries(this))
        CasualGuiUtils.addCasualFooterAndHeader(this, display)
        this.visuals.setPlayerListDisplay(display)

        this.addEventListener(LobbyAdvancementManager(this))
        this.advancements.addAll(LobbyAdvancements)

        this.visuals.addBossbar(this.bossbar)

        this.settings.pauseOnServerStop = false
        this.settings.canPvp.set(false)
        this.settings.canGetHungry.set(false)
        this.settings.canBreakBlocks.set(false)
        this.settings.canPlaceBlocks.set(false)
        this.settings.canDropItems.set(false)
        this.settings.canPickupItems.set(false)
        this.settings.canTakeDamage.set(false)
        this.settings.canAttackEntities.set(true)
        this.settings.canInteractAll = false

        if (!this.modules.has(MinigameWorldData.type)) {
            BoxedArea(Vec3i(0, -1, 0), 10, 3, this.level).place()
        }
    }

    @Listener
    private fun onMinigameAddNewPlayer(event: MinigameAddNewPlayerEvent) {
        val (_, player) = event
        player.resetHealth()
        player.unboostHealth()
        player.resetExperience()
        player.resetHunger()
        player.clearPlayerInventory()
        player.removeAllEffects()

        this.teleport(player)
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val (_, player) = event
        if (this.tags.add(player, WELCOMED)) {
            player.setTitleAnimation(stay = 10.Seconds)
            player.sendTitle(CasualComponents.Text.WELCOME_TO_CASUAL_CHAMPIONSHIPS.wrap().shadowless())
        }

        if (!this.players.isAdmin(player)) {
            player.extendedGameMode = ExtendedGameMode.Adventure
        }

        player.grantAdvancement(LobbyAdvancements.ROOT)
        val team = player.team
        event.spectating = team == null || this.teams.isTeamIgnored(team)
        if (team != null) {
            team.collisionRule = Team.CollisionRule.NEVER
        }

        val winners = this.tags.getUUIDsFor(CasualTags.WON)
        if (!this.tags.has(player, SEEN_FIREWORKS) && winners.isNotEmpty()) {
            this.server.launch {
                player.awaitPacks()
                playFireworksFor(player)
            }
        }
    }

    @Listener
    private fun onPlayerTeamJoin(event: PlayerTeamJoinEvent) {
        val (player, team) = event
        when {
            this.teams.isTeamIgnored(team) -> this.players.setSpectating(player)
            else -> this.players.setPlaying(player)
        }
    }

    @Listener
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        this.teleport(event.player)
        event.cancel()
    }

    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        if (this.timer.getRemainingDuration() == 25.Seconds) {
            this.players.forEach { player -> player.sendSound(CasualSounds.WAITING) }
        }

        if (this.timer.getRemainingDuration() == 1.Ticks) {
            val component = Component {
                translatable("minigame.lobby.ready.finishedWaiting") + nl +
                    literal("[Click to ready teams]").lime().command("/lobby ready teams") + nl +
                    literal("[Click to ready players]").lime().command("/lobby ready players")
            }

            this.chat.broadcastTo(component, this.players.admins)
        }
    }

    @Listener
    private fun onMinigamePhaseSet(event: MinigameSetPhaseEvent) {
        if (event.phase >= LobbyPhase.Readying) {
            this.duels.close()
        }
    }

    private fun playFireworksFor(player: ServerPlayer) {
        this.tags.add(player, SEEN_FIREWORKS)
        player.sendSound(CasualSounds.GAME_WON)
        this.scheduler.schedule(10.Seconds, PlayerTask(player, this.fireworks::spawnFireworkDisplayFor))
    }

    private fun createLevel(): CustomLevel {
        val data = this.lobbyData
        val level = this.levels.create(arcade("overworld")) {
            spoofedDimensionKey(casual("lobby"))
            randomDimensionKey()
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(server))
            defaultLevelProperties()
            viewDistance(20)
            weather {
                if (data.raining) {
                    isRaining = true
                    rainTime = 999999
                }
            }
            gameRules {
                resetToDefault()
                set(GameRules.SPAWN_PHANTOMS, false)
                set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0)
                set(GameRules.FALL_DAMAGE, false)
                set(GameRules.DROWNING_DAMAGE, false)
                set(GameRules.ENTITY_DROPS, false)
                set(GameRules.ADVANCE_WEATHER, false)
                set(GameRules.SPAWN_WANDERING_TRADERS, false)
                set(GameRules.MOB_DROPS, false)
                set(GameRules.BLOCK_DROPS, false)
                set(GameRules.COMMAND_BLOCK_OUTPUT, false)
                set(GameRules.MAX_SNOW_ACCUMULATION_HEIGHT, 0)
                set(GameRules.RANDOM_TICK_SPEED, 0)
                set(GameRules.SPAWN_MOBS, false, server)
                set(GameRules.LOCATOR_BAR, false, server)
            }
            when {
                data.timeOfDay.isEmpty -> clockState(paused = false)
                else -> clockState(totalTicks = data.timeOfDay.get().toLong(), paused = true)
            }
        }
        this.modules.get(MinigameWorldData.type)?.extract(this.server, level.dimension())
        return level
    }

    private fun initializePhases() {
        this.phases.coroutines[LobbyPhase.Waiting] = this::runWaitingLogic
        this.phases.coroutines[LobbyPhase.Readying] = this::runReadingLogic
        this.phases.coroutines[LobbyPhase.Countdown] = this::runCountdownLogic
    }

    private suspend fun runWaitingLogic() {
        this.visuals.addBossbar(this.bossbar)
        for (team in this.teams.getAllTeams()) {
            team.collisionRule = Team.CollisionRule.NEVER
        }
    }

    private suspend fun runReadingLogic() {
        awaitCancellation()
    }

    private suspend fun runCountdownLogic() {
        this.visuals.removeBossbar(this.bossbar)
        for (team in this.teams.getAllTeams()) {
            team.collisionRule = Team.CollisionRule.ALWAYS
        }
        this.visuals.countdown.transition(players = players::all)
        delay(1.Seconds)
        this.moveToNextMinigame()
    }

    companion object {
        private val SEEN_FIREWORKS = casual("seen_fireworks")
        private val WELCOMED = casual("welcomed")

        val ID = casual("lobby")
    }
}