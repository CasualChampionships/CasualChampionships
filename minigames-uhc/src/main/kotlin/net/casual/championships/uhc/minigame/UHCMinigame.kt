package net.casual.championships.uhc.minigame

import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevels
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.guis.utils.removeCustomInventory
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_PLAYING
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.minigame.utils.MinigameUtils.addEventListener
import net.casual.arcade.pack.utils.ResourcePackUtils.afterPacksLoad
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.task.impl.PlayerTask
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.math.location.with
import net.casual.arcade.utils.player.*
import net.casual.arcade.utils.registries.isOf
import net.casual.arcade.utils.scoreboard.color
import net.casual.arcade.utils.scoreboard.getOnlineCount
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.arcade.virtual.visuals.predicate.PlayerObserverPredicate
import net.casual.championships.common.event.PlayerCheatEvent
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.items.minigame.recipes.GoldenHeadRecipe
import net.casual.championships.common.minigame.CasualTimeTracker
import net.casual.championships.common.minigame.TimeTrackedMinigame
import net.casual.championships.common.minigame.rules.MinigameRulesProvider
import net.casual.championships.common.ui.bossbar.ActiveBossbar
import net.casual.championships.common.util.*
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.CasualGuiUtils.broadcastWithSound
import net.casual.championships.common.util.player.boostHealth
import net.casual.championships.common.util.player.unboostHealth
import net.casual.championships.uhc.advancement.UHCAdvancementManager
import net.casual.championships.uhc.advancement.UHCAdvancements
import net.casual.championships.uhc.boundary.UHCBoundary
import net.casual.championships.uhc.minigame.modifier.UHCModifiers
import net.casual.championships.uhc.ui.UHCHud
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension.Companion.sharedHealthExtension
import net.casual.championships.uhc.ui.gui.UHCMapRenderer
import net.casual.championships.uhc.item.TMCStarterPack
import net.casual.championships.uhc.minigame.UHCPhase.GameOver
import net.casual.championships.uhc.minigame.UHCPhase.Initializing
import net.casual.championships.uhc.recipe.FlowerPowerRecipe
import net.casual.championships.uhc.recipe.HeavyCoreRecipe
import net.casual.championships.uhc.utils.UHCMinigameRules
import net.casual.championships.uhc.utils.UHCStats
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.GameType
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.io.path.createDirectories

