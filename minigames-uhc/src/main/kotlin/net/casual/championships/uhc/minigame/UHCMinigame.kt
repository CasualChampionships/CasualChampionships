package net.casual.championships.uhc.minigame

import com.google.gson.JsonObject
import eu.pb4.sgui.api.GuiHelpers
import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.extension.LevelBoundaryExtension.Companion.levelBoundary
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.dimensions.utils.deleteCustomLevel
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.block.BlockDropLootEvent
import net.casual.arcade.events.server.block.BrewingStandBrewEvent
import net.casual.arcade.events.server.entity.EntityBeforeLootEvent
import net.casual.arcade.events.server.entity.EntityStartTrackingEvent
import net.casual.arcade.events.server.level.LevelLootEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.events.threading.ThreadingTarget
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.annotation.ListenerFlags.IS_PLAYING
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.gamemode.ExtendedGameMode.Companion.extendedGameMode
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.minigame.utils.MinigameUtils.addEventListener
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.ResourcePackUtils.afterPacksLoad
import net.casual.arcade.resources.utils.spaced
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.resources.utils.withMiniShiftedDownFont
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.*
import net.casual.arcade.utils.ItemUtils.isOf
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.utils.MathUtils.isAbove
import net.casual.arcade.utils.MathUtils.isBelow
import net.casual.arcade.utils.PlayerUtils.boostHealth
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.dropItemStackIntoInventory
import net.casual.arcade.utils.PlayerUtils.getKillCreditWith
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.grantAllRecipesSilently
import net.casual.arcade.utils.PlayerUtils.resetExperience
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.resetHunger
import net.casual.arcade.utils.PlayerUtils.revokeAllAdvancements
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.server
import net.casual.arcade.utils.PlayerUtils.unboostHealth
import net.casual.arcade.utils.TeamUtils.color
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.TimeUtils.formatMMSS
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.math.location.Location.Companion.withRotation
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.elements.LevelSpecificElement
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate
import net.casual.arcade.visuals.shapes.ShapePoints.Companion.drawAsParticlesFor
import net.casual.arcade.visuals.shapes.impl.ArrowShape
import net.casual.arcade.visuals.sidebar.DynamicSidebar
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.sidebar.SidebarComponents
import net.casual.arcade.visuals.sidebar.SidebarComponents.Companion.addRow
import net.casual.arcade.visuals.utils.elements.ComponentElements
import net.casual.championships.common.event.ChunkGenerationMobSpawnEvent
import net.casual.championships.common.event.PlayerCheatEvent
import net.casual.championships.common.event.TippedArrowTradeOfferEvent
import net.casual.championships.common.event.portal.EntityPortalEntryPositionEvent
import net.casual.championships.common.event.portal.PortalCreateValidPositionEvent
import net.casual.championships.common.event.portal.PortalFindValidPositionEvent
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.items.minigame.PlayerHeadItem
import net.casual.championships.common.items.minigame.recipes.GoldenHeadRecipe
import net.casual.championships.common.minigame.rules.MinigameRulesProvider
import net.casual.championships.common.ui.bossbar.ActiveBossbar
import net.casual.championships.common.ui.elements.MinigamePhaseSidebarElement
import net.casual.championships.common.ui.elements.MobcapSidebarElement
import net.casual.championships.common.ui.elements.PerformanceSidebarElement
import net.casual.championships.common.ui.elements.TeammatesSidebarElements
import net.casual.championships.common.util.*
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.CasualGuiUtils.broadcastWithSound
import net.casual.championships.uhc.advancement.UHCAdvancementManager
import net.casual.championships.uhc.advancement.UHCAdvancements
import net.casual.championships.uhc.border.UHCBoundaryManager
import net.casual.championships.uhc.border.UHCBoundaryPhase
import net.casual.championships.uhc.gui.UHCMapRenderer
import net.casual.championships.uhc.gui.UHCSpectatorHotbar
import net.casual.championships.uhc.item.TMCStarterPack
import net.casual.championships.uhc.minigame.UHCPhase.GameOver
import net.casual.championships.uhc.minigame.UHCPhase.Initializing
import net.casual.championships.uhc.recipe.FlowerPowerRecipe
import net.casual.championships.uhc.recipe.HeavyCoreRecipe
import net.casual.championships.uhc.utils.UHCComponents
import net.casual.championships.uhc.utils.UHCDimensions
import net.casual.championships.uhc.utils.UHCMinigameRules
import net.casual.championships.uhc.utils.UHCStats
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.ChatFormatting
import net.minecraft.ChatFormatting.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.util.Mth
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

