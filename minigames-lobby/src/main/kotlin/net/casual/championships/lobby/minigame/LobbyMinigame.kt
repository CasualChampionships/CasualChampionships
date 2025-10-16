package net.casual.championships.lobby.minigame

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.player.PlayerVoidDamageEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.area.BoxedArea
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.minigame.events.MinigameAddNewPlayerEvent
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.events.MinigameSetPhaseEvent
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.minigame.managers.MinigameLevelManager.SpawnLocation
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.utils.MinigameUtils.addEventListener
import net.casual.arcade.minigame.utils.MinigameUtils.countdown
import net.casual.arcade.minigame.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.resources.utils.ResourcePackUtils.afterPacksLoad
import net.casual.arcade.scheduler.task.Completable
import net.casual.arcade.scheduler.task.impl.PlayerTask
import net.casual.arcade.utils.*
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.PlayerUtils.unboostHealth
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.component.shadowless
import net.casual.arcade.utils.component.wrap
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.minigame.rules.MinigameRulesProvider
import net.casual.championships.common.ui.bossbar.LobbyBossbar
import net.casual.championships.common.util.*
import net.casual.championships.common.util.CasualGuiUtils.broadcastWithSound
import net.casual.championships.lobby.advancement.LobbyAdvancementManager
import net.casual.championships.lobby.advancement.LobbyAdvancements
import net.casual.championships.lobby.gui.LobbyPlayerListEntries
import net.casual.championships.lobby.minigame.command.DuelCommand
import net.casual.championships.lobby.minigame.command.LobbyCommand
import net.casual.championships.lobby.minigame.command.MinesweeperCommand
import net.casual.championships.lobby.minigame.modules.LobbyData
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.reflect.KProperty0

class LobbyMinigame(
    server: MinecraftServer,
    uuid: UUID,
    next: KProperty0<Minigame?>,
    val modules: MinigameDataModules
): Minigame(server, uuid) {
    private val lobbyData: LobbyData
        get() = this.modules.get<LobbyData>() ?: LobbyData.DEFAULT

    private val fireworks = LobbyFireworks(this, this.lobbyData)
    private val parkour = LobbyParkour(this)
    val duels = LobbyDuels(this)

    val level: ServerLevel = this.createLevel()
    val bossbar = LobbyBossbar()
    val next by next

    override val settings: MinigameSettings = CasualSettings(this)
    override val id: ResourceLocation = ID

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

    fun startCountdown() {
        this.ui.countdown.countdown(this).then {
            this.scheduler.schedulePhased(1.Seconds, this::moveToNextMinigame)
        }
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

        this.setPhase(LobbyPhase.Waiting)
    }

    fun playRulesForNextMinigame(): Completable {
        val next = this.next
        if (next !is MinigameRulesProvider) {
            return Completable.complete()
        }

        val completable = Completable.Impl()
        this.settings.isChatMuted.set(true)
        val rules = next.getRules()
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
            completable.complete()
        }
        this.scheduler.schedulePhasedCancellable(delay) {
            this.settings.isChatMuted.set(false)
        }.runIfCancelled()
        return completable
    }

    override fun phases(): Collection<Phase<out Minigame>> {
        return LobbyPhase.entries
    }

    @Listener
    private fun onMinigameInitialize(event: MinigameInitializeEvent) {
        this.levels.add(this.level)
        this.levels.spawn = SpawnLocation.global(this.lobbyData.spawn.get().with(this.level))

        this.parkour.initialize()

        this.commands.register(DuelCommand(this))
        this.commands.register(LobbyCommand(this))
        this.commands.register(MinesweeperCommand(this))

        val display = PlayerListDisplay(LobbyPlayerListEntries(this))
        CasualGuiUtils.addCasualFooterAndHeader(this, display)
        this.ui.setPlayerListDisplay(display)

        this.addEventListener(LobbyAdvancementManager(this))
        this.advancements.addAll(LobbyAdvancements)

        this.ui.addBossbar(this.bossbar)

        this.registerProperties()

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
        this.settings.daylightCycle = 0

        if (!this.modules.has<MinigameWorldData>()) {
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
            player.setTitleAnimation(stay = 5.Seconds)
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
            player.afterPacksLoad { this.playFireworksFor(player) }
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
        if (this.bossbar.getRemainingDuration() == 25.Seconds) {
            this.players.forEach { player -> player.sendSound(CasualSounds.WAITING) }
        }
    }

    @Listener
    private fun onMinigamePhaseSet(event: MinigameSetPhaseEvent) {
        if (event.phase >= LobbyPhase.Readying) {
            this.duels.close()
        }
    }

    private fun registerProperties() {
        this.property("next_minigame") { this.next?.id?.toString() }
    }

    private fun playFireworksFor(player: ServerPlayer) {
        this.tags.add(player, SEEN_FIREWORKS)
        player.sendSound(CasualSounds.GAME_WON)
        this.scheduler.schedule(10.Seconds, PlayerTask(player, this.fireworks::spawnFireworkDisplayFor))
    }

    private fun extractLobbyWorld(destination: ResourceKey<Level>) {
        val world = this.modules.get<MinigameWorldData>() ?: return
        world.extract(this.server, destination)
    }

    private fun createLevel(): CustomLevel {
        val data = this.lobbyData
        val dimension = ResourceUtils.random().toKey(Registries.DIMENSION)
        this.extractLobbyWorld(dimension)
        val level = CustomLevelBuilder.build(this.server) {
            spoofedDimensionKey(casual("lobby"))
            dimensionKey(dimension)
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(server))
            defaultLevelProperties()
            persistence(LevelPersistence.Temporary)
            viewDistance(20)
            weather {
                if (data.raining) {
                    raining = true
                    rainTime = 999999
                }
            }
            gameRules {
                resetToDefault()
                set(GameRules.RULE_DOINSOMNIA, false)
                set(GameRules.RULE_DOFIRETICK, false)
                set(GameRules.RULE_DOMOBSPAWNING, false)
                set(GameRules.RULE_FALL_DAMAGE, false)
                set(GameRules.RULE_DROWNING_DAMAGE, false)
                set(GameRules.RULE_DOENTITYDROPS, false)
                set(GameRules.RULE_WEATHER_CYCLE, false)
                set(GameRules.RULE_DO_TRADER_SPAWNING, false)
                set(GameRules.RULE_DOMOBLOOT, false)
                set(GameRules.RULE_DOBLOCKDROPS, false)
                set(GameRules.RULE_COMMANDBLOCKOUTPUT, false)
                set(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT, 0)
                set(GameRules.RULE_RANDOMTICKING, 0)
                set(GameRules.RULE_LOCATOR_BAR, false)
            }
            when {
                data.timeOfDay.isEmpty -> tickTime(true)
                else -> timeOfDay(data.timeOfDay.get().toLong())
            }
        }
        return level
    }

    companion object {
        private val SEEN_FIREWORKS = casual("seen_fireworks")
        private val WELCOMED = casual("welcomed")

        val ID = casual("lobby")
    }
}