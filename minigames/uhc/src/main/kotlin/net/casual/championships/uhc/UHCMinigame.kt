package net.casual.championships.uhc

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import me.senseiwells.replay.player.PlayerRecorders
import net.casual.arcade.border.MultiLevelBorderListener
import net.casual.arcade.border.MultiLevelBorderTracker
import net.casual.arcade.border.TrackedBorder
import net.casual.arcade.entity.player.ExtendedGameMode
import net.casual.arcade.entity.player.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.block.BrewingStandBrewEvent
import net.casual.arcade.events.level.LevelLootEvent
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.*
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.predicate.PlayerObserverPredicate
import net.casual.arcade.gui.shapes.ArrowShape
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_PLAYING
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.*
import net.casual.arcade.utils.CommandUtils.argument
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.literal
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.withMiniShiftedDownFont
import net.casual.arcade.utils.FantasyUtils.PersistentConfig
import net.casual.arcade.utils.ItemUtils.isOf
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.longOrNull
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.casual.arcade.utils.MathUtils.opposite
import net.casual.arcade.utils.MinigameUtils.addEventListener
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.PlayerUtils.boostHealth
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.directionToNearestBorder
import net.casual.arcade.utils.PlayerUtils.directionVectorToNearestBorder
import net.casual.arcade.utils.PlayerUtils.getKillCreditWith
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.grantAllRecipesSilently
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.revokeAllAdvancements
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.PlayerUtils.unboostHealth
import net.casual.arcade.utils.ShapeUtils.drawAsParticlesFor
import net.casual.arcade.utils.StatUtils.increment
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.location.Location
import net.casual.championships.common.event.TippedArrowTradeOfferEvent
import net.casual.championships.common.event.border.BorderEntityPortalEntryPointEvent
import net.casual.championships.common.event.border.BorderPortalWithinBoundsEvent
import net.casual.championships.common.items.PlayerHeadItem
import net.casual.championships.common.minigame.rules.Rules
import net.casual.championships.common.minigame.rules.RulesProvider
import net.casual.championships.common.recipes.GoldenHeadRecipe
import net.casual.championships.common.task.GlowingBossBarTask
import net.casual.championships.common.task.GracePeriodBossBarTask
import net.casual.championships.common.ui.bossbar.ActiveBossBar
import net.casual.championships.common.util.*
import net.casual.championships.common.util.CommonUI.broadcastGame
import net.casual.championships.common.util.CommonUI.broadcastInfo
import net.casual.championships.common.util.CommonUI.broadcastWithSound
import net.casual.championships.common.util.RuleUtils.addRule
import net.casual.championships.uhc.UHCPhase.*
import net.casual.championships.uhc.advancement.UHCAdvancementManager
import net.casual.championships.uhc.advancement.UHCAdvancements
import net.casual.championships.uhc.border.UHCBorderSize
import net.casual.championships.uhc.border.UHCBorderStage
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.phys.HitResult
import net.minecraft.world.scores.Team
import kotlin.math.abs
import kotlin.math.atan2

