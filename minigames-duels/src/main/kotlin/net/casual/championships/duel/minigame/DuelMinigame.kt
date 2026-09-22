package net.casual.championships.duel.minigame

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.getDimensionPath
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.level.LevelBlockChangedEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.restrictMovement
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.unrestrictMovement
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter.Companion.teleport
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.suggestCommand
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.level.resetToDefault
import net.casual.arcade.utils.level.set
import net.casual.arcade.utils.math.location.asLocation
import net.casual.arcade.utils.player.clearPlayerInventory
import net.casual.arcade.utils.player.resetHealth
import net.casual.arcade.utils.player.sendTitle
import net.casual.arcade.utils.player.server
import net.casual.arcade.utils.registries.isOf
import net.casual.arcade.utils.registries.toKey
import net.casual.arcade.utils.scoreboard.color
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.championships.common.event.LevelFluidTrySpreadEvent
import net.casual.championships.common.items.minigame.PlayerHeadItem
import net.casual.championships.common.items.minigame.recipes.GoldenHeadRecipe
import net.casual.championships.common.minigame.CasualTimeTracker
import net.casual.championships.common.minigame.TimeTrackedMinigame
import net.casual.championships.common.ui.bossbar.ActiveBossbar
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.CasualStats
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.common.util.RuleUtils
import net.casual.championships.common.util.casual
import net.casual.championships.common.util.player.boostHealth
import net.casual.championships.common.util.player.unboostHealth
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.ItemTags
import net.minecraft.util.Prediction
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.random.Random

