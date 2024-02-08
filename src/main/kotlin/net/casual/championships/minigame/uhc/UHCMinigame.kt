package net.casual.championships.minigame.uhc

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.netty.buffer.Unpooled
import me.senseiwells.replay.player.PlayerRecorders
import net.casual.arcade.border.MultiLevelBorderListener
import net.casual.arcade.border.MultiLevelBorderTracker
import net.casual.arcade.border.TrackedBorder
import net.casual.arcade.events.block.BrewingStandBrewEvent
import net.casual.arcade.events.entity.EntityStartTrackingEvent
import net.casual.arcade.events.entity.MobCategorySpawnEvent
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.*
import net.casual.arcade.events.server.ServerRecipeReloadEvent
import net.casual.arcade.gui.shapes.ArrowShape
import net.casual.arcade.minigame.annotation.HAS_PLAYER_PLAYING
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.BorderUtils
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.MinigameUtils.addEventListener
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.directionToNearestBorder
import net.casual.arcade.utils.PlayerUtils.directionVectorToNearestBorder
import net.casual.arcade.utils.PlayerUtils.getKillCreditWith
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.grantAllRecipes
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.sendParticles
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendSubtitle
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.CasualMod
import net.casual.championships.events.border.BorderEntityPortalEntryPointEvent
import net.casual.championships.events.border.BorderPortalWithinBoundsEvent
import net.casual.championships.extensions.PlayerFlag
import net.casual.championships.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.championships.extensions.PlayerUHCExtension.Companion.uhc
import net.casual.championships.managers.DataManager
import net.casual.championships.managers.TeamManager.hasAlivePlayers
import net.casual.championships.minigame.uhc.UHCPhase.*
import net.casual.championships.minigame.uhc.advancement.UHCAdvancementManager
import net.casual.championships.minigame.uhc.advancement.UHCAdvancements
import net.casual.championships.minigame.uhc.resources.UHCResources
import net.casual.championships.minigame.uhc.task.GlowingBossBarTask
import net.casual.championships.minigame.uhc.task.GracePeriodBossBarTask
import net.casual.championships.minigame.uhc.ui.ActiveBossBar
import net.casual.championships.recipes.GoldenHeadRecipe
import net.casual.championships.util.*
import net.casual.championships.util.CasualPlayerUtils.boostHealth
import net.casual.championships.util.CasualPlayerUtils.isAliveSolo
import net.casual.championships.util.CasualPlayerUtils.updateGlowingTag
import net.casual.championships.util.DirectionUtils.opposite
import net.casual.championships.util.Texts.regularShiftedDown1
import net.casual.championships.util.Texts.regularShiftedDown2
import net.casual.championships.util.Texts.regularShiftedDown3
import net.casual.championships.util.Texts.regularShiftedDown4
import net.casual.championships.util.Texts.regularShiftedDown5
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction8
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.TicketType
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.util.Unit
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Items
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.HitResult
import net.minecraft.world.scores.Team
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import kotlin.math.atan2

