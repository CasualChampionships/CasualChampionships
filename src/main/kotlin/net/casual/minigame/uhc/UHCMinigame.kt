package net.casual.minigame.uhc

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.netty.buffer.Unpooled
import me.senseiwells.replay.player.PlayerRecorders
import net.casual.CasualMod
import net.casual.arcade.border.MultiLevelBorderListener
import net.casual.arcade.border.MultiLevelBorderTracker
import net.casual.arcade.border.TrackedBorder
import net.casual.arcade.events.block.BrewingStandBrewEvent
import net.casual.arcade.events.entity.EntityStartTrackingEvent
import net.casual.arcade.events.entity.MobCategorySpawnEvent
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigamePauseEvent
import net.casual.arcade.events.minigame.MinigameRemovePlayerEvent
import net.casual.arcade.events.minigame.MinigameUnpauseEvent
import net.casual.arcade.events.player.*
import net.casual.arcade.events.server.ServerRecipeReloadEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.shapes.ArrowShape
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.SavableMinigame
import net.casual.arcade.minigame.annotation.MinigameEvent
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.settings.DisplayableGameSettingBuilder
import net.casual.arcade.stats.ArcadeStats
import net.casual.arcade.utils.BorderUtils
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.literalNamed
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.LevelUtils
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.directionToNearestBorder
import net.casual.arcade.utils.PlayerUtils.directionVectorToNearestBorder
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.message
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.sendParticles
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendSubtitle
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.teamMessage
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.SettingsUtils.defaultOptions
import net.casual.arcade.utils.StatUtils.increment
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.events.border.BorderEntityPortalEntryPointEvent
import net.casual.events.border.BorderPortalWithinBoundsEvent
import net.casual.events.player.PlayerFlagEvent
import net.casual.extensions.PlayerFlag
import net.casual.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.extensions.PlayerStat
import net.casual.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casual.extensions.PlayerUHCExtension.Companion.uhc
import net.casual.extensions.TeamFlag
import net.casual.extensions.TeamFlagsExtension.Companion.flags
import net.casual.items.MinesweeperItem
import net.casual.managers.DataManager
import net.casual.managers.TeamManager
import net.casual.managers.TeamManager.hasAlivePlayers
import net.casual.minigame.CasualMinigame
import net.casual.minigame.uhc.UHCPhase.*
import net.casual.minigame.uhc.advancement.RaceAdvancement
import net.casual.minigame.uhc.advancement.UHCAdvancements
import net.casual.minigame.uhc.events.DefaultUHC
import net.casual.minigame.uhc.events.UHCEvent
import net.casual.minigame.uhc.gui.ActiveBossBar
import net.casual.minigame.uhc.gui.BorderDistanceRow
import net.casual.minigame.uhc.gui.TeammateRow
import net.casual.minigame.uhc.task.GlowingBossBarTask
import net.casual.minigame.uhc.task.GracePeriodBossBarTask
import net.casual.recipes.GoldenHeadRecipe
import net.casual.screen.MinesweeperScreen
import net.casual.util.*
import net.casual.util.CasualPlayerUtils.isAliveSolo
import net.casual.util.CasualPlayerUtils.isMessageGlobal
import net.casual.util.CasualPlayerUtils.setForUHC
import net.casual.util.CasualPlayerUtils.updateGlowingTag
import net.casual.util.DirectionUtils.opposite
import net.casual.util.Texts.monospaced
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.effect.MobEffects.NIGHT_VISION
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec2
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.math.atan2