class DuelMinigame(
    server: MinecraftServer,
    uuid: UUID,
    val duelSettings: DuelSettings,
    val duelArena: DuelArenasDataModule.DuelArena
): Minigame(server, uuid, ID, DuelPhase.entries), TimeTrackedMinigame {
    private val lootSeed = Random.nextLong()
    private val modifiableBlocks = HashSet<BlockPos>()
    private var emptyTicks = 0

    private val level: ServerLevel = this.createLevel()

    override val settings = MinigameSettings(this)
    override val timeTracker = CasualTimeTracker()

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.initializePhases()

        this.tickrate.useGlobalManager = false

        this.settings.copyFrom(this.duelSettings)
        this.recipes.add(GoldenHeadRecipe.INSTANCE)

        this.effects.setGlowingPredicate({ observee, observer ->
            this.players.isPlaying(observee) && (this.players.isSpectating(observer) || this.duelSettings.glowing)
        }, false)

        this.levels.spawn = MinigameLevelManager.SpawnLocation.global(
            this.level.asLocation(Vec3.atBottomCenterOf(this.duelArena.data.spawn))
        )

        this.visuals.addNametag(CasualGuiUtils.createPlayingHealthTag(this))
    }

    @Listener
    private fun onTick(event: ServerTickEvent) {
        if (this.players.playingPlayerCount <= 1) {
            this.emptyTicks++
            if (this.emptyTicks.Ticks > 30.Seconds) {
                this.close()
            }
        } else {
            this.emptyTicks = 0
        }
    }

    @Listener
    private fun onLevelBlockChanged(event: LevelBlockChangedEvent) {
        if (this.state < DuelPhase.Dueling) {
            return
        }

        val context = DirectionalPlaceContext(event.level, event.pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
        if ((event.old.canBeReplaced() || event.old.canBeReplaced(context)) && !event.new.isAir) {
            this.modifiableBlocks.add(event.pos)
        }
    }

    @Listener(priority = Int.MAX_VALUE, phase = BuiltInEventPhases.POST)
    private fun onPlayerBlockPlaced(event: PlayerBlockPlacedEvent) {
        this.modifiableBlocks.add(event.context.clickedPos)
    }

    @Listener
    private fun onPlayerBlockStartMining(event: PlayerBlockStartMiningEvent) {
        if (!this.modifiableBlocks.contains(event.pos)) {
            event.cancel()
        }
    }

    @Listener
    private fun onPlayerBlockInteraction(event: PlayerBlockInteractionEvent) {
        if (!this.modifiableBlocks.contains(event.result.blockPos)) {
            event.preventUsingOnBlock()
            if (event.stack.item !is BlockItem) {
                event.cancel(InteractionResult.PASS)
            }
        }
    }

    @Listener
    private fun onPlayerItemUse(event: PlayerItemUseEvent) {
        if (event.stack.isOf(ItemTags.BOATS)) {
            event.cancel(InteractionResult.PASS)
        }
    }

    @Listener
    private fun onPlayerTryHarm(event: PlayerTryHarmEvent) {
         if (!this.duelSettings.teams) {
             event.canHarmOtherPlayer = true
         }
    }

    @Listener
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!this.state.isAt(DuelPhase.Dueling)) {
            return
        }

        val player = event.player
        val killer = player.killCredit

        // This will prevent loot being dropped
        this.players.setSpectating(player)

        if (this.duelSettings.playerDropsHead) {
            val head = PlayerHeadItem.create(player)
            if (killer is ServerPlayer) {
                if (!killer.inventory.add(head)) {
                    player.drop(head, true, Prediction.SERVER_ONLY)
                }
            } else {
                player.drop(head, true, Prediction.SERVER_ONLY)
            }
        }

        val remaining = if (!this.duelSettings.teams) this.players.playing else this.teams.getPlayingTeams()
        if (remaining.size <= 1) {
            this.phases.set(DuelPhase.Complete)
        }
    }

    @Listener
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.lastDeathLocation.ifPresent { location ->
            val level = player.server.getLevel(location.dimension)
            if (level != null && this.levels.has(level) && player.isSpectator) {
                player.teleportTo(level.asLocation(Vec3.atCenterOf(location.pos)))
            }
        }
    }

    @Listener
    private fun onMinigameSetSpectating(event: MinigameSetSpectatingEvent) {
        event.player.setGameMode(GameType.SPECTATOR)

        if (event.player.level() != this.level) {
            this.duelArena.data.teleporter.teleportEntities(this.level, listOf(event.player))
        }
    }

    @Listener
    private fun onMinigameSetPlaying(event: MinigameSetPlayingEvent) {
        val player = event.player

        this.recipes.grant(player, GoldenHeadRecipe.INSTANCE.id, true)
        player.setGameMode(GameType.SURVIVAL)
        player.boostHealth(this.duelSettings.health)
        player.resetHealth()

        val stacks = this.duelSettings.getSelectedKit().lootTable.getRandomItems(
            LootParams.Builder(player.level()).create(ContextKeySet.Builder().build()),
            this.lootSeed
        )

        player.clearPlayerInventory()
        for (stack in stacks) {
            val armor = stack.get(DataComponents.EQUIPPABLE)
            when {
                armor != null -> {
                    player.setItemSlot(armor.slot, stack)
                    continue
                }
                player.offhandItem.isEmpty && stack.item == Items.WIND_CHARGE -> {
                    player.setItemInHand(InteractionHand.OFF_HAND, stack)
                    continue
                }
            }
            player.inventory.add(stack)
        }
    }

    @Listener
    private fun onMinigameAddPlayer(event: MinigameAddNewPlayerEvent) {
        val leave = Component.literal("/duel leave").withMiniFont()
            .bold().color(0x65b7db).suggestCommand("/duel leave")
        val message = RuleUtils.formatLine(Component.translatable("casual.duel.leaveDuel", leave))
        this.chat.broadcastInfo(message, listOf(event.player))
    }

    @Listener
    private fun onMinigameRemovePlayer(event: MinigameRemovePlayerEvent) {
        event.player.unboostHealth()
        event.player.removeAllEffects()
        event.player.unrestrictMovement()
    }

    @Listener
    private fun onPlayerAdvancement(event: PlayerAdvancementEvent) {
        event.announce = false
    }

    @Listener
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        if (player.isSpectator) {
            event.cancel()
        }
    }

    @Listener
    private fun onFluidTrySpread(event: LevelFluidTrySpreadEvent) {
        if (!this.modifiableBlocks.contains(event.spreadPos) && !event.spreadBlockState.isAir) {
            event.canSpread = false
        }
    }

    private fun createLevel(): CustomLevel {
        val level = this.levels.create(casual("overworld")) {
            spoofedDimensionKey(casual("duel"))
            randomDimensionKey()
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(server))
            gameRules { }
        }
        this.duelArena.world.extract(this.server.getDimensionPath(level.dimension()))
        return level
    }

    private fun initializePhases() {
        this.phases.coroutines[DuelPhase.Initializing] = this::runInitializingLogic
        this.phases.coroutines[DuelPhase.Countdown] = this::runCountdownLogic
        this.phases.coroutines[DuelPhase.Dueling] = this::runDuelingLogic
        this.phases.coroutines[DuelPhase.Complete] = this::runCompletionLogic
    }

    private suspend fun runInitializingLogic() {
        this.levels.setGameRules {
            resetToDefault()
            set(GameRules.IMMEDIATE_RESPAWN, true, server)
            set(GameRules.LOCATOR_BAR, false, server)
            set(GameRules.COMMAND_BLOCK_OUTPUT, false)
            set(GameRules.RANDOM_TICK_SPEED, 0)
            if (!duelSettings.naturalRegen) {
                set(GameRules.NATURAL_HEALTH_REGENERATION, false)
            }
        }

        this.visuals.addBossbar(ActiveBossbar.create(this))

        this.duelArena.data.teleporter.teleport(this.level, this.players.playing, this.duelSettings.teams)

        this.settings.canInteractAll = false
        this.settings.canAttackEntities.set(false)
    }

    private suspend fun runCountdownLogic() {
        for (player in this.players.playing) {
            player.restrictMovement()
        }
        this.visuals.countdown.transition(players = this.players::all)

        for (player in this.players.playing) {
            player.unrestrictMovement()
        }
    }

    private suspend fun runDuelingLogic() {
        this.settings.canInteractAll = true
        this.settings.canAttackEntities.set(true)

        awaitCancellation()
    }

    private suspend fun runCompletionLogic() {
        var winner = if (this.duelSettings.teams) {
            val winners = this.teams.getPlayingTeams().firstOrNull()
            if (winners != null) {
                for (winner in winners.getOnlinePlayers()) {
                    this.stats.getOrCreateStat(winner, CasualStats.WON).modify { true }
                }
            }
            winners?.formattedDisplayName
        } else {
            val winner = this.players.playing.firstOrNull()
            if (winner != null) {
                this.stats.getOrCreateStat(winner, CasualStats.WON).modify { true }
                val named = Component.literal(winner.scoreboardName)
                val team = winner.team
                if (team != null) {
                    named.color(team)
                }
                named
            } else {
                null
            }
        }
        if (winner == null) {
            CasualUtils.logger.warn("Couldn't find winner for duel!")
            winner = Component.literal("Unknown").withStyle(ChatFormatting.OBFUSCATED)
        }

        val title = CasualComponents.GAME_WON.generate(winner)
        for (player in this.players) {
            player.sendTitle(title)
        }
        this.stats.freeze()

        delay(10.Seconds)
        this.complete()
    }

    companion object {
        val ID = casual("duel_minigame")
    }
}