class UHCMinigame(
    server: MinecraftServer,
    uuid: UUID,
    nerfedPlayers: Set<UUID>,
    private val dimensions: VanillaLikeLevels,
    private val factory: UHCMinigameFactory? = null
): Minigame(server, uuid), MinigameRulesProvider by UHCMinigameRules, TimeTrackedMinigame {
    override val id = ID

    val boundary = UHCBoundary(this)
    val mapRenderer = UHCMapRenderer(this)
    val uhcAdvancements = UHCAdvancementManager(this)

    private val hud = UHCHud(this)
    private val spectators = UHCSpectators(this)
    private val modifiers = UHCModifiers(this)

    override val settings = UHCSettings(this)
    override val timeTracker = CasualTimeTracker()

    val overworld: ServerLevel
        get() = this.dimensions.getOrThrow(VanillaDimension.Overworld)
    val nether: ServerLevel
        get() = this.dimensions.getOrThrow(VanillaDimension.Nether)
    val end: ServerLevel
        get() = this.dimensions.getOrThrow(VanillaDimension.End)

    init {
        this.tickrate.useGlobalManager = false

        this.visuals.addBossbar(ActiveBossbar.create(this))
        this.effects.setGlowingPredicate(PlayerObserverPredicate(this::shouldObserveeGlow))
        this.effects.setInvisiblePredicate(PlayerObserverPredicate(this::shouldObserveeBeInvisible))

        this.levels.addAll(this.dimensions.all())

        for (player in nerfedPlayers) {
            this.tags.add(player, UHCModifiers.NERFED)
        }
    }

    fun resetPlayerHealth(player: ServerPlayer) {
        player.boostHealth(this.settings.health)
        player.resetHealth()

        val team = player.team
        if (team != null) {
            val extension = team.sharedHealthExtension
            if (this.settings.sharingIsCaring) {
                extension.enabled = true
                extension.maxHealth = (20 * (this.settings.health + 1.0)).toFloat()
            } else {
                extension.enabled = false
            }
        }
    }

    fun weAreInTheEndgameNow() {
        for (player in this.players) {
            player.sendSound(CasualSounds.GAME_GRACE_END)
        }
        if (this.settings.endGameGlow) {
            this.settings.glowing = true
        }

        if (this.settings.generatePortals) {
            val overworldSurface = this.overworld.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0)
            this.overworld.portalForcer.createPortal(BlockPos(0, overworldSurface, 0), Direction.Axis.X)
            this.nether.portalForcer.createPortal(BlockPos(0, 64, 0), Direction.Axis.X)
        }
    }

    override fun phases(): Collection<UHCPhase> {
        return UHCPhase.entries
    }

    override fun factory(): MinigameFactory? {
        return this.factory
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun load(input: ValueInput) {
        this.uhcAdvancements.deserialize(input.childOrEmpty("advancements"))
        this.boundary.deserialize(input.childOrEmpty("boundary"))
    }

    override fun save(output: ValueOutput) {
        this.uhcAdvancements.serialize(output.child("advancements"))
        this.boundary.serialize(output.child("boundary"))
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.commands.register(UHCMinigameCommands(this))

        this.addEventListener(this.uhcAdvancements)
        this.addEventListener(this.boundary)
        this.addEventListener(this.hud)
        this.addEventListener(this.spectators)
        this.addEventListener(this.modifiers)
        this.recipes.add(GoldenHeadRecipe.INSTANCE)
        if (this.settings.heavyHeads) {
            this.recipes.add(HeavyCoreRecipe.INSTANCE)
        }
        if (this.settings.flowerPower) {
            this.recipes.add(FlowerPowerRecipe.getOrCreate(this.server.registryAccess()))
        }
        this.advancements.addAll(UHCAdvancements)
        this.settings.enableChatCommand.set(true)

        this.levels.spawn = UHCSpawnLocation(this)

        this.visuals.setSidebar(this.hud.createSidebar())
    }

    @Listener(priority = -2000)
    private fun onMinigameClose(event: MinigameCloseEvent) {
        for (team in this.teams.getPlayingTeams()) {
            team.sharedHealthExtension.enabled = false
        }
    }

    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        this.mapRenderer.update(this.overworld)
        this.mapRenderer.update(this.nether)
        this.mapRenderer.update(this.end)
    }

    @Listener(flags = ListenerFlags.HAS_PLAYER)
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.lastDeathLocation.ifPresent { pos ->
            val level = player.server.getLevel(pos.dimension)
            if (level != null && this.levels.has(level)) {
                val location = Vec3.atCenterOf(pos.pos).with(player.rotationVector).with(level)
                player.teleportTo(location)
            }
        }
    }

    @Listener
    private fun onMinigamePlayerRemoved(event: MinigameRemovePlayerEvent) {
        val player = event.player
        player.unboostHealth()
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()
        player.stopRiding()
        ReplayPlayerRecorders.get(player).forEach { it.stop() }
    }

    @Listener(flags = IS_PLAYING, phase = BuiltInEventPhases.POST)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val (player, source) = event

        GlobalTickedScheduler.Server.schedule(1.Seconds) {
            ReplayPlayerRecorders.get(player).forEach { it.stop() }
        }

        this.onEliminated(player, player.getKillCreditWith(source))
    }

    @Listener(phase = BuiltInEventPhases.POST)
    private fun onPlayerJump(event: PlayerJumpEvent) {
        this.stats.getOrCreateStat(event.player, CasualStats.JUMPS).increment()
    }

    @Listener
    private fun onPlayerUseItem(event: PlayerItemUseEvent) {
        if (event.stack.isOf(CasualItems.PLAYER_HEAD) || event.stack.isOf(CasualItems.GOLDEN_HEAD)) {
            this.stats.getOrCreateStat(event.player, UHCStats.HEADS_CONSUMED).increment()
        }
    }

    @Listener
    private fun onMinigameAddNewPlayer(event: MinigameAddNewPlayerEvent) {
        if (this.phase > Initializing) {
            event.spectating = true
        }
    }

    @Listener(priority = 0)
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val (_, player) = event

        if (player.team == null) {
            event.spectating = true
        }

        GlobalTickedScheduler.Server.schedule(1.Seconds) {
            // This is kinda a hack fix, but we need it, so the client renders the world border
            player.connection.send(ClientboundTickingStepPacket(1))
            // Needed for updating the player's health
            player.resetSentInfo()
        }
    }

    @Listener
    private fun onLoadPlaying(event: MinigameLoadPlayingEvent) {
        val player = event.player
        this.mapRenderer.stopWatching(player)

        if (!ReplayPlayerRecorders.has(player) && this.settings.replay) {
            val directory = CasualUtils.resolve("replays")
                .resolve("uhc")
                .resolve(player.scoreboardName)
                .createDirectories()
            ReplayPlayerRecorders.create(player, directory).start()
        }
    }

    @Listener
    private fun onMinigameRemovePlayer(event: MinigameRemovePlayerEvent) {
        val player = event.player
        player.isInvisible = false
        player.removeCustomInventory()
    }

    @Listener
    private fun onSetPlaying(event: MinigameSetPlayingEvent) {
        val player = event.player
        player.removeCustomInventory()
        player.isInvisible = false
        player.closeContainer()

        player.grantAllRecipesSilently()
        this.recipes.grantAll(player)

        player.revokeAllAdvancements()
        player.grantAdvancement(UHCAdvancements.ROOT)

        this.resetPlayerHealth(player)
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()
        player.removeAllEffects()

        player.removeVehicle()
        player.setGlowingTag(false)

        this.effects.addFullbright(player)

        val team = player.team
        if (team != null) {
            team.nameTagVisibility = Team.Visibility.NEVER
            team.collisionRule = Team.CollisionRule.ALWAYS
        }

        this.tags.add(player, CasualTags.HAS_PARTICIPATED)
        this.tags.add(player, CasualTags.HAS_TEAM_GLOW)

        player.setGameMode(GameType.SURVIVAL)
        player.isInvulnerable = true
        val task = PlayerTask(player) { it.isInvulnerable = false }
        GlobalTickedScheduler.Server.schedule(10.Seconds, task)
        player.afterPacksLoad(task::run)

        if (team != null) {
            this.teams.removeEliminatedTeam(team)
            if (team.getOnlineCount() == 1) {
                this.scheduler.schedule(1.Seconds) {
                    player.grantAdvancement(UHCAdvancements.SOLOIST)
                }
            }
        }

        if (this.settings.headStart) {
            player.inventory.add(ItemStack(Items.STONE_PICKAXE))
            player.inventory.add(ItemStack(Items.STONE_AXE))
            player.inventory.add(ItemStack(Items.BUNDLE))
            player.inventory.add(ItemStack(Items.APPLE, 5))
        }

        if (this.settings.tmcStarterPack) {
            player.inventory.add(TMCStarterPack.create())
        }

        if (this.settings.lawsOfAviation) {
            val elytra = ItemStack(Items.ELYTRA)
            elytra.set(DataComponents.DAMAGE, 431)
            elytra.enchant(this.server.registryAccess().getOrThrow(Enchantments.MENDING), 1)
            player.inventory.add(elytra)
        }
    }

    @Listener
    private fun onPlayerTeamJoin(event: PlayerTeamJoinEvent) {
        val (player, team) = event
        for (teammate in team.getOnlinePlayers()) {
            this.effects.forceUpdate(teammate, player)
            this.effects.forceUpdate(player, teammate)
        }
    }

    @Listener
    private fun onPlayerTeamLeave(event: PlayerTeamLeaveEvent) {
        val (player, team) = event
        for (teammate in team.getOnlinePlayers()) {
            this.effects.forceUpdate(teammate, player)
            this.effects.forceUpdate(player, teammate)
        }
    }

    @Listener
    private fun onPlayerCheat(event: PlayerCheatEvent) {
        val message = Component {
            literal("Player ") + event.player.displayName + literal(" tried to cheat with ${event.type}")
        }
        this.chat.broadcastInfo(message, this.players.admins)
    }

    private fun onEliminated(player: ServerPlayer, killer: Entity?) {
        this.modifiers.onPlayerEliminated(player, killer)

        this.players.setSpectating(player)

        val team = player.team
        if (team !== null && !this.teams.isTeamEliminated(team) && team.getOnlinePlayers().none(this.players::isPlaying)) {
            this.teams.addEliminatedTeam(team)
            this.chat.broadcastWithSound(
                CasualComponents.HAS_BEEN_ELIMINATED.generate(team.name).color(team).bold().withMiniFont(),
                Sound(CasualSounds.TEAM_ELIMINATION)
            )
        }

        if (this.teams.getPlayingTeams().size <= 1) {
            this.setPhase(GameOver)
        }
    }

    private fun shouldObserveeGlow(observee: ServerPlayer, observer: ServerPlayer): Boolean {
        if (this.players.isSpectating(observee)) {
            return false
        }
        if (this.settings.glowing) {
            return true
        }
        if (!this.settings.friendlyPlayerGlow) {
            return false
        }
        if (!this.tags.has(observer, CasualTags.HAS_TEAM_GLOW)) {
            return false
        }
        return observee.team != null && observee.team === observer.team
    }

    private fun shouldObserveeBeInvisible(observee: ServerPlayer, observer: ServerPlayer): Boolean {
        return this.players.isSpectating(observee) && observee !== observer
    }

    companion object {
        val ID = casual("uhc_minigame")
    }
}