class UHCMinigame(
    server: MinecraftServer,
    uuid: UUID,
    nerfedPlayers: Set<UUID>,
    private val dimensions: UHCDimensions,
    private val factory: UHCMinigameFactory? = null
): Minigame(server, uuid), MinigameRulesProvider by UHCMinigameRules {
    override val id = ID

    private var lastBoundaryTime = 0.Ticks
    var boundaryPhase: UHCBoundaryPhase = UHCBoundaryPhase.First

    val mapRenderer = UHCMapRenderer(this)
    val uhcAdvancements = UHCAdvancementManager(this)

    override val settings = UHCSettings(this)

    val overworld: ServerLevel
        get() = this.dimensions.overworld.level
    val nether: ServerLevel
        get() = this.dimensions.nether.level
    val end: ServerLevel
        get() = this.dimensions.end.level

    init {
        this.tickrate.useGlobalManager = false

        this.visuals.addBossbar(ActiveBossbar(this))
        this.effects.setGlowingPredicate(PlayerObserverPredicate(this::shouldObserveeGlow))
        this.effects.setInvisiblePredicate(PlayerObserverPredicate(this::shouldObserveeBeInvisible))

        this.levels.addAll(this.dimensions.map { it.level })

        for (player in nerfedPlayers) {
            this.tags.add(player, NERFED)
        }
    }

    fun resetPlayerHealth(player: ServerPlayer) {
        player.boostHealth(this.settings.health)
        player.resetHealth()
    }

    fun onStartBoundary() {
        this.lastBoundaryTime = this.uptime.Ticks
    }

    fun onPauseBoundary() {
        this.lastBoundaryTime = this.uptime.Ticks
        this.chat.broadcastGame(component = CasualComponents.BORDER_PAUSED.withMiniFont().red())
    }

    fun onResumeBoundary() {
        this.lastBoundaryTime = this.uptime.Ticks
        this.chat.broadcastGame(
            component = CasualComponents.BORDER_RESUMED.withMiniFont().red(),
            sound = Sound(CasualSounds.GAME_BORDER_MOVING)
        )
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

    override fun load(data: JsonObject) {
        if (data.has("advancements")) {
            this.uhcAdvancements.deserialize(data.obj("advancements"))
        }
        if (data.has("boundary_phase")) {
            this.boundaryPhase = UHCBoundaryPhase.entries[data.int("boundary_phase")]
        }
        if (data.has("last_boundary_time")) {
            this.lastBoundaryTime = data.int("last_boundary_time").Ticks
        }
    }

    override fun save(data: JsonObject) {
        data.add("advancements", this.uhcAdvancements.serialize())
        data.addProperty("boundary_phase", UHCBoundaryPhase.entries.indexOf(this.boundaryPhase))
        data.addProperty("last_boundary_time", this.lastBoundaryTime.ticks)
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.commands.register(UHCMinigameCommands(this))

        this.addEventListener(this.uhcAdvancements)
        this.recipes.add(GoldenHeadRecipe.INSTANCE)
        if (this.settings.heavyHeads) {
            this.recipes.add(HeavyCoreRecipe.INSTANCE)
        }
        if (this.settings.flowerPower) {
            this.recipes.add(FlowerPowerRecipe.getOrCreate(this.server.registryAccess()))
        }
        this.advancements.addAll(UHCAdvancements)
        this.settings.enableChatCommand.set(true)

        val y =  this.overworld.getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0)
        this.levels.spawn = MinigameLevelManager.SpawnLocation.global(
            location = this.overworld.asLocation(Vec3(0.0, y.toDouble(), 0.0)),
            overridesPlayerSpawnPoint = true
        )

        this.visuals.setSidebar(this.createSidebar())
    }

    @Listener
    private fun onMinigameClose(event: MinigameCloseEvent) {
        for ((level, persist) in this.dimensions) {
            if (!persist) {
                this.server.deleteCustomLevel(level)
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
    private fun onEntityPortalEntryPosition(event: EntityPortalEntryPositionEvent) {
        val (level, _, pos) = event

        val boundary = level.levelBoundary ?: return

        val shrinkingSpeed = this.boundaryPhase.getSpeedInBlocksPerTick(level)
        if (shrinkingSpeed <= 0) {
            val box = boundary.getAABB()
            event.cancel(BlockPos.containing(
                Mth.clamp(pos.x, box.minX, box.maxX),
                Mth.clamp(pos.y, box.minY, box.maxY),
                Mth.clamp(pos.z, box.minZ, box.maxZ)
            ))
            return
        }

        val margin = shrinkingSpeed * this.settings.portalEscapeTime.ticks
        if (margin >= boundary.getSize().x * 0.5) {
            val (x, y, z) = boundary.getCenter()
            // The border would reach size 0 within 30 seconds
            event.cancel(BlockPos.containing(x, y, z))
            return
        }

        val box = boundary.getAABB()
        event.cancel(BlockPos.containing(
            Mth.clamp(pos.x, box.minX + margin, box.maxX - margin),
            Mth.clamp(pos.y, box.minY + margin, box.maxY - margin),
            Mth.clamp(pos.z, box.minZ + margin, box.maxZ - margin)
        ))
    }

    @Listener
    private fun onPortalCreateValidPosition(event: PortalCreateValidPositionEvent) {
        val (level, position) = event
        val boundary = level.levelBoundary ?: return
        event.and { this.isPositionValidForPortal(level, position, boundary) }
    }

    @Listener
    private fun onPortalFindValidPosition(event: PortalFindValidPositionEvent) {
        val (level, position) = event
        val boundary = level.levelBoundary ?: return
        event.and { this.isPositionValidForPortal(level, position, boundary) }
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
            this.updateBoundaryInfo(player)
            this.updatePedalToTheMetal(player)
        } else if (!player.isCreative) {
            val interval = 20.Minutes.ticks
            if (this.uptime % interval == interval - 1) {
                this.chat.broadcastInfo(UHCMinigameRules.getFormattedSpectatorRules(), listOf(player))
            }

            val gui = GuiHelpers.getCurrentGui(player)
            if (gui == null && player.containerMenu == player.inventoryMenu) {
                UHCSpectatorHotbar(event.player, this).open()
            }
        }

        this.updateHUD(player)
    }

    @Listener
    private fun onLevelLoot(event: LevelLootEvent) {
        for (item in event.items) {
            val contents = item.get(DataComponents.POTION_CONTENTS) ?: continue
            val potion = contents.potion
            if (potion.isPresent) {
                val replacement = this.replacePotion(potion.get())
                item.set(DataComponents.POTION_CONTENTS, contents.withPotion(replacement))
            }
        }
    }

    @Listener(flags = IS_PLAYING, phase = BuiltInEventPhases.POST)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val (player, source) = event

        GlobalTickedScheduler.schedule(1.Seconds) {
            ReplayPlayerRecorders.get(player).forEach { it.stop() }
        }

        this.onEliminated(player, player.getKillCreditWith(source))
    }

    @Listener(flags = ListenerFlags.HAS_PLAYER)
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.lastDeathLocation.ifPresent { pos ->
            val level = player.server.getLevel(pos.dimension)
            if (level != null && this.levels.has(level)) {
                val location = pos.pos.center.withRotation(player.rotationVector).with(level)
                player.teleportTo(location)
            }
        }
    }

    @Listener
    private fun onTippedArrowTradeOffer(event: TippedArrowTradeOfferEvent) {
        event.potion = this.replacePotion(event.potion)
    }

    @Listener
    private fun onPlayerAttack(event: PlayerAttackEvent) {
        if (this.tags.has(event.player, NERFED) && event.target is ServerPlayer) {
            event.damage *= 1 - this.settings.nerfedPlayerDamage
        }
    }

    @Listener
    private fun onPlayerInteract(event: PlayerEntityInteractionEvent) {
        val (player, target) = event
        if (this.settings.helpingHand && player.mainHandItem.isEmpty && target is ServerPlayer) {
            if (player.team == target.team) {
                target.startRiding(player)
            }
        }
    }

    @Listener
    private fun onPlayerAttack(event: PlayerTryAttackEvent) {
        val (player, target) = event
        if (this.settings.helpingHand && player.mainHandItem.isEmpty && target.vehicle == player) {
            target.stopRiding()
            val direction = player.lookAngle.normalize()
            val modified = MathUtils.min(direction, Vec3(5.0, 0.5, 5.0))
                .add(player.deltaMovement)
            target.setVelocityAndMark(modified.x, modified.y, modified.z)
            event.cancel()
        }
    }

    @Listener
    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        if (event.player.vehicle is ServerPlayer) {
            event.player.stopRiding()
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

    @Listener
    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        val (player, stack) = event
        if (stack.isOf(Items.BOW)) {
            player.cooldowns.addCooldown(stack, this.settings.bowCooldown.ticks)
        }
    }

    @Listener
    private fun onBlockMined(event: PlayerBlockMinedEvent) {
        val (player, _, state, _) = event
        if (this.settings.bloodDiamonds) {
            if (state.isOf(BlockTags.DIAMOND_ORES)) {
                player.hurtServer(player.level(), player.damageSources().magic(), 1.0F)
            }
        }
    }

    @Listener
    private fun onBlockDrop(event: BlockDropLootEvent) {
        if (!this.settings.instantSmeltOres) {
            return
        }

        val entity = event.params.getOptionalParameter(LootContextParams.THIS_ENTITY)
        if (entity is ServerPlayer) {
            if (event.state.isOf(ConventionalBlockTags.ORES)) {
                val tool = event.params.getOptionalParameter(LootContextParams.TOOL) ?: return
                val silkTouch = tool.enchantments.keySet().any { it.isOf(Enchantments.SILK_TOUCH) }
                if (silkTouch) {
                    return
                }
                event.drops = event.drops.map { item ->
                    val input = SingleRecipeInput(item)
                    val recipe = this.server.recipeManager.getRecipeFor(RecipeType.SMELTING, input, event.level)
                    recipe.getOrNull()?.value?.assemble(input, event.level.registryAccess()) ?: item
                }
            }
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
    private fun onPlayerAdvancement(event: PlayerAdvancementEvent) {
        if (event.player.isSpectator) {
            event.reward = false
        }
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
    private fun onPlayerSpectatorTeleport(event: PlayerSpectatorTeleportEvent) {
        val (player, _) = event
        if (player.isSpectator && this.players.isSpectating(player)) {
            val target = event.getTarget()
            if (target is ServerPlayer) {
                player.teleportTo(target.locationWithLevel)
            }
        }
        event.cancel()
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

        GlobalTickedScheduler.schedule(1.Seconds) {
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
    }

    @Listener
    private fun onSetPlaying(event: MinigameSetPlayingEvent) {
        val player = event.player
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
        team?.nameTagVisibility = Team.Visibility.NEVER
        team?.collisionRule = Team.CollisionRule.ALWAYS

        this.tags.add(player, CasualTags.HAS_PARTICIPATED)
        this.tags.add(player, CasualTags.HAS_TEAM_GLOW)

        player.addEffect(
            MobEffectInstance(MobEffects.RESISTANCE, 2000, 255, true, false)
        )
        player.setGameMode(GameType.SURVIVAL)
        player.afterPacksLoad {
            player.removeEffect(MobEffects.RESISTANCE)
            player.addEffect(
                MobEffectInstance(MobEffects.RESISTANCE, 200, 255, true, false)
            )
        }

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
    private fun onSetSpectating(event: MinigameSetSpectatingEvent) {
        val (_, player) = event
        player.extendedGameMode = ExtendedGameMode.AdventureSpectator

        this.effects.addFullbright(player)
        this.tags.remove(player, CasualTags.HAS_TEAM_GLOW)

        if (!this.levels.has(player.level())) {
            player.teleportTo(this.overworld.asLocation(Vec3(0.0, 128.0, 0.0)))
        }

        this.scheduler.schedule(1.Ticks) {
            this.chat.broadcastInfo(UHCMinigameRules.getFormattedSpectatorRules(), listOf(player))
        }
    }

    @Listener
    private fun onLoadSpectating(event: MinigameLoadSpectatingEvent) {
        val player = event.player
        this.mapRenderer.startWatching(player)
    }

    @Listener(flags = ListenerFlags.IS_SPECTATOR)
    private fun onPlayerSneak(event: PlayerSetSneakingEvent) {
        val (player, sneaking) = event
        if (!player.isShiftKeyDown && sneaking) {
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
    private fun onPlayerBlockDropLoot(event: PlayerBlockDropLootEvent) {
        val (player) = event
        if (this.settings.handsFree && player.isShiftKeyDown) {
            event.drops.forEach { player.dropItemStackIntoInventory(it) { } }
        }
    }

    @Listener
    private fun onEntityTrack(event: EntityStartTrackingEvent) {
        val (entity) = event
        if (this.settings.mobMash && entity is LivingEntity && entity !is ServerPlayer) {
            val scale = entity.attributes.getInstance(Attributes.SCALE)
            if (scale != null && !scale.hasModifier(MOB_MASH)) {
                val value = if (entity.random.nextDouble() < 0.02) 9.0 else entity.random.nextDouble() - 0.4
                val modifier = AttributeModifier(MOB_MASH, value, AttributeModifier.Operation.ADD_VALUE)
                scale.addPermanentModifier(modifier)
            }
        }
    }

    @Listener
    private fun onEntityBeforeLoot(event: EntityBeforeLootEvent) {
        event.lootMultiplier *= MOB_LOOT_MULTIPLIER
    }

    @Listener(strategy = ThreadingTarget.UseCurrentThread)
    private fun onChunkGenerationMobSpawn(event: ChunkGenerationMobSpawnEvent) {
        event.probability *= MOB_SPAWN_PROBABILITY
    }

    @Listener
    private fun onPlayerCheat(event: PlayerCheatEvent) {
        val message = Component {
            literal("Player ") + event.player.displayName!! + literal(" tried to cheated with ${event.type}")
        }
        this.chat.broadcastInfo(message, this.players.admins)
    }

    private fun isPositionValidForPortal(level: ServerLevel, position: BlockPos, boundary: LevelBoundary): Boolean {
        val shrinkingSpeed = this.boundaryPhase.getSpeedInBlocksPerTick(level)
        if (shrinkingSpeed <= 0) {
            // The border is static or expanding
            return boundary.contains(position) == BoundaryShape.Containment.Full
        }

        val margin = shrinkingSpeed * this.settings.portalEscapeTime.ticks
        val size = boundary.getSize()
        val xMargin = margin.coerceAtMost(size.x * 0.5 - 1)
        val yMargin = margin.coerceAtMost(size.y * 0.5 - 1)
        val zMargin = margin.coerceAtMost(size.z * 0.5 - 1)
        val box = boundary.getAABB()
        return position.x >= box.minX + xMargin
            && position.x + 1 <= box.maxX - xMargin
            && position.y >= box.minY + yMargin
            && position.y + 1 <= box.maxY - yMargin
            && position.z >= box.minZ + zMargin
            && position.z + 1 <= box.maxZ - zMargin
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
                CasualComponents.HAS_BEEN_ELIMINATED.generate(team.name).color(team).bold().withMiniFont(),
                Sound(CasualSounds.TEAM_ELIMINATION)
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

    private fun updateBoundaryInfo(player: ServerPlayer) {
        val level = player.level()
        val boundary = level.levelBoundary ?: return

        val position = player.position()
        if (boundary.contains(position)) {
            return
        }

        val box = boundary.getAABB()
        when {
            box.isAbove(position) -> this.handleOutsideBorderVertically(player, Direction.DOWN)
            box.isBelow(position) -> this.handleOutsideBorderVertically(player, Direction.UP)
            else -> this.handleOutsideBorderHorizontally(player, level, boundary)
        }
    }

    private fun handleOutsideBorderVertically(player: ServerPlayer, direction: Direction) {
        if (this.uptime % 200 == 0) {
            player.sendTitle(
                Component.empty(),
                CasualComponents.INSIDE_BORDER.generate(CasualComponents.direction(direction).lime()).withMiniFont()
            )
        }
    }

    private fun handleOutsideBorderHorizontally(player: ServerPlayer, level: ServerLevel, boundary: LevelBoundary) {
        val vector = boundary.getDirectionFrom(player.eyePosition)

        val start = player.eyePosition.add(0.0, 4.0, 0.0)
        val end = start.add(vector.normalize())

        for (i in 1..2) {
            val top = start.lerp(end, 1.5 * i)
            val bottom = top.subtract(0.0, 10.0, 0.0)
            val hit = level.clip(ClipContext(top, bottom, ClipContext.Block.VISUAL, ClipContext.Fluid.SOURCE_ONLY, player))

            if (hit.type != HitResult.Type.MISS) {
                val position = hit.blockPos
                val rotation = atan2(vector.x, vector.z)

                val arrow = ArrowShape.createHorizontalCentred(
                    position.x, hit.location.y + 0.1, position.z, 1.0, rotation
                )
                arrow.drawAsParticlesFor(player, pointsPerUnit = 10.0)
            }
        }

        val direction = MathUtils.getDirection8(vector)
        if (this.uptime % 200 == 0) {
            player.sendTitle(
                Component.empty(),
                CasualComponents.INSIDE_BORDER.generate(CasualComponents.direction(direction).lime()).withMiniFont()
            )
        }
    }

    private fun updatePedalToTheMetal(player: ServerPlayer) {
        if (!this.settings.pedalToTheMetal) {
            return
        }

        val teammates = player.team?.getOnlinePlayers()?.filter { it.isAlive } ?: return
        for (teammate in teammates) {
            if (teammate != player && teammate.closerThan(player, 50.0)) {
                return
            }
        }
        player.addEffect(MobEffectInstance(
            MobEffects.SPEED, 5.Seconds.ticks + 5, 0, false, false, false
        ))
    }

    private fun createSidebar(): DynamicSidebar {
        val sidebar = DynamicSidebar(ComponentElements.of(UHCComponents.Bitmap.TITLE))
        val buffer = SpacingFontResources.spaced(4)
        val border = CasualGuiUtils.getBorderSidebarElements(buffer)
        val pause = BorderMovingInfo(buffer).cached()
        val teammates = TeammatesSidebarElements(Component.empty(), buffer, true)
        val performance = PerformanceSidebarElement(SpacingFontResources.spaced(2)).cached()
        val phase = MinigamePhaseSidebarElement(this, SpacingFontResources.spaced(2)).cached()
        val mobcaps = MobcapSidebarElement.cached()
        sidebar.setRows(PlayerSpecificElement.composed(*border, performance, phase, mobcaps, pause) { player ->
            val components = SidebarComponents.empty()

            val team = player.team
            if (team != null && !this.teams.isAdminTeam(team) && !this.teams.isSpectatorTeam(team)) {
                teammates.addTeammates(player, components)
                components.addRow(SidebarComponent.EMPTY)
            }

            if (this.players.isAdmin(player)) {
                components.addRow(performance.get(player))
                components.addRow(phase.get(player))
                components.addRow(SidebarComponent.EMPTY)
                components.addRow(Component { empty() + spaced(2.0F) + "Mobcaps:" }.withMiniFont())
                components.addRow(mobcaps.get(player))
                components.addRow(SidebarComponent.EMPTY)
            }

            if (components.size() == 0) {
                components.addRow(SidebarComponent.EMPTY)
            }
            border.forEach { components.addRow(it.get(player)) }
            components.addRow(pause.get(player))
            components.addRow(SidebarComponent.EMPTY)
        })
        return sidebar
    }

    private fun updateHUD(player: ServerPlayer) {
        val direction = CasualComponents.direction(Direction.orderedByNearest(player).filter { it.axis != Direction.Axis.Y }[0])
        val position = "(${player.blockX}, ${player.blockY}, ${player.blockZ})"
        val shift = position.length - 7

        val mode = this.chat.getChatModeFor(player)
        player.connection.send(ClientboundSetActionBarTextPacket(
            Component.empty().apply {
                append(SpacingFontResources.spaced(26))
                append(SpacingFontResources.spaced(shift * 6))
                append(ComponentUtils.negativeWidthOf(mode.name))
                append(SpacingFontResources.spaced(-11))
                append(CasualComponents.Hud.EPIC_CHAT_ICON_M54)
                append(SpacingFontResources.spaced(1))
                append(Component.empty().append(mode.name).withMiniShiftedDownFont(63))

                append(SpacingFontResources.spaced(190))
                append(direction.withMiniShiftedDownFont(54))
                append(ComponentUtils.negativeWidthOf(direction))
                append(SpacingFontResources.spaced(-2))
                append(Component.literal(position).withMiniShiftedDownFont(63))
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
        if (!this.tags.has(observer, CasualTags.HAS_TEAM_GLOW)) {
            return false
        }
        return observee.team != null && observee.team === observer.team
    }

    private fun shouldObserveeBeInvisible(observee: ServerPlayer, observer: ServerPlayer): Boolean {
        return this.players.isSpectating(observee) && observee !== observer
    }

    private fun replacePotion(potion: Holder<Potion>): Holder<Potion> {
        return when (potion.value()) {
            Potions.HEALING.value(), Potions.STRONG_HEALING.value(), Potions.STRONG_REGENERATION.value() -> Potions.REGENERATION
            Potions.STRONG_POISON.value() -> Potions.POISON
            Potions.STRONG_HARMING.value() -> Potions.HARMING
            Potions.STRONG_LEAPING.value() -> Potions.LEAPING
            Potions.STRONG_SLOWNESS.value() -> Potions.SLOWNESS
            Potions.STRONG_STRENGTH.value() -> Potions.STRENGTH
            Potions.STRONG_TURTLE_MASTER.value() -> Potions.TURTLE_MASTER
            else -> potion
        }
    }

    private fun isFinalStage(level: ServerLevel): Boolean {
        return UHCBoundaryManager.getFinalPhase(this, level) <= this.boundaryPhase
    }

    private inner class BorderMovingInfo(private val buffer: Component): LevelSpecificElement<SidebarComponent> {
        override fun get(level: ServerLevel): SidebarComponent {
            val boundary = level.levelBoundary ?: return SidebarComponent.EMPTY
            if (boundary.shape.getStatus().isMoving()) {
                val remainingTime = boundaryPhase.getDuration(settings.borderTime) - (uptime.Ticks - lastBoundaryTime)
                return SidebarComponent.withCustomScore(
                    this.buffer.wrap().append(this.buffer).append(Component.translatable("casual.game.borderPausingIn").withMiniFont()),
                    Component.literal(remainingTime.formatMMSS()).withStyle(colorTime(remainingTime)).withMiniFont().append(this.buffer)
                )
            }
            if (isFinalStage(level)) {
                return SidebarComponent.withNoScore(
                    this.buffer.wrap().append(this.buffer).append(Component.translatable("casual.game.borderFinished").withMiniFont())
                )
            }

            val cooldown = when {
                boundaryPhase == UHCBoundaryPhase.First -> settings.gracePeriod
                else -> boundaryPhase.getCooldown(settings.borderTime)
            }
            val remainingTime = cooldown - (uptime.Ticks - lastBoundaryTime)
            return SidebarComponent.withCustomScore(
                this.buffer.wrap().append(this.buffer).append(Component.translatable("casual.game.borderMovingIn").withMiniFont()),
                Component.literal(remainingTime.formatMMSS()).withStyle(colorTime(remainingTime)).append(buffer).withMiniFont()
            )
        }

        private fun colorTime(time: MinecraftTimeDuration): ChatFormatting {
            return when {
                time > 30.Minutes -> DARK_GREEN
                time > 15.Minutes -> GREEN
                time > 8.Minutes -> YELLOW
                time > 3.Minutes -> GOLD
                time > 1.Minutes -> RED
                else -> DARK_RED
            }
        }
    }

    companion object {
        private const val MOB_SPAWN_PROBABILITY = 1.0F / 2.0F
        private val MOB_LOOT_MULTIPLIER = (1.0F / MOB_SPAWN_PROBABILITY).roundToInt()

        private val MOB_MASH = casual("mob_mash")
        private val NERFED = casual("nerfed")

        val ID = casual("uhc_minigame")
    }
}