class UHCMinigame(
    server: MinecraftServer,
    overworld: RuntimeWorldHandle,
    nether: RuntimeWorldHandle,
    end: RuntimeWorldHandle
): SavableMinigame<UHCMinigame>(
    server,
), MultiLevelBorderListener {
    private val tracker = MultiLevelBorderTracker()
    private val spawn by lazy {
        this.overworld.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.ZERO)
    }
    private var bordersMoving = false

    override val id = CasualUtils.id("uhc_minigame")

    val uhcAdvancements = UHCAdvancementManager(this)

    val overworld: ServerLevel = overworld.asWorld()
    val nether: ServerLevel = nether.asWorld()
    val end: ServerLevel = end.asWorld()

    override val settings = UHCSettings(this)

    init {
        this.addLevel(overworld)
        this.addLevel(nether)
        this.addLevel(end)

        this.addTaskFactory(GlowingBossBarTask.cast())
        this.addTaskFactory(GracePeriodBossBarTask.cast())

        this.ui.addBossbar(ActiveBossBar(this))
        this.addResources(UHCResources)

        this.settings.replay = !Config.dev

        // Spawn chunks
        this.overworld.chunkSource.addRegionTicket(TicketType.START, ChunkPos(BlockPos.ZERO), 3, Unit.INSTANCE)
    }

    override fun initialize() {
        super.initialize()

        this.addEventListener(this.uhcAdvancements)
        this.recipes.add(listOf(GoldenHeadRecipe.create()))
        this.advancements.addAll(UHCAdvancements.getAllAdvancements())
        this.initialiseBorderTracker()

        this.commands.register(this.createUHCCommand())
    }

    private fun isRunning(): Boolean {
        return this.isPhase(Grace) || this.isPhase(BorderMoving) || this.isPhase(BorderFinished)
    }

    fun startWorldBorders() {
        this.moveWorldBorders(this.settings.borderStage)
    }

    fun resetWorldBorders() {
        this.bordersMoving = false
        val multiplier = this.settings.borderSizeMultiplier
        for ((border, level) in this.tracker.getAllTracking()) {
            border.setSizeUntracked(UHCBorderStage.FIRST.getStartSizeFor(level, multiplier))
        }
    }

    fun moveWorldBorders(stage: UHCBorderStage, size: UHCBorderSize = UHCBorderSize.END, instant: Boolean = false) {
        this.bordersMoving = true
        for ((border, level) in this.tracker.getAllTracking()) {
            this.moveWorldBorder(border, level, stage, size, instant)
        }
    }

    override fun onAllBordersComplete(borders: Map<TrackedBorder, ServerLevel>) {
        val stage = this.settings.borderStage
        val size = stage.getEndSizeFor(this.overworld, this.settings.borderSizeMultiplier)
        if (this.overworld.worldBorder.size != size) {
            CasualMod.logger.info("Border paused at stage $stage")
            return
        }
        this.bordersMoving = false

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

    override fun getPhases(): Collection<UHCPhase> {
        return UHCPhase.values().toList()
    }

    override fun readData(json: JsonObject) {
        this.uhcAdvancements.deserialize(json.obj("advancements"))
        this.bordersMoving = json.booleanOrDefault("borders_moving")
    }

    override fun writeData(json: JsonObject) {
        json.add("advancements", this.uhcAdvancements.serialize())
        json.addProperty("borders_moving", this.bordersMoving)
        val dimensions = JsonObject()
        dimensions["overworld"] = this.overworld.dimension().location().toString()
        dimensions["nether"] = this.nether.dimension().location().toString()
        dimensions["end"] = this.end.dimension().location().toString()
        json.add("dimensions", dimensions)
    }

    private fun createUHCCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("uhc").requiresAdminOrPermission().then(
            Commands.literal("player").then(
                Commands.argument("player", EntityArgument.player()).then(
                    Commands.literal("add").then(
                        Commands.argument("team", TeamArgument.team()).then(
                            Commands.argument("teleport", BoolArgumentType.bool()).executes(this::addPlayerToTeam)
                        ).executes { this.addPlayerToTeam(it, false) }
                    )
                ).then(
                    Commands.literal("resethealth").executes(this::resetPlayerHealth)
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

        this.setAsPlaying(target)

        if (teleport) {
            for (player in this.getPlayingPlayers()) {
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

    @Listener(before = BORDER_FINISHED_ID)
    private fun onPause(event: MinigamePauseEvent) {
        if (this.bordersMoving) {
            this.pauseWorldBorders()
        }
    }

    @Listener(before = BORDER_FINISHED_ID)
    private fun onUnpause(event: MinigameUnpauseEvent) {
        if (this.bordersMoving) {
            this.startWorldBorders()
        }
    }

    @Listener
    private fun onMobCategorySpawn(event: MobCategorySpawnEvent) {
        val (_, category, _, state) = event
        val i = category.maxInstancesPerChunk * state.spawnableChunkCount / 289
        if (state.mobCategoryCounts.getInt(category) >= i * PerformanceUtils.MOBCAP_MULTIPLIER) {
            event.cancel()
        }
    }

    @Listener
    private fun onEntityStartTracking(event: EntityStartTrackingEvent) {
        val entity = event.entity
        if (entity is Mob && PerformanceUtils.isEntityAIDisabled(entity)) {
            entity.isNoAi = true
        }
    }

    @Listener
    private fun onBrewingStandBrew(event: BrewingStandBrewEvent) {
        if (!this.settings.opPotions) {
            val ingredient = event.entity.getItem(3)
            if (ingredient.`is`(Items.GLOWSTONE_DUST) || ingredient.`is`(Items.GLISTERING_MELON_SLICE)) {
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
        val margin = shrinkingSpeed * this.settings.portalEscapeTime.toMilliseconds()
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
        var margin = shrinkingSpeed * this.settings.portalEscapeTime.toMilliseconds()
        margin = margin.coerceAtMost(border.size * 0.5 - 1)
        event.cancel(
            pos.x >= border.minX + margin
                && pos.x + 1 <= border.maxX - margin
                && pos.z >= border.minZ + margin
                && pos.z + 1 <= border.maxZ - margin
        )
    }

    @Listener(before = BORDER_FINISHED_ID)
    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event

        if (!this.isSpectating(player)) {
            this.updateWorldBorder(player)
        }

        this.updateHUD(player)
    }

    @Listener(flags = HAS_PLAYER_PLAYING)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        event.invoke() // Post event
        val (player, source) = event

        // We can stop recording now...
        GlobalTickedScheduler.schedule(1, MinecraftTimeUnit.Seconds) {
            PlayerRecorders.get(player)?.stop()
        }

        if (this.isRunning()) {
            this.onEliminated(player, player.getKillCreditWith(source))
        }
    }

    @Listener
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.setGameMode(GameType.SPECTATOR)
    }

    @Listener
    private fun onMinigamePlayerRemoved(event: MinigameRemovePlayerEvent) {
        val player = event.player
        val instance = player.attributes.getInstance(Attributes.MAX_HEALTH)
        instance?.removeModifier(CasualPlayerUtils.HEALTH_BOOST)
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()
        PlayerRecorders.get(player)?.stop()
    }

    @Listener
    private fun onRecipeReload(event: ServerRecipeReloadEvent) {
        event.add(GoldenHeadRecipe.create())
    }

    @Listener
    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        val (player, stack) = event
        if (stack.`is`(Items.BOW)) {
            player.cooldowns.addCooldown(Items.BOW, this.settings.bowCooldown.toTicks())
        }
    }

    @Listener
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        if (player.isSpectator) {
            event.cancel()
        }
    }

    @Listener
    private fun onMinigameAddNewPlayer(event: MinigameAddNewPlayerEvent) {
        event.player.setRespawnPosition(
            this.overworld.dimension(),
            this.spawn,
            0.0F,
            true,
            false
        )
    }

    @Listener(priority = 0)
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {
        val (_, player) = event
        player.updateGlowingTag()

        if (this.isRunning() && this.settings.glowing && this.isPlaying(player)) {
            player.setGlowingTag(true)
        }

        if (player.team == null || (this.phase > Initializing && !player.flags.has(PlayerFlag.Participating))) {
            this.makeSpectator(player)
        } else {
            GlobalTickedScheduler.later {
                if (!PlayerRecorders.has(player) && this.isPlaying(player) && this.settings.replay) {
                    PlayerRecorders.create(player).tryStart()
                }
            }
        }

        // Needed for updating the player's health
        GlobalTickedScheduler.schedule(1, MinecraftTimeUnit.Seconds, player::resetSentInfo)
    }

    @Listener
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

    @Listener
    private fun onMinigameClose(event: MinigameCloseEvent) {
        // TODO:
        DataManager.database.update(this)

        this.teams.showNameTags()
    }

    @Listener
    private fun onMinigameAddSpectator(event: MinigameAddSpectatorEvent) {
        val (_, player) = event
        player.setGameMode(GameType.SPECTATOR)
        player.setGlowingTag(false)

        val flags = player.flags
        flags.set(PlayerFlag.TeamGlow, false)
        flags.set(PlayerFlag.FullBright, true)
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
        if (packet is ClientboundAddEntityPacket && packet.type == EntityType.PLAYER) {
            val newPlayer = player.server.playerList.getPlayer(packet.uuid)
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
        fakeBorder.size = border.size - 0.5
        fakeBorder.lerpSizeBetween(fakeBorder.size, fakeBorder.size + 0.5, Long.MAX_VALUE)
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

    private fun updateHUD(player: ServerPlayer) {
        val direction = Texts.direction(Direction.orderedByNearest(player).filter { it.axis != Direction.Axis.Y }[0])
        val back = Texts.negativeWidthOf(direction)
        val position = "(${player.blockX}, ${player.blockY}, ${player.blockZ})"
        val shift = position.length - 7

        player.connection.send(ClientboundSetActionBarTextPacket(
            Component.empty()
                .append(Texts.space(-485 + shift * 6))
                .append(direction.regularShiftedDown4())
                .append(back)
                .append(position.literal().regularShiftedDown5())
        ))
    }

    private fun handleTrackerUpdatePacketForTeamGlowing(
        glowingPlayer: ServerPlayer,
        observingPlayer: ServerPlayer,
        packet: ClientboundSetEntityDataPacket
    ): ClientboundSetEntityDataPacket {
        if (!this.settings.friendlyPlayerGlow || !this.isRunning()) {
            return packet
        }
        if (!this.isPlaying(glowingPlayer) || !observingPlayer.flags.has(PlayerFlag.TeamGlow)) {
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
        this.makeSpectator(player)

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
            } else {
                player.drop(head, true, false)
            }
        }

        val team = player.team
        if (team !== null && !this.teams.isTeamEliminated(team) && !team.hasAlivePlayers(player)) {
            this.teams.addEliminatedTeam(team)
            for (playing in this.getAllPlayers()) {
                playing.sendSound(SoundEvents.LIGHTNING_BOLT_THUNDER, volume = 0.5F)
                playing.sendSystemMessage(Texts.UHC_ELIMINATED.generate(team.name).withStyle(team.color).bold())
            }
        }

        // TODO: Test this
        if (this.teams.getPlayingTeams().size <= 1) {
            this.setPhase(GameOver)
        }
    }

    private fun onKilled(player: ServerPlayer, killed: ServerPlayer) {
        val team = killed.team
        if (team != null && this.settings.soloBuff && team.hasAlivePlayers() && player.isAliveSolo()) {
            player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 60, 2))
        }
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

        CasualMod.logger.info("Level ${level.dimension().location()} moving to $dest")
        moveWorldBorder(border, dest, time)
    }

    private fun moveWorldBorder(border: TrackedBorder, newSize: Double, percent: Double = -1.0) {
        val duration = this.settings.borderTime * percent
        if (!duration.isZero()) {
            border.lerpSizeBetween(border.size, newSize, duration)
            return
        }
        border.size = newSize
    }

    fun setAsPlaying(player: ServerPlayer) {
        this.removeSpectator(player)

        player.sendTitle(Texts.LOBBY_GOOD_LUCK.gold().bold())
        player.sendSound(SoundEvents.NOTE_BLOCK_BELL.value())

        player.grantAllRecipes()

        player.grantAdvancement(UHCAdvancements.ROOT)

        this.resetPlayerHealth(player)
        player.resetHunger()
        player.resetExperience()
        player.clearPlayerInventory()

        player.removeVehicle()
        player.setGlowingTag(false)

        val flags = player.flags
        flags.set(PlayerFlag.FullBright, false)
        flags.set(PlayerFlag.FullBright, true)

        val team = player.team
        team?.nameTagVisibility = Team.Visibility.NEVER

        flags.set(PlayerFlag.Participating, true)
        flags.set(PlayerFlag.TeamGlow, true)

        player.addEffect(
            MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4, true, false)
        )
        player.setGameMode(GameType.SURVIVAL)

        if (team != null && team.getOnlineCount() == 1) {
            player.grantAdvancement(UHCAdvancements.SOLOIST)
        }
    }

    private fun resetPlayerHealth(player: ServerPlayer) {
        player.boostHealth(this.settings.health)
        player.resetHealth()
    }
}