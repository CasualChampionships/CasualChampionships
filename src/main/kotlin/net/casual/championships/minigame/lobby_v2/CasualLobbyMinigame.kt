package net.casual.championships.minigame.lobby_v2

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.events.server.player.PlayerVoidDamageEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.area.BoxedArea
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.minigame.events.MinigameAddNewPlayerEvent
import net.casual.arcade.minigame.events.MinigameInitializeEvent
import net.casual.arcade.minigame.managers.MinigameLevelManager.SpawnLocation
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.minigame.utils.MinigameUtils.countdown
import net.casual.arcade.minigame.utils.MinigameUtils.transferAdminAndSpectatorTeamsTo
import net.casual.arcade.scheduler.task.Completable
import net.casual.arcade.utils.*
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.unboostHealth
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.minigame.rules.MinigameRulesProvider
import net.casual.championships.common.ui.bossbar.LobbyBossbar
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastWithSound
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.common.util.casual
import net.casual.championships.minigame.lobby.LobbyAdvancements
import net.casual.championships.minigame.lobby.LobbyStats
import net.casual.championships.minigame.lobby_v2.modules.CasualLobbyData
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.scores.PlayerTeam
import java.io.IOException
import java.util.*
import kotlin.reflect.KProperty0

class CasualLobbyMinigame(
    server: MinecraftServer,
    uuid: UUID,
    next: KProperty0<Minigame?>,
    val modules: MinigameDataModules
): Minigame(server, uuid) {
    private val level = this.createLevel()

    private val lobbyData: CasualLobbyData
        get() = this.modules.get<CasualLobbyData>() ?: CasualLobbyData.DEFAULT

    val parkour = CasualLobbyParkour(this)
    val duels = CasualLobbyDuels(this)

    val bossbar = LobbyBossbar()
    val next by next

    override val settings: MinigameSettings = CasualSettings(this)
    override val id: ResourceLocation = ID

    fun teleport(player: ServerPlayer) {
        player.teleportTo(this.lobbyData.spawn.get().with(this.level))
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

        this.setPhase(CasualLobbyPhase.Waiting)
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
        return CasualLobbyPhase.entries
    }

    @Listener
    private fun onMinigameInitialize(event: MinigameInitializeEvent) {
        this.levels.add(this.level)
        this.levels.spawn = SpawnLocation.global(this.level, BlockPos.containing(this.lobbyData.spawn.get().position))

        this.parkour.initialize()

        this.commands.register(LobbyCommand(this))

        val display = PlayerListDisplay(CasualLobbyPlayerListEntries(this))
        CasualGuiUtils.addCasualFooterAndHeader(this, display)
        this.ui.setPlayerListDisplay(display)

        this.advancements.addAll(LobbyAdvancements)

        this.ui.addBossbar(this.bossbar)

        // this.ui.setSidebar(this.createSidebar())

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
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        player.grantAdvancement(LobbyAdvancements.UH_OH)

        val stat = this.stats.getOrCreateStat(player, LobbyStats.LEFT_LOBBY)
        stat.increment()
        if (stat.value >= 20) {
            player.grantAdvancement(LobbyAdvancements.YOU_SHALL_NOT_LEAVE)
        }

        this.teleport(player)
        event.cancel()
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
        private val lobbies = CasualUtils.resolve("lobbies")

        val ID = casual("lobby")

        fun create(lobby: String, next: KProperty0<Minigame?>, context: MinigameCreationContext): CasualLobbyMinigame {
            val server = context.server
            val modules = try {
                val archive = ReadableArchive.from(this.lobbies.resolve(lobby))
                MinigameDataModules.from(archive, server)
            } catch (exception: IOException) {
                CasualUtils.logger.error("Failed to read lobby $lobby", exception)
                MinigameDataModules.empty()
            }
            return CasualLobbyMinigame(server, context.uuid, next, modules)
        }
    }
}