class UHCMinigame(
    server: MinecraftServer,
    event: UHCEvent = DefaultUHC,
    val overworld: ServerLevel = LevelUtils.overworld(),
    val nether: ServerLevel = LevelUtils.nether(),
    val end: ServerLevel = LevelUtils.end()
): SavableMinigame<UHCMinigame>(
    server,
), MultiLevelBorderListener {
    private val tracker = MultiLevelBorderTracker()
    private val claimed = HashSet<RaceAdvancement>()

    override val id = CasualUtils.id("uhc_minigame")

    val settings = Settings()

    var event: UHCEvent = event
        private set

    var uptime = 0
        private set

    init {
        this.initialize()

        this.addTaskFactory(GlowingBossBarTask.cast())
        this.addTaskFactory(GracePeriodBossBarTask.cast())
    }

    override fun initialize() {
        super.initialize()

        this.registerAdvancements()
        this.recipes.add(listOf(GoldenHeadRecipe.create()))
        this.initialiseBorderTracker()

        this.event.initialise(this)

        this.ui.addBossbar(ActiveBossBar(this))
        this.ui.setTabDisplay(CasualMinigame.createTabDisplay())
        this.createActiveSidebar()

        this.commands.register(this.createUHCCommand())
    }

    private fun isRunning(): Boolean {
        return this.isPhase(Grace) || this.isPhase(BorderMoving) || this.isPhase(BorderFinished)
    }

    fun startWorldBorders() {
        this.moveWorldBorders(this.settings.borderStage)
    }

    fun resetWorldBorders() {
        val multiplier = this.settings.borderSizeMultiplier
        for ((border, level) in this.tracker.getAllTracking()) {
            border.setSizeUntracked(UHCBorderStage.FIRST.getStartSizeFor(level, multiplier))
        }
    }

    fun moveWorldBorders(stage: UHCBorderStage, size: UHCBorderSize = UHCBorderSize.END, instant: Boolean = false) {
        for ((border, level) in this.tracker.getAllTracking()) {
            this.moveWorldBorder(border, level, stage, size, instant)
        }
    }

    override fun onAllBordersComplete(borders: Map<TrackedBorder, ServerLevel>) {
        val stage = this.settings.borderStage
        CasualMod.logger.info("Finished world border stage: $stage")
        if (!this.isRunning()) {
            return
        }
        val next = stage.getNextStage()

        if (next == UHCBorderStage.END) {
            this.setPhase(BorderFinished)
            return
        }

        this.scheduler.schedulePhased(10.Seconds, MinigameTask(this, UHCMinigame::startNextBorders))
    }

    private fun pauseWorldBorders() {
        for ((border, _) in tracker.getAllTracking()) {
            this.moveWorldBorder(border, border.size)
        }
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

    fun isUnclaimed(achievement: RaceAdvancement): Boolean {
        return this.claimed.add(achievement)
    }

    fun resetTrackers() {
        this.uptime = 0
        this.claimed.clear()
        this.settings.glowing = false
    }

    override fun getResources(): MinigameResources {
        return this.event.getResourcePackHandler()
    }

    override fun getLevels(): Collection<ServerLevel> {
        return listOf(this.overworld, this.nether, this.end)
    }

    override fun getPhases(): Collection<UHCPhase> {
        return UHCPhase.values().toList()
    }

    override fun readData(json: JsonObject) {
        this.uptime = json.int("uptime")
        for (claimed in json.array("claimed")) {
            this.claimed.add(RaceAdvancement.valueOf(claimed.asString))
        }
    }

    override fun writeData(json: JsonObject) {
        json.addProperty("uptime", this.uptime)
        val claimed = this.claimed.stream().collect(::JsonArray, { a, r -> a.add(r.name) }, JsonArray::addAll)
        json.add("claimed", claimed)
    }

    override fun start() {
        this.setPhase(Grace)

        this.getResources().sendTo(this.getPlayers())

        val players = this.getPlayers().filter { player ->
            val team = player.team
            team !== null && !team.flags.has(TeamFlag.Ignored)
        }
        val overworld = LevelUtils.overworld()
        val range = this.settings.borderStage.getStartSizeFor(overworld, this.settings.borderSizeMultiplier) * 0.45
        PlayerUtils.spread(
            overworld,
            Vec2(0.0F, 0.0F),
            500.0,
            range,
            true,
            players
        )

        for (player in this.getPlayers()) {
            player.setForUHC(this, true)
            if (player.isSpectator) {
                if (player.level() != overworld) {
                    player.teleportTo(overworld, 0.0, 200.0, 0.0, 0.0F, 0.0F)
                }
            }
        }
    }

    private fun createUHCCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("uhc").requires {
            it.hasPermission(4)
        }.then(
            Commands.literal("player").then(
                Commands.argument("player", EntityArgument.player()).then(
                    Commands.literal("add").then(
                        Commands.argument("team", TeamArgument.team()).then(
                            Commands.argument("teleport", BoolArgumentType.bool()).executes(this::addPlayerToTeam)
                        ).executes { this.addPlayerToTeam(it, false) }
                    )
                )
            )
        ).then(
            Commands.literal("border").then(
                Commands.literal("start").executes(this::startWorldBorders)
            )
        )
    }

    private fun addPlayerToTeam(
        context: CommandContext<CommandSourceStack>,
        teleport: Boolean = BoolArgumentType.getBool(context, "teleport")
    ): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val team = TeamArgument.getTeam(context, "team")

        val server = context.source.server
        server.scoreboard.addPlayerToTeam(target.scoreboardName, team)
        target.sendSystemMessage(Texts.UHC_ADDED_TO_TEAM.generate(team.formattedDisplayName))

        target.setForUHC(this, !target.flags.has(PlayerFlag.Participating))

        if (teleport) {
            for (player in this.getPlayers()) {
                if (team.players.contains(player.scoreboardName) && player.isSurvival && target != player) {
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

    private fun startWorldBorders(context: CommandContext<CommandSourceStack>): Int {
        this.startWorldBorders()
        return context.source.success("Successfully started world borders")
    }

    @MinigameEvent
    private fun onServerTick(event: ServerTickEvent) {
        if (!this.paused) {
            this.uptime++
        }
    }

    @MinigameEvent(start = GRACE_ID, end = BORDER_FINISHED_ID)
    private fun onPause(event: MinigamePauseEvent) {
        this.pauseWorldBorders()
    }

    @MinigameEvent(start = GRACE_ID, end = BORDER_FINISHED_ID)
    private fun onUnpause(event: MinigameUnpauseEvent) {
        this.startWorldBorders()
    }

    @MinigameEvent
    private fun onMobCategorySpawn(event: MobCategorySpawnEvent) {
        val (_, category, _, state) = event
        val i = category.maxInstancesPerChunk * state.spawnableChunkCount / 289
        if (state.mobCategoryCounts.getInt(category) >= i * PerformanceUtils.MOBCAP_MULTIPLIER) {
            event.cancel()
        }
    }

    @MinigameEvent
    private fun onEntityStartTracking(event: EntityStartTrackingEvent) {
        val entity = event.entity
        if (entity is Mob && PerformanceUtils.isEntityAIDisabled(entity)) {
            entity.isNoAi = true
        }
    }

    @MinigameEvent
    private fun onBrewingStandBrew(event: BrewingStandBrewEvent) {
        if (!this.settings.opPotions) {
            val ingredient = event.entity.getItem(3)
            if (ingredient.`is`(Items.GLOWSTONE_DUST) || ingredient.`is`(Items.GLISTERING_MELON_SLICE)) {
                event.cancel()
            }
        }
    }

    @MinigameEvent
    private fun onBorderEntityPortalEntryPointEvent(event: BorderEntityPortalEntryPointEvent) {
        val (border, _, _, pos) = event

        // Blocks per millisecond
        val shrinkingSpeed = border.lerpSpeed
        if (shrinkingSpeed <= 0) {
            // The border is static or expanding
            return
        }
        val margin = shrinkingSpeed * (this.settings.portalEscapeTime * 1000)
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

    @MinigameEvent
    private fun onBorderWithinBoundsEvent(event: BorderPortalWithinBoundsEvent) {
        val (border, _, pos) = event
        // Blocks per millisecond
        val shrinkingSpeed = border.lerpSpeed
        if (shrinkingSpeed <= 0) {
            // The border is static or expanding
            return
        }
        var margin = shrinkingSpeed * (this.settings.portalEscapeTime * 1000)
        margin = margin.coerceAtMost(border.size * 0.5 - 1)
        event.cancel(
            pos.x >= border.minX + margin
                && pos.x + 1 <= border.maxX - margin
                && pos.z >= border.minZ + margin
                && pos.z + 1 <= border.maxZ - margin
        )
    }

    @MinigameEvent(start = GRACE_ID, end = BORDER_FINISHED_ID)
    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event

        if (this.isRunning()) {
            if (player.isSurvival) {
                this.updateWorldBorder(player)
            }
        }
    }

    @MinigameEvent
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        event.invoke() // Post event
        val (player, source) = event

        player.flags.set(PlayerFlag.TeamGlow, false)

        // We can stop recording now...
        GlobalTickedScheduler.schedule(1, MinecraftTimeUnit.Seconds) {
            PlayerRecorders.get(player)?.stop()
        }

        if (this.isRunning()) {
            this.onEliminated(player, source.entity)
        }
    }

    @MinigameEvent
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.setGameMode(GameType.SPECTATOR)
    }

    @MinigameEvent
    private fun onMinigamePlayerRemoved(event: MinigameRemovePlayerEvent) {
        val player = event.player
        val instance = player.attributes.getInstance(Attributes.MAX_HEALTH)
        instance?.removeModifier(CasualPlayerUtils.HEALTH_BOOST)
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()
        PlayerRecorders.get(player)?.stop()
    }

    @MinigameEvent(start = GRACE_ID, end = BORDER_FINISHED_ID)
    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        val (player) = event
        DataManager.database.updateStats(player)
    }

    @MinigameEvent
    private fun onRecipeReload(event: ServerRecipeReloadEvent) {
        event.add(GoldenHeadRecipe.create())
    }

    @MinigameEvent
    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        val (player, stack) = event
        if (stack.`is`(Items.BOW)) {
            player.cooldowns.addCooldown(Items.BOW, (this.settings.bowCooldown * 20).toInt())
        }
    }

    @MinigameEvent
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        if (player.isSpectator) {
            event.cancel()
        }
    }

    @MinigameEvent
    private fun onPlayerFlag(event: PlayerFlagEvent) {
        val player = event.player
        when (event.flag) {
            PlayerFlag.TeamGlow -> {
                val team = player.team ?: return player.updateGlowingTag()
                for (member in team.getOnlinePlayers()) {
                    member.updateGlowingTag()
                }
            }
            PlayerFlag.FullBright -> {
                if (event.value) {
                    player.addEffect(MobEffectInstance(NIGHT_VISION, INFINITE_DURATION, 0, false, false))
                } else {
                    player.removeEffect(NIGHT_VISION)
                }
            }
            else -> { }
        }
    }

    @MinigameEvent(priority = 0)
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val (_, player) = event
        player.updateGlowingTag()

        if (this.isRunning() && this.settings.glowing && player.isSurvival) {
            player.setGlowingTag(true)
        }

        val scoreboard = this.server.scoreboard
        if (player.team == null || !player.flags.has(PlayerFlag.Participating)) {
            player.setGameMode(GameType.SPECTATOR)
        } else if (!PlayerRecorders.has(player)) {
            GlobalTickedScheduler.later {
                PlayerRecorders.create(player).start()
            }
        }

        if (player.team == null) {
            val spectator = scoreboard.getPlayerTeam("Spectator")
            if (spectator != null) {
                scoreboard.addPlayerToTeam(player.scoreboardName, spectator)
            }
        }

        // Needed for updating the player's health
        GlobalTickedScheduler.schedule(1, MinecraftTimeUnit.Seconds, player::resetSentInfo)
    }

    @MinigameEvent
    private fun onPlayerChat(event: PlayerChatEvent) {
        val (player, message) = event
        val content = message.signedContent()
        if (this.isRunning() && !player.isMessageGlobal(content)) {
            player.teamMessage(message)
        } else {
            val decorated = if (content.startsWith('!')) content.substring(1) else content
            if (decorated.isNotBlank()) {
                player.message(Component.literal(decorated.trim()))
            }
        }
        event.cancel()
    }

    @MinigameEvent
    private fun onPlayerClientboundPacket(event: PlayerClientboundPacketEvent) {
        val (player, packet) = event

        if (packet is ClientboundBundlePacket || packet is ClientboundSetEntityDataPacket) {
            @Suppress("UNCHECKED_CAST")
            val updated = this.updatePacket(player, packet as Packet<ClientGamePacketListener>)
            if (updated !== packet) {
                event.cancel(updated)
            }
        }

        if (packet is ClientboundBundlePacket) {
            for (sub in packet.subPackets()) {
                this.onPacket(player, sub)
            }
        } else {
            this.onPacket(player, packet)
        }
    }

    private fun updatePacket(player: ServerPlayer, packet: Packet<ClientGamePacketListener>): Packet<ClientGamePacketListener> {
        if (packet is ClientboundSetEntityDataPacket) {
            val glowing = player.serverLevel().getEntity(packet.id())
            if (glowing is ServerPlayer) {
                return this.handleTrackerUpdatePacketForTeamGlowing(glowing, player, packet)
            }
        }
        if (packet is ClientboundBundlePacket) {
            val updated = ArrayList<Packet<ClientGamePacketListener>>()
            for (sub in packet.subPackets()) {
                updated.add(this.updatePacket(player, sub))
            }
            return ClientboundBundlePacket(updated)
        }
        return packet
    }

    private fun onPacket(player: ServerPlayer, packet: Packet<*>) {
        if (packet is ClientboundAddPlayerPacket) {
            val newPlayer = player.server.playerList.getPlayer(packet.playerId)
            newPlayer?.updateGlowingTag()
        }
    }

    private fun updateWorldBorder(player: ServerPlayer) {
        val level = player.level()
        val border = level.worldBorder

        if (border.isWithinBounds(player.boundingBox)) {
            if (player.flags.has(PlayerFlag.WasInBorder)) {
                player.flags.set(PlayerFlag.WasInBorder, false)
                player.connection.send(ClientboundInitializeBorderPacket(border))
            }
            return
        }

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

                val arrow = ArrowShape.createCentred(position.x, hit.location.y + 0.1, position.z, 1.0, rotation)

                for (point in arrow) {
                    player.sendParticles(ParticleTypes.END_ROD, point)
                }
            }
        }

        val direction = player.directionToNearestBorder()
        val fakeDirection = direction.opposite()

        val fakeCenterX = border.centerX + fakeDirection.stepX * border.size
        val fakeCenterZ = border.centerZ + fakeDirection.stepZ * border.size

        val scale = level.dimensionType().coordinateScale

        val fakeBorder = player.uhc.border
        fakeBorder.size = border.size + 0.5
        // Foolish Minecraft uses scale for the centre, even on the client,
        // so we need to reproduce.
        fakeBorder.setCenter(fakeCenterX * scale, fakeCenterZ * scale)
        player.connection.send(ClientboundInitializeBorderPacket(fakeBorder))

        if (this.uptime % 200 == 0) {
            player.sendTitle(Component.empty())
            player.sendSubtitle(Texts.UHC_OUTSIDE_BORDER.generate(Texts.direction(direction).lime()))
        }

        player.flags.set(PlayerFlag.WasInBorder, true)
    }

    private fun handleTrackerUpdatePacketForTeamGlowing(
        glowingPlayer: ServerPlayer,
        observingPlayer: ServerPlayer,
        packet: ClientboundSetEntityDataPacket
    ): ClientboundSetEntityDataPacket {
        if (!this.settings.friendlyPlayerGlow || !this.isRunning()) {
            return packet
        }
        if (!glowingPlayer.isSurvival || !observingPlayer.flags.has(PlayerFlag.TeamGlow)) {
            return packet
        }
        if (glowingPlayer.team !== observingPlayer.team) {
            return packet
        }

        val tracked = packet.packedItems ?: return packet
        if (tracked.none { it.id == Entity.DATA_SHARED_FLAGS_ID.id }) {
            return packet
        }

        // Make a copy of the packet, because other players are sent the same instance of
        // The packet and may not be on the same team
        val buf = FriendlyByteBuf(Unpooled.buffer())
        packet.write(buf)
        val new = ClientboundSetEntityDataPacket(buf)

        val iterator = new.packedItems.listIterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            // Need to compare ids because they're not the same instance once re-serialized
            if (value.id() == Entity.DATA_SHARED_FLAGS_ID.id) {
                @Suppress("UNCHECKED_CAST")
                val byteValue = value as SynchedEntityData.DataValue<Byte>
                var flags = byteValue.value()
                flags = (flags.toInt() or (1 shl Entity.FLAG_GLOWING)).toByte()
                iterator.set(SynchedEntityData.DataValue.create(Entity.DATA_SHARED_FLAGS_ID, flags))
            }
        }
        return new
    }

    private fun onEliminated(player: ServerPlayer, killer: Entity?) {
        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), player.xRot, true, false)
        player.setGameMode(GameType.SPECTATOR)

        if (this.isUnclaimed(RaceAdvancement.Death)) {
            player.grantAdvancement(UHCAdvancements.EARLY_EXIT)
        }

        if (killer is ServerPlayer) {
            this.onKilled(killer, player)
        }

        if (this.settings.playerDropsGapple) {
            player.drop(Items.GOLDEN_APPLE.defaultInstance, true, false)
        }

        if (this.settings.playerDropsHead) {
            val head = HeadUtils.createConsumablePlayerHead(player)
            if (killer is ServerPlayer) {
                if (!killer.inventory.add(head)) {
                    player.drop(head, true, false)
                }
            }
        }

        val team = player.team
        if (team !== null && !team.flags.has(TeamFlag.Eliminated) && !team.hasAlivePlayers(player)) {
            team.flags.set(TeamFlag.Eliminated, true)
            for (playing in this.getPlayers()) {
                playing.sendSound(SoundEvents.LIGHTNING_BOLT_THUNDER, volume = 0.5F)
                playing.sendSystemMessage(Texts.UHC_ELIMINATED.generate(team.name).withStyle(team.color).bold())
            }
        }

        if (TeamManager.isOneTeamRemaining(this.getPlayers().filter { it != player })) {
            this.setPhase(GameOver)
        }
    }

    private fun onKilled(player: ServerPlayer, killed: ServerPlayer) {
        if (this.isUnclaimed(RaceAdvancement.Kill)) {
            player.grantAdvancement(UHCAdvancements.FIRST_BLOOD)
        }

        val team = killed.team
        if (team != null && this.settings.soloBuff && team.hasAlivePlayers() && player.isAliveSolo()) {
            player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 60, 2))
        }
    }

    fun createActiveSidebar() {
        val name = ArcadeNameTag(
            Entity::getDisplayName
        ) { a, _ -> !a.isInvisible }
        val health = ArcadeNameTag(
            { "${it.health / 2} ".literal().append(Texts.ICON_HEART) },
            { a, b -> !a.isInvisible && (b.isSpectator || b.team == a.team) }
        )

        this.ui.addNameTag(health)
        this.ui.addNameTag(name)

        val sidebar = ArcadeSidebar(ComponentSupplier.of(Texts.CASUAL_UHC.gold().bold()))

        val buffer = "   "

        sidebar.addRow(ComponentSupplier.of(Component.empty().append(buffer).append(Texts.SIDEBAR_TEAMMATES.monospaced())))
        for (i in 0 until this.event.getTeamSize()) {
            sidebar.addRow(TeammateRow(i, buffer))
        }
        sidebar.addRow(ComponentSupplier.of(Component.empty()))
        sidebar.addRow(BorderDistanceRow(buffer))
        sidebar.addRow { player ->
            Component.literal(buffer).append(Texts.UHC_WB_RADIUS.generate((player.level().worldBorder.size / 2.0).toInt())).monospaced()
        }
        sidebar.addRow(ComponentSupplier.of(Component.empty()))

        this.ui.setSidebar(sidebar)
    }

    private fun initialiseBorderTracker() {
        this.tracker.addListener(this)
        for (level in this.getLevels()) {
            this.tracker.addLevelBorder(level)
        }

        BorderUtils.isolateWorldBorders()
    }

    private fun moveWorldBorder(border: TrackedBorder, level: Level, stage: UHCBorderStage, size: UHCBorderSize, instant: Boolean = false) {
        if (level == this.end && stage >= UHCBorderStage.SIX) {
            return
        }
        val multiplier = this.settings.borderSizeMultiplier
        val dest = if (size == UHCBorderSize.END) {
            stage.getEndSizeFor(level, multiplier)
        } else {
            stage.getStartSizeFor(level, multiplier)
        }
        val time = if (instant) -1.0 else stage.getRemainingTimeAsPercent(border.size, level, multiplier)

        CasualMod.logger.info("Level $level moving to $dest")
        moveWorldBorder(border, dest, time)
    }

    private fun moveWorldBorder(border: TrackedBorder, newSize: Double, percent: Double = -1.0) {
        val seconds = (percent * this.settings.borderTime).toLong()
        if (seconds > 0) {
            border.lerpSizeBetween(border.size, newSize, seconds * 1000L)
            return
        }
        border.size = newSize
    }

    private fun registerAdvancements() {
        for (advancement in UHCAdvancements.getAllAdvancements()) {
            this.advancements.add(advancement)
        }

        this.events.register<PlayerJoinEvent>(2000) { event ->
            if (this.isRunning() && event.player.isSurvival) {
                val relogs = this.stats.getOrCreateStat(event.player, ArcadeStats.RELOGS)

                // Wait for player to load in
                GlobalTickedScheduler.schedule(1.Seconds) {
                    event.player.grantAdvancement(UHCAdvancements.COMBAT_LOGGER)
                    if (relogs.value == 10) {
                        event.player.grantAdvancement(UHCAdvancements.OK_WE_BELIEVE_YOU_NOW)
                    }
                }

                val team = event.player.team
                if (team !== null && team.flags.has(TeamFlag.Eliminated)) {
                    team.flags.set(TeamFlag.Eliminated, false)
                    event.player.grantAdvancement(UHCAdvancements.TEAM_PLAYER)
                }
            }
        }
        this.events.register<PlayerDeathEvent> { event ->
            if (event.player.containerMenu is MinesweeperScreen) {
                event.player.grantAdvancement(UHCAdvancements.DISTRACTED)
            }
        }
        this.events.register<PlayerBlockPlacedEvent> { event ->
            val state = event.state
            val block = state.block
            val context = event.context
            val pos = context.clickedPos
            val world = context.level
            if (block is FallingBlock && FallingBlock.isFree(world.getBlockState(pos.below())) || pos.y < world.minBuildHeight) {
                event.player.grantAdvancement(UHCAdvancements.FALLING_BLOCK)
            } else if (block === Blocks.REDSTONE_WIRE) {
                event.player.grantAdvancement(UHCAdvancements.NOT_DUSTLESS)
            } else if (block === Blocks.TNT) {
                event.player.grantAdvancement(UHCAdvancements.DEMOLITION_EXPERT)
            }
        }
        this.events.register<PlayerCraftEvent> { event ->
            if (event.stack.`is`(Items.CRAFTING_TABLE) && this.isUnclaimed(RaceAdvancement.Craft)) {
                event.player.grantAdvancement(UHCAdvancements.WORLD_RECORD_PACE)
            }
        }
        this.events.register<PlayerBorderDamageEvent> { event ->
            if (event.invoke() && event.player.isDeadOrDying) {
                event.player.grantAdvancement(UHCAdvancements.SKILL_ISSUE)
            }
        }
        this.events.register<PlayerLootEvent> { event ->
            if (event.items.any { it.`is`(Items.ENCHANTED_GOLDEN_APPLE) }) {
                event.player.grantAdvancement(UHCAdvancements.DREAM_LUCK)
            }
        }
        this.events.register<PlayerTickEvent> { event ->
            val player = event.player
            val extension = event.player.uhc
            if (player.isSurvival && player.flags.has(PlayerFlag.Participating) && player.health <= 1.0) {
                if (++extension.halfHealthTicks == 1200) {
                    player.grantAdvancement(UHCAdvancements.ON_THE_EDGE)
                }
            } else {
                extension.halfHealthTicks = 0
            }
        }
        this.events.register<PlayerLandEvent> { event ->
            if (this.isRunning() && this.uptime < 1200 && event.damage > 0) {
                event.player.grantAdvancement(UHCAdvancements.BROKEN_ANKLES)
            }
        }
        this.events.register<PlayerChatEvent> { event ->
            val message: String = event.message.signedContent().lowercase()
            if (this.isRunning() && event.player.isMessageGlobal(message)) {
                if (message.contains("jndi") && message.contains("ldap")) {
                    event.player.grantAdvancement(UHCAdvancements.LDAP)
                }
                if (message.contains("basically")) {
                    event.player.grantAdvancement(UHCAdvancements.BASICALLY)
                }
            }
        }
        this.events.register<PlayerBlockCollisionEvent> { event ->
            if (event.state.`is`(Blocks.SWEET_BERRY_BUSH)) {
                event.player.grantAdvancement(UHCAdvancements.EMBARRASSING)
            }
        }
        this.events.register<PlayerAdvancementEvent> { event ->
            event.announce = UHCAdvancements.isRegistered(event.advancement) && event.announce
        }
    }

    internal fun grantFinalAdvancements() {
        var lowest: PlayerAttacker? = null
        var highest: PlayerAttacker? = null
        for (player in this.getPlayers()) {
            if (player.flags.has(PlayerFlag.Participating)) {
                val current = this.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_DEALT).value
                if (lowest === null) {
                    val first = PlayerAttacker(player, current)
                    lowest = first
                    highest = first
                }
                if (lowest.damage > current) {
                    lowest = PlayerAttacker(player, current)
                } else if (highest!!.damage < current) {
                    highest = PlayerAttacker(player, current)
                }
            }
        }
        if (lowest != null) {
            lowest.player.grantAdvancement(UHCAdvancements.MOSTLY_HARMLESS)
            highest!!.player.grantAdvancement(UHCAdvancements.HEAVY_HITTER)
        }
    }

    private class PlayerAttacker(
        val player: ServerPlayer,
        val damage: Float
    )

    inner class Settings {
        var glowing by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("glowing")
                .display(Items.GLOWSTONE_DUST.literalNamed("Glowing"))
                .defaultOptions()
                .value(false)
                .listener { _, value ->
                    if (value) {
                        var count = 0
                        for (player in getPlayers()) {
                            if (player.isSurvival) {
                                player.setGlowingTag(true)
                                count++
                            }
                        }
                        CasualMod.logger.info("$count player's are now glowing")
                    } else {
                        for (player in getPlayers()) {
                            player.setGlowingTag(false)
                        }
                    }
                }
                .build()
        )
        var borderSizeMultiplier by registerSetting(
            DisplayableGameSettingBuilder.double()
                .name("border_size_multiplier")
                .display(Items.BEACON.literalNamed("Border Size Multiplier"))
                .option("one_third", Items.SCAFFOLDING.literalNamed("0.33x Size"), 1.0 / 3.0)
                .option("half", Items.ANVIL.literalNamed("0.5x Size"), 0.5)
                .option("two_thirds", Items.GREEN_STAINED_GLASS_PANE.literalNamed("0.66x Size"), 2.0 / 3.0)
                .option("normal", Items.LIME_STAINED_GLASS_PANE.literalNamed("1x Size"), 1.0)
                .option("three_halves", Items.RED_STAINED_GLASS_PANE.literalNamed("1.5x Size"), 1.5)
                .option("double", Items.RED_STAINED_GLASS_PANE.literalNamed("2x Size"), 2.0)
                .value(1.0)
                .build()
        )
        var borderTime by registerSetting(
            DisplayableGameSettingBuilder.long()
                .name("border_completion_time")
                .display(Items.DIAMOND_BOOTS.literalNamed("Border Completion Time"))
                .option("ten_minutes", Items.CAKE.literalNamed("10 Minutes"), MINUTES.toSeconds(10))
                .option("thirty_minutes", Items.SCULK_SENSOR.literalNamed("30 Minutes"), MINUTES.toSeconds(30))
                .option("two_hours", Items.GREEN_STAINED_GLASS_PANE.literalNamed("2 Hours"), MINUTES.toSeconds(120))
                .option("two_and_half_hours", Items.YELLOW_STAINED_GLASS_PANE.literalNamed("2.5 Hours"), MINUTES.toSeconds(150))
                .option("three_hours", Items.RED_STAINED_GLASS_PANE.literalNamed("3 Hours"), MINUTES.toSeconds(180))
                .value(MINUTES.toSeconds(150))
                .build()
        )
        var portalEscapeTime by registerSetting(
            DisplayableGameSettingBuilder.long()
                .name("portal_escape_time")
                .display(Items.OBSIDIAN.literalNamed("Portal Escape Time"))
                .option("none", Items.CLOCK.literalNamed("None"), 0)
                .option("ten_seconds", Items.CLOCK.literalNamed("10 Seconds"), 10)
                .option("twenty_seconds", Items.CLOCK.literalNamed("20 Second"), 20)
                .option("thirty_seconds", Items.CLOCK.literalNamed("30 Seconds"), 30)
                .option("sixty_seconds", Items.CLOCK.literalNamed("60 Seconds"), 60)
                .value(30)
                .build()
        )
        var bowCooldown by registerSetting(
            DisplayableGameSettingBuilder.double()
                .name("bow_cooldown")
                .display(Items.BOW.literalNamed("Bow Cooldown"))
                .option("none", Items.CLOCK.literalNamed("None"), 0.0)
                .option("half_second", Items.CLOCK.literalNamed("0.5 Seconds"), 0.5)
                .option("one_second", Items.CLOCK.literalNamed("1 Second"), 1.0)
                .option("two_seconds", Items.CLOCK.literalNamed("2 Seconds"), 2.0)
                .option("three_seconds", Items.CLOCK.literalNamed("3 Seconds"), 3.0)
                .option("five_seconds", Items.CLOCK.literalNamed("5 Seconds"), 5.0)
                .value(1.0)
                .build()
        )
        var health by registerSetting(
            DisplayableGameSettingBuilder.double()
                .name("health")
                .display(Items.POTION.literalNamed("Health").potion(Potions.HEALING))
                .option("triple", Items.GREEN_STAINED_GLASS_PANE.literalNamed("Triple"), 2.0)
                .option("double", Items.YELLOW_STAINED_GLASS_PANE.literalNamed("Double"), 1.0)
                .option("normal", Items.RED_STAINED_GLASS_PANE.literalNamed("Normal"), 0.0)
                .value(1.0)
                .build()
        )
        var endGameGlow by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("end_game_glow")
                .display(Items.SPECTRAL_ARROW.literalNamed("End Game Glow"))
                .defaultOptions()
                .value(true)
                .build()
        )
        var friendlyPlayerGlow by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("friendly_player_glow")
                .display(Items.GOLDEN_CARROT.literalNamed("Friendly Player Glow"))
                .defaultOptions()
                .value(true)
                .build()
        )
        var playerDropsGapple by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("player_drops_gapple")
                .display(Items.GOLDEN_APPLE.literalNamed("Player Drops Gapple"))
                .defaultOptions()
                .value(false)
                .build()
        )
        var playerDropsHead by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("player_drops_head")
                .display(Items.PLAYER_HEAD.literalNamed("Player Drops Head"))
                .defaultOptions()
                .value(true)
                .build()
        )
        var opPotions by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("op_potions")
                .display(Items.SPLASH_POTION.literalNamed("OP Potions").potion(Potions.STRONG_HARMING))
                .defaultOptions()
                .value(false)
                .build()
        )
        var generatePortals by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("generate_portals")
                .display(Items.CRYING_OBSIDIAN.literalNamed("Generate Portals"))
                .defaultOptions()
                .value(true)
                .build()
        )
        var announceMinesweeper by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("announce_minesweeper")
                .display(MinesweeperItem.MINE.literalNamed("Announce Minesweeper"))
                .defaultOptions()
                .value(true)
                .build()
        )
        var soloBuff by registerSetting(
            DisplayableGameSettingBuilder.boolean()
                .name("solo_buff")
                .display(Items.LINGERING_POTION.literalNamed("Solo Buff").potion(Potions.REGENERATION))
                .defaultOptions()
                .value(true)
                .build()
        )
        var borderSize by registerSetting(
            DisplayableGameSettingBuilder.enum<UHCBorderSize>()
                .name("border_size")
                .display(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.literalNamed("World Border Size"))
                .defaultOptions(UHCBorderSize::class.java)
                .value(UHCBorderSize.START)
                .build()
        )
        var borderStageSetting = registerSetting(
            DisplayableGameSettingBuilder.enum<UHCBorderStage>()
                .name("border_stage")
                .display(Items.BARRIER.literalNamed("World Border Stage"))
                .defaultOptions(UHCBorderStage::class.java)
                .value(UHCBorderStage.FIRST)
                .listener { _, value ->
                    moveWorldBorders(value, borderSize, true)
                }
                .build()
        )
        var borderStage by this.borderStageSetting
    }
}