class UHCMinigame(
    server: MinecraftServer,
    val overworld: ServerLevel,
    val nether: ServerLevel,
    val end: ServerLevel
): SavableMinigame<UHCMinigame>(server), MultiLevelBorderListener, RulesProvider {
    private val tracker = MultiLevelBorderTracker()
    private var movingBorders = HashSet<ResourceKey<Level>>()

    override val id = ID

    val mapRenderer = UHCMapRenderer(this)
    val uhcAdvancements = UHCAdvancementManager(this)
    val winners = HashSet<String>()

    override val settings = UHCSettings(this)

    init {
        this.addTaskFactory(GlowingBossBarTask.cast())
        this.addTaskFactory(GracePeriodBossBarTask.cast())

        this.ui.addBossbar(ActiveBossBar(this))
        this.effects.setGlowingPredicate(PlayerObserverPredicate(this::shouldObserveeGlow))
        this.effects.setInvisiblePredicate(PlayerObserverPredicate(this::shouldObserveeBeInvisible))
    }

    override fun initialize() {
        super.initialize()

        this.registerCommands()
        this.addEventListener(this.uhcAdvancements)
        this.recipes.add(GoldenHeadRecipe.INSTANCE)
        this.advancements.addAll(UHCAdvancements)
        this.initialiseBorderTracker()

        this.levels.add(this.overworld)
        this.levels.add(this.nether)
        this.levels.add(this.end)
        this.levels.spawn = MinigameLevelManager.SpawnLocation.global(this.overworld)
    }

    override fun getPhases(): Collection<UHCPhase> {
        return entries
    }

    override fun loadData(json: JsonObject) {
        this.movingBorders = HashSet(json.arrayOrDefault("moving_borders").strings().map {
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(it))
        })
        if (json.has("advancements")) {
            this.uhcAdvancements.deserialize(json.obj("advancements"))
        }
    }

    override fun saveData(json: JsonObject) {
        json.add("advancements", this.uhcAdvancements.serialize())
        json.add("moving_borders", this.movingBorders.toJsonStringArray { it.location().toString() })
    }

    override fun saveParameters(json: JsonObject) {
        val dimensions = JsonObject()
        dimensions.addProperty("overworld", this.overworld.dimension().location().toString())
        dimensions.addProperty("nether", this.nether.dimension().location().toString())
        dimensions.addProperty("end", this.end.dimension().location().toString())
        json.add("dimensions", dimensions)
        json.addProperty("seed", this.overworld.seed)
    }

    @Listener(during = During(before = BORDER_FINISHED_ID))
    private fun onPause(event: MinigamePauseEvent) {
        for ((border, level) in tracker.getAllTracking()) {
            if (this.movingBorders.contains(level.dimension())) {
                this.moveWorldBorder(border, border.size)
            }
        }
    }

    @Listener(during = During(before = BORDER_FINISHED_ID))
    private fun onUnpause(event: MinigameUnpauseEvent) {
        for ((border, level) in tracker.getAllTracking()) {
            if (this.movingBorders.contains(level.dimension())) {
                this.moveWorldBorder(border, level, this.settings.borderStage, UHCBorderSize.End)
            }
        }
    }

    @Listener
    private fun onBrewingStandBrew(event: BrewingStandBrewEvent) {
        if (!this.settings.opPotions) {
            val ingredient = event.entity.getItem(3)
            if (ingredient.isOf(Items.GLOWSTONE_DUST) || ingredient.isOf(Items.GLISTERING_MELON_SLICE)) {
                event.cancel()
            }
        }
    }

    @Listener
    private fun onBorderEntityPortalEntryPointEvent(event: BorderEntityPortalEntryPointEvent) {
        val (border, _, _, pos) = event

        // Blocks per millisecond
        val shrinkingSpeed = border.lerpSpeed
        if (shrinkingSpeed <= 0) {
            // The border is static or expanding
            return
        }
        val margin = shrinkingSpeed * this.settings.portalEscapeTime.milliseconds
        if (margin >= border.size * 0.5) {
            // The border would reach size 0 within 30 seconds
            event.cancel(BlockPos.containing(border.centerX, pos.y, border.centerZ))
            return
        }

        event.cancel(BlockPos.containing(
            Mth.clamp(pos.x, border.minX + margin, border.maxX - margin),
            pos.y,
            Mth.clamp(pos.z, border.minZ + margin, border.maxZ - margin)
        ))
    }

    @Listener
    private fun onBorderWithinBoundsEvent(event: BorderPortalWithinBoundsEvent) {
        val (border, _, pos) = event
        // Blocks per millisecond
        val shrinkingSpeed = border.lerpSpeed
        if (shrinkingSpeed <= 0) {
            // The border is static or expanding
            return
        }
        var margin = shrinkingSpeed * this.settings.portalEscapeTime.milliseconds
        margin = margin.coerceAtMost(border.size * 0.5 - 1)
        event.cancel(
            pos.x >= border.minX + margin
                && pos.x + 1 <= border.maxX - margin
                && pos.z >= border.minZ + margin
                && pos.z + 1 <= border.maxZ - margin
        )
    }

    @Listener
    private fun onServerTick(event: ServerTickEvent) {
        this.mapRenderer.update(this.overworld)
        this.mapRenderer.update(this.nether)
        this.mapRenderer.update(this.end)
    }

    @Listener(during = During(before = GAME_OVER_ID))
    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event

        if (!this.players.isSpectating(player)) {
            this.updateWorldBorder(player)
        }

        this.updateHUD(player)
    }

    @Listener
    private fun onLevelLoot(event: LevelLootEvent) {
        for (item in event.items) {
            val contents = item.get(DataComponents.POTION_CONTENTS) ?: continue
            val potion = contents.potion
            if (potion.isPresent && potion.get().value() == Potions.HEALING.value()) {
                val newContents = contents.withPotion(Potions.REGENERATION)
                item.set(DataComponents.POTION_CONTENTS, newContents)
            }
        }
    }

    @Listener(flags = IS_PLAYING, phase = BuiltInEventPhases.POST)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val (player, source) = event

        GlobalTickedScheduler.schedule(1.Seconds) {
            PlayerRecorders.get(player)?.stop()
        }

        this.onEliminated(player, player.getKillCreditWith(source))
    }

    @Listener(flags = ListenerFlags.HAS_PLAYER)
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.setGameMode(GameType.SPECTATOR)

        player.lastDeathLocation.ifPresent { location ->
            val level = player.server.getLevel(location.dimension)
            if (level != null && this.levels.has(level)) {
                val pos = location.pos
                player.teleportTo(Location.of(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), level = level))
            }
        }
    }

    @Listener
    private fun onTippedArrowTradeOffer(event: TippedArrowTradeOfferEvent) {
        event.potion = when (event.potion.value()) {
            Potions.HEALING.value(), Potions.STRONG_HEALING.value(), Potions.STRONG_REGENERATION.value() -> Potions.REGENERATION
            Potions.STRONG_POISON.value() -> Potions.POISON
            Potions.STRONG_HARMING.value() -> Potions.HARMING
            Potions.STRONG_LEAPING.value() -> Potions.LEAPING
            Potions.STRONG_SLOWNESS.value() -> Potions.SLOWNESS
            Potions.STRONG_STRENGTH.value() -> Potions.STRENGTH
            Potions.STRONG_TURTLE_MASTER.value() -> Potions.TURTLE_MASTER
            else -> return
        }
    }

    @Listener
    private fun onMinigamePlayerRemoved(event: MinigameRemovePlayerEvent) {
        val player = event.player
        player.unboostHealth()
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()
        PlayerRecorders.get(player)?.stop()
    }

    @Listener
    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        val (player, stack) = event
        if (stack.isOf(Items.BOW)) {
            player.cooldowns.addCooldown(Items.BOW, this.settings.bowCooldown.ticks)
        }
    }

    @Listener
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        if (player.isSpectator) {
            event.cancel()
        }
    }

    @Listener(phase = BuiltInEventPhases.POST)
    private fun onPlayerJump(event: PlayerJumpEvent) {
        this.stats.getOrCreateStat(event.player, CommonStats.JUMPS).increment()
    }

    @Listener
    private fun onPlayerUseItem(event: PlayerItemUseEvent) {
        if (event.stack.isOf(CommonItems.PLAYER_HEAD) || event.stack.isOf(CommonItems.GOLDEN_HEAD)) {
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

        // Needed for updating the player's health
        GlobalTickedScheduler.schedule(1.Seconds, player::resetSentInfo)
    }

    @Listener
    private fun onMinigameRemovePlayer(event: MinigameRemovePlayerEvent) {
        val player = event.player
        player.isInvisible = false
    }

    @Listener
    private fun onSetPlaying(event: MinigameSetPlayingEvent) {
        val player = event.player
        player.isInvisible = false
        player.closeContainer()

        player.grantAllRecipesSilently()

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
        team?.nameTagVisibility = Team.Visibility.NEVER
        team?.collisionRule = Team.CollisionRule.ALWAYS

        this.tags.add(player, CommonTags.HAS_PARTICIPATED)
        this.tags.add(player, CommonTags.HAS_TEAM_GLOW)

        player.addEffect(
            MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4, true, false)
        )
        player.setGameMode(GameType.SURVIVAL)

        if (team != null && team.getOnlineCount() == 1) {
            player.grantAdvancement(UHCAdvancements.SOLOIST)
        }
    }

    @Listener
    private fun onLoadPlaying(event: MinigameLoadPlayingEvent) {
        val player = event.player
        this.mapRenderer.stopWatching(player)

        if (!PlayerRecorders.has(player) && this.settings.replay) {
            PlayerRecorders.create(player).start()
        }
    }

    @Listener
    private fun onSetSpectating(event: MinigameSetSpectatingEvent) {
        val (_, player) = event
        player.extendedGameMode = ExtendedGameMode.AdventureSpectator

        this.effects.addFullbright(player)
        this.tags.remove(player, CommonTags.HAS_TEAM_GLOW)

        if (!this.levels.has(player.serverLevel())) {
            player.teleportTo(Location.of(0.0, 128.0, 0.0, level = this.overworld))
        }

        this.chat.broadcastInfo(UHCComponents.BROADCAST_SPECTATING.mini(), listOf(player))
    }

    @Listener
    private fun onLoadSpectating(event: MinigameLoadSpectatingEvent) {
        UHCSpectatorHotbar(event.player, this).open()
        this.mapRenderer.startWatching(event.player)
    }

    @Listener(flags = ListenerFlags.IS_SPECTATOR)
    private fun onPlayerSneak(event: PlayerSetSneakingEvent) {
        val (player, sneaking) = event
        if (sneaking) {
            val last = this.stats.getOrCreateStat(player, UHCStats.LAST_SNEAK_TIME)
            if (abs(this.server.tickCount - last.value) < 7) {
                val mode = when (player.extendedGameMode) {
                    ExtendedGameMode.AdventureSpectator -> ExtendedGameMode.NoClipSpectator
                    else -> ExtendedGameMode.AdventureSpectator
                }
                player.extendedGameMode = mode
            } else {
                last.modify { this.server.tickCount }
            }
        }
    }

    private fun onEliminated(player: ServerPlayer, killer: Entity?) {
        if (killer is ServerPlayer) {
            this.onKilled(killer, player)
        }

        if (this.settings.playerDropsGapple) {
            player.drop(Items.GOLDEN_APPLE.defaultInstance, true, false)
        }

        if (this.settings.playerDropsHead) {
            val head = PlayerHeadItem.create(player)
            if (killer is ServerPlayer) {
                if (!killer.inventory.add(head)) {
                    player.drop(head, true, false)
                }
            } else {
                player.drop(head, true, false)
            }
        }

        this.players.setSpectating(player)

        val team = player.team
        if (team !== null && !this.teams.isTeamEliminated(team) && team.getOnlinePlayers().none(this.players::isPlaying)) {
            this.teams.addEliminatedTeam(team)
            this.chat.broadcastWithSound(
                CommonComponents.HAS_BEEN_ELIMINATED.generate(team.name).withStyle(team.color).bold().mini(),
                Sound(CommonSounds.TEAM_ELIMINATION)
            )
        }

        if (this.teams.getPlayingTeams().size <= 1) {
            this.setPhase(GameOver)
        }
    }

    private fun onKilled(player: ServerPlayer, killed: ServerPlayer) {
        val opposing = killed.team ?: return
        if (this.settings.soloBuff) {
            // Opposing team has many players
            if (opposing.getOnlinePlayers().count(this.players::isPlaying) > 0) {
                val team = player.team ?: return
                // Killer is solo
                if (team.getOnlinePlayers().count(this.players::isPlaying) == 1) {
                    player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 60, 2))
                }
            }
        }
    }

    // Rules

    override fun getRules(): Rules {
        return Rules.build {
            addRule("uhc.rules.announcement", 1 to 9.Seconds)
            addRule("uhc.rules.mods", 3)
            addRule("uhc.rules.exploits", 1)
            addRule("uhc.rules.gameplay", 2, 5)
            addRule("uhc.rules.chat", 2)
            addRule("uhc.rules.spectators", 1)
            addRule("uhc.rules.gentlemansRules", 1)
            rule {
                title = RuleUtils.formatTitle(Component.translatable("uhc.rules.reminders"))
                entry {
                    val teamglow = "/uhc teamglow".literal().mini().bold().colour(0x65b7db)
                    val fullbright = "/uhc fullbright".literal().mini().bold().colour(0x65b7db)
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.1", teamglow)))
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.2", fullbright)))
                }
                entry {
                    val prefix = "!".literal().mini().bold().colour(0x65b7db)
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.3", prefix)))
                }
            }
            addRule("uhc.rules.questions", 1)
            addRule("uhc.rules.finally", 1 to 9.Seconds)
        }
    }

    // World Border

    fun startWorldBorders() {
        this.moveWorldBorders(this.settings.borderStage)
    }

    fun resetWorldBorders() {
        val multiplier = this.settings.borderSizeMultiplier
        for ((border, level) in this.tracker.getAllTracking()) {
            border.setSizeUntracked(UHCBorderStage.First.getStartSizeFor(level, multiplier))
        }
        this.movingBorders.clear()
    }

    fun moveWorldBorders(stage: UHCBorderStage, size: UHCBorderSize = UHCBorderSize.End, instant: Boolean = false) {
        for ((border, level) in this.tracker.getAllTracking()) {
            this.moveWorldBorder(border, level, stage, size, instant)
        }
    }

    override fun onSingleBorderActive(border: TrackedBorder, level: ServerLevel) {
        this.movingBorders.add(level.dimension())
    }

    override fun onSingleBorderComplete(border: TrackedBorder, level: ServerLevel) {
        if (!this.paused) {
            this.movingBorders.remove(level.dimension())
        }
    }

    override fun onAllBordersComplete(borders: Map<TrackedBorder, ServerLevel>) {
        val stage = this.settings.borderStage
        val size = stage.getEndSizeFor(this.overworld, this.settings.borderSizeMultiplier)
        if (this.overworld.worldBorder.size != size) {
            UHCMod.logger.info("Border paused at stage $stage")
            return
        }

        UHCMod.logger.info("Finished world border stage: $stage")
        if (stage == UHCBorderStage.Fifth) {
            this.setPhase(BorderFinished)
            return
        }

        val delay = this.settings.borderTime * stage.getPausedTimeAsPercent()
        this.scheduler.schedulePhased(delay, MinigameTask(this, UHCMinigame::startNextBorders))
    }

    private fun startNextBorders() {
        this.settings.borderStageSetting.setQuietly(this.settings.borderStage.getNextStage())
        this.moveWorldBorders(this.settings.borderStage)

        this.chat.broadcastGame(
            component = CommonComponents.BORDER_RESUMED.mini().red(),
            sound = Sound(CommonSounds.GAME_BORDER_MOVING)
        )
    }

    fun onBorderFinish() {
        for (player in this.players) {
            player.sendSound(CommonSounds.GAME_GRACE_END)
        }
        if (this.settings.endGameGlow) {
            this.settings.glowing = true
        }
        if (this.settings.generatePortals) {
            this.overworld.portalForcer.createPortal(BlockPos(0, 100, 0), Direction.Axis.X)
            this.nether.portalForcer.createPortal(BlockPos(0, 100, 0), Direction.Axis.X)
        }
    }

    fun getCurrentBorderSizeFor(level: Level, size: UHCBorderSize): Double {
        val stage = this.settings.borderStage
        val modified =  if (level == this.end && stage >= UHCBorderStage.Fifth) UHCBorderStage.Fourth else stage
        val multiplier = this.settings.borderSizeMultiplier
        return if (size == UHCBorderSize.End) {
            modified.getEndSizeFor(level, multiplier)
        } else {
            modified.getStartSizeFor(level, multiplier)
        }
    }

    private fun initialiseBorderTracker() {
        this.tracker.addListener(this)
        for (level in this.levels.all()) {
            this.tracker.addLevelBorder(level)
        }

        BorderUtils.isolateWorldBorders()
    }

    private fun moveWorldBorder(border: TrackedBorder, level: Level, stage: UHCBorderStage, size: UHCBorderSize, instant: Boolean = false) {
        val modified =  if (level == this.end && stage >= UHCBorderStage.Fifth) UHCBorderStage.Fourth else stage
        val multiplier = this.settings.borderSizeMultiplier
        val dest = if (size == UHCBorderSize.End) {
            modified.getEndSizeFor(level, multiplier)
        } else {
            modified.getStartSizeFor(level, multiplier)
        }
        val time = if (instant) -1.0 else modified.getRemainingMovingTimeAsPercent(border.size, level, multiplier)

        UHCMod.logger.info("Level ${level.dimension().location()} moving to $dest")
        moveWorldBorder(border, dest, time)
    }

    private fun moveWorldBorder(border: TrackedBorder, newSize: Double, percent: Double = -1.0) {
        val duration = this.settings.borderTime * percent
        if (!duration.isZero) {
            border.lerpSizeBetween(border.size, newSize, duration)
            return
        }
        border.size = newSize
    }

    private fun updateWorldBorder(player: ServerPlayer) {
        val level = player.level()
        val border = level.worldBorder

        val worldBorderTime = this.stats.getOrCreateStat(player, UHCStats.WORLD_BORDER_TIME)
        if (border.isWithinBounds(player.boundingBox)) {
            if (worldBorderTime.value > 0) {
                worldBorderTime.modify { 0 }
                player.connection.send(ClientboundInitializeBorderPacket(border))
            }
            return
        }
        worldBorderTime.increment()

        val vector = player.directionVectorToNearestBorder()

        val start = player.eyePosition.add(0.0, 4.0, 0.0)
        val end = start.add(vector.normalize())

        for (i in 1..2) {
            val top = start.lerp(end, 1.5 * i)
            val bottom = top.subtract(0.0, 10.0, 0.0)
            val hit = level.clip(ClipContext(top, bottom, ClipContext.Block.VISUAL, ClipContext.Fluid.SOURCE_ONLY, player))

            if (hit.type != HitResult.Type.MISS) {
                val position = hit.blockPos
                val rotation = atan2(vector.x, vector.z)

                val arrow = ArrowShape.createHorizontalCentred(position.x, hit.location.y + 0.1, position.z, 1.0, rotation)
                arrow.drawAsParticlesFor(player)
            }
        }

        val direction = player.directionToNearestBorder()
        val fakeDirection = direction.opposite()

        val fakeCenterX = border.centerX + fakeDirection.stepX * border.size
        val fakeCenterZ = border.centerZ + fakeDirection.stepZ * border.size

        val scale = level.dimensionType().coordinateScale

        FAKE_BORDER.size = border.size + 0.6
        FAKE_BORDER.lerpSizeBetween(FAKE_BORDER.size, FAKE_BORDER.size - 0.5, Long.MAX_VALUE)
        // Foolish Minecraft uses scale for the centre, even on the client,
        // so we need to reproduce.
        FAKE_BORDER.setCenter(fakeCenterX * scale, fakeCenterZ * scale)
        player.connection.send(ClientboundInitializeBorderPacket(FAKE_BORDER))

        if (this.uptime % 200 == 0) {
            player.sendTitle(
                Component.empty(),
                CommonComponents.INSIDE_BORDER.generate(CommonComponents.direction(direction).lime()).mini()
            )
        }
    }

    private fun updateHUD(player: ServerPlayer) {
        if (this.players.isSpectating(player)) {
            return
        }

        val direction = CommonComponents.direction(Direction.orderedByNearest(player).filter { it.axis != Direction.Axis.Y }[0])
        val back = ComponentUtils.negativeWidthOf(direction)
        val position = "(${player.blockX}, ${player.blockY}, ${player.blockZ})"
        val shift = position.length - 7

        player.connection.send(ClientboundSetActionBarTextPacket(
            Component.empty().apply {
                append(ComponentUtils.space(215 + shift * 6))
                append(direction.withMiniShiftedDownFont(6))
                append(back)
                append(ComponentUtils.space(-2))
                append(position.literal().withMiniShiftedDownFont(7))
            }
        ))
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
        if (!this.tags.has(observer, CommonTags.HAS_TEAM_GLOW)) {
            return false
        }
        return observee.team != null && observee.team === observer.team
    }

    private fun shouldObserveeBeInvisible(observee: ServerPlayer, observer: ServerPlayer): Boolean {
        return this.players.isSpectating(observee) && observee !== observer
    }

    private fun resetPlayerHealth(player: ServerPlayer) {
        player.boostHealth(this.settings.health)
        player.resetHealth()
    }

    // Commands

    private fun registerCommands() {
        this.commands.register(CommandUtils.buildLiteral("uhc") {
            literal("player") {
                requiresAdminOrPermission()
                argument("player", EntityArgument.player()) {
                    literal("add") {
                        argument("team", TeamArgument.team()) {
                            argument("teleport", BoolArgumentType.bool()) {
                                executes(::addPlayerToTeam)
                            }
                            executes { addPlayerToTeam(it, false) }
                        }
                    }
                    literal("reset-health") {
                        executes(::resetPlayerHealth)
                    }
                }
            }
            literal("border") {
                requiresAdminOrPermission()
                literal("start") {
                    executes(::startWorldBorders)
                }
            }
            literal("map") {
                requiresAdminOrPermission()
                literal("give") {
                    executes { mapRenderer.getMaps().forEach(it.source.playerOrException::addItem).commandSuccess() }
                }
                literal("clear") {
                    executes { mapRenderer.clear().commandSuccess() }
                }
            }
            literal("fullbright") {
                executes { CommonCommands.toggleFullbright(this@UHCMinigame, it) }
            }
            literal("teamglow") {
                executes { CommonCommands.toggleTeamGlow(this@UHCMinigame, it) }
            }
            literal("spectate") {
                executes { CommonCommands.openSpectatingScreen(this@UHCMinigame, it) }
                argument("player", EntityArgument.player()) {
                    executes(::teleportToPlayer)
                }
            }
            literal("pos") {
                executes { CommonCommands.broadcastPositionToTeammates(this@UHCMinigame, it) }
            }
        })
        this.commands.register(CommandUtils.buildLiteral("s") {
            executes { CommonCommands.openSpectatingScreen(this@UHCMinigame, it) }
            argument("player", EntityArgument.player()) {
                executes(::teleportToPlayer)
            }
        })
    }

    private fun addPlayerToTeam(
        context: CommandContext<CommandSourceStack>,
        teleport: Boolean = BoolArgumentType.getBool(context, "teleport")
    ): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val team = TeamArgument.getTeam(context, "team")

        val server = context.source.server
        server.scoreboard.addPlayerToTeam(target.scoreboardName, team)
        target.sendSystemMessage(CommonComponents.ADDED_TO_TEAM.generate(team.formattedDisplayName))

        this.players.setPlaying(target)

        if (teleport) {
            for (player in this.players.playing) {
                if (team.players.contains(player.scoreboardName) && target != player) {
                    target.teleportTo(player.location)
                    break
                }
            }
        }

        val message = "${target.scoreboardName} has joined team ".literal()
            .append(team.formattedDisplayName)
            .append(" and has ${if (teleport) "been teleported to a random teammate" else "not been teleported"}")
        return context.source.success(message, true)
    }

    private fun resetPlayerHealth(context: CommandContext<CommandSourceStack>): Int {
        val target = EntityArgument.getPlayer(context, "player")
        this.resetPlayerHealth(target)
        return context.source.success("Successfully reset ${target.scoreboardName}'s health")
    }

    private fun startWorldBorders(context: CommandContext<CommandSourceStack>): Int {
        this.startWorldBorders()
        return context.source.success("Successfully started world borders")
    }

    private fun teleportToPlayer(context: CommandContext<CommandSourceStack>): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val player = context.source.playerOrException
        if (!this.players.isSpectating(player)) {
            return context.source.fail(CommonComponents.NOT_SPECTATING)
        }
        return player.teleportTo(target.location).commandSuccess()
    }

    companion object {
        private val FAKE_BORDER = WorldBorder()
        val ID = UHCMod.id("uhc_minigame")

        fun create(context: MinigameCreationContext): UHCMinigame {
            val parameters = context.parameters
            val dimensions = parameters.objOrNull("dimensions")
            val seed = parameters.longOrNull("seed") ?: WorldOptions.randomSeed()
            val permanent = parameters.booleanOrDefault("permanent")
            val levels = if (dimensions != null) {
                FantasyUtils.createPersistentVanillaLikeLevels(
                    context.server,
                    ResourceLocation.parse(dimensions.string("overworld")),
                    ResourceLocation.parse(dimensions.string("nether")),
                    ResourceLocation.parse(dimensions.string("end")),
                    seed
                )
            } else {
                FantasyUtils.createPersistentVanillaLikeLevels(
                    context.server,
                    ResourceUtils.random { "overworld_$it" },
                    ResourceUtils.random { "nether_$it" },
                    ResourceUtils.random { "end_$it" },
                    seed
                )
            }
            val uhc = UHCMinigame(context.server, levels.overworld, levels.nether, levels.end)
            uhc.levels.add(levels.overworldHandle)
            uhc.levels.add(levels.netherHandle)
            uhc.levels.add(levels.endHandle)
            if (permanent) {
                uhc.settings.shouldDeleteLevels = false
            }
            return uhc
        }
    }
}