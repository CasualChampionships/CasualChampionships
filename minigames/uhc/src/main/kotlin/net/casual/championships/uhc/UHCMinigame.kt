package net.casual.championships.uhc

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.border.MultiLevelBorderListener
import net.casual.arcade.border.MultiLevelBorderTracker
import net.casual.arcade.border.TrackedBorder
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.block.BrewingStandBrewEvent
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.*
import net.casual.arcade.gui.shapes.ArrowShape
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.BorderUtils
import net.casual.arcade.utils.CommandUtils
import net.casual.arcade.utils.CommandUtils.argument
import net.casual.arcade.utils.CommandUtils.literal
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.withMiniShiftedDownFont
import net.casual.arcade.utils.ItemUtils.isOf
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.set
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
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.phys.HitResult
import net.minecraft.world.scores.Team
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import kotlin.math.atan2

class UHCMinigame(
    server: MinecraftServer,
    val overworld: ServerLevel,
    val nether: ServerLevel,
    val end: ServerLevel
): SavableMinigame<UHCMinigame>(
    server,
), MultiLevelBorderListener, RulesProvider {
    private val tracker = MultiLevelBorderTracker()
    private var movingBorders = HashSet<ResourceKey<Level>>()

    override val id = ID

    val uhcAdvancements = UHCAdvancementManager(this)
    val winners = HashSet<String>()

    override val settings = UHCSettings(this)

    init {
        this.levels.add(this.overworld)
        this.levels.add(this.nether)
        this.levels.add(this.end)
        this.levels.spawn = MinigameLevelManager.SpawnLocation.global(this.overworld)

        this.addTaskFactory(GlowingBossBarTask.cast())
        this.addTaskFactory(GracePeriodBossBarTask.cast())

        this.ui.addBossbar(ActiveBossBar(this))
        this.effects.setGlowingPredicate(this::shouldObserveeGlow)
    }

    override fun initialize() {
        super.initialize()

        this.registerCommands()
        this.addEventListener(this.uhcAdvancements)
        this.recipes.add(GoldenHeadRecipe.INSTANCE)
        this.advancements.addAll(UHCAdvancements.getAllAdvancements())
        this.initialiseBorderTracker()
    }

    override fun getPhases(): Collection<UHCPhase> {
        return entries
    }

    override fun readData(json: JsonObject) {
        this.uhcAdvancements.deserialize(json.obj("advancements"))
        this.movingBorders = HashSet(json.arrayOrDefault("moving_borders").strings().map {
            ResourceKey.create(Registries.DIMENSION, ResourceLocation(it))
        })
    }

    override fun writeData(json: JsonObject) {
        json.add("advancements", this.uhcAdvancements.serialize())
        json.add("moving_borders", this.movingBorders.toJsonStringArray { it.location().toString() })
        val dimensions = JsonObject()
        dimensions["overworld"] = this.overworld.dimension().location().toString()
        dimensions["nether"] = this.nether.dimension().location().toString()
        dimensions["end"] = this.end.dimension().location().toString()
        json.add("dimensions", dimensions)
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

    @Listener(during = During(before = GAME_OVER_ID))
    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event

        if (!this.players.isSpectating(player)) {
            this.updateWorldBorder(player)

            this.stats.getOrCreateStat(player, CommonStats.ALIVE_TIME).increment()
            if (player.isShiftKeyDown) {
                this.stats.getOrCreateStat(player, CommonStats.CROUCH_TIME).increment()
            }
        }

        this.updateHUD(player)
    }

    @Listener
    private fun onPlayerBlockMined(event: PlayerBlockMinedEvent) {
        this.stats.getOrCreateStat(event.player, CommonStats.BLOCKS_MINED).increment()
    }

    @Listener(phase = BuiltInEventPhases.POST)
    private fun onPlayerBlockPlaced(event: PlayerBlockPlacedEvent) {
        this.stats.getOrCreateStat(event.player, CommonStats.BLOCKS_PLACED).increment()
    }

    @Listener(flags = ListenerFlags.HAS_PLAYER_PLAYING, phase = BuiltInEventPhases.POST)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val (player, source) = event

        // TODO: We can stop recording now...
        // GlobalTickedScheduler.schedule(1.Seconds) {
        //     PlayerRecorders.get(player)?.stop()
        // }

        this.onEliminated(player, player.getKillCreditWith(source))
    }

    @Listener
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.setGameMode(GameType.SPECTATOR)
    }

    @Listener
    private fun onMinigamePlayerRemoved(event: MinigameRemovePlayerEvent) {
        val player = event.player
        player.unboostHealth()
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()
        // PlayerRecorders.get(player)?.stop()
    }

    @Listener
    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        val (player, stack) = event
        if (stack.`is`(Items.BOW)) {
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
        } else {
            // GlobalTickedScheduler.later {
            //     if (!PlayerRecorders.has(player) && this.players.isPlaying(player) && this.settings.replay) {
            //         PlayerRecorders.create(player).start()
            //     }
            // }
        }

        // Needed for updating the player's health
        GlobalTickedScheduler.schedule(1.Seconds, player::resetSentInfo)
    }

    @Listener
    private fun onSetPlaying(event: MinigameSetPlayingEvent) {
        val player = event.player

        player.grantAllRecipesSilently()

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
    private fun onSetSpectating(event: MinigameSetSpectatingEvent) {
        val (_, player) = event
        player.setGameMode(GameType.SPECTATOR)

        this.effects.addFullbright(player)
        this.tags.remove(player, CommonTags.HAS_TEAM_GLOW)

        if (!this.levels.has(player.serverLevel())) {
            player.teleportTo(Location.of(0.0, 128.0, 0.0, level = this.overworld))
        }

        this.chat.broadcastInfo(UHCComponents.BROADCAST_SPECTATING.mini(), listOf(player))
    }

    private fun onEliminated(player: ServerPlayer, killer: Entity?) {
        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.yRot, true, false)
        this.players.setSpectating(player)

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
        val next = stage.getNextStage()

        if (next == UHCBorderStage.End) {
            this.setPhase(BorderFinished)
            return
        }

        this.scheduler.schedulePhased(10.Seconds, MinigameTask(this, UHCMinigame::startNextBorders))
    }

    private fun startNextBorders() {
        this.settings.borderStageSetting.setQuietly(this.settings.borderStage.getNextStage())
        this.moveWorldBorders(this.settings.borderStage)
    }

    fun onBorderFinish() {
        if (this.settings.endGameGlow) {
            this.settings.glowing = true
        }
        if (this.settings.generatePortals) {
            this.overworld.portalForcer.createPortal(BlockPos.ZERO, Direction.Axis.X)
            this.nether.portalForcer.createPortal(BlockPos.ZERO, Direction.Axis.X)
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
        val modified =  if (level == this.end && stage >= UHCBorderStage.Sixth) UHCBorderStage.Fifth else stage
        val multiplier = this.settings.borderSizeMultiplier
        val dest = if (size == UHCBorderSize.End) {
            modified.getEndSizeFor(level, multiplier)
        } else {
            modified.getStartSizeFor(level, multiplier)
        }
        val time = if (instant) -1.0 else modified.getRemainingTimeAsPercent(border.size, level, multiplier)

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

        FAKE_BORDER.size = border.size + 0.5
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

    private fun shouldObserveeGlow(observee: Entity, observer: ServerPlayer): Boolean {
        if (observee !is ServerPlayer || this.players.isSpectating(observee)) {
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
                            argument("teleport", BoolArgumentType.bool()).executes(::addPlayerToTeam)
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
            literal("fullbright") {
                executes { CommonCommands.toggleFullbright(this@UHCMinigame, it) }
            }
            literal("teamglow") {
                executes { CommonCommands.toggleTeamGlow(this@UHCMinigame, it) }
            }
            literal("spectate") {
                executes { CommonCommands.openSpectatingScreen(this@UHCMinigame, it) }
            }
            literal("pos") {
                executes { CommonCommands.broadcastPositionToTeammates(this@UHCMinigame, it) }
            }
        })
        this.commands.register(CommandUtils.buildLiteral("s") {
            executes { CommonCommands.openSpectatingScreen(this@UHCMinigame, it) }
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

    companion object {
        private val FAKE_BORDER = WorldBorder()
        val ID = UHCMod.id("uhc_minigame")

        fun of(
            server: MinecraftServer,
            overworld: RuntimeWorldHandle,
            nether: RuntimeWorldHandle,
            end: RuntimeWorldHandle
        ): UHCMinigame {
            return UHCMinigame(server, overworld.asWorld(), nether.asWorld(), end.asWorld()).apply {
                levels.add(overworld)
                levels.add(nether)
                levels.add(end)
            }
        }
    }
}