package net.casual.championships.duel.minigame

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.getDimensionPath
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.level.LevelBlockChangedEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.unrestrictMovement
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.isOf
import net.casual.arcade.utils.LootTableUtils
import net.casual.arcade.utils.LootTableUtils.addItem
import net.casual.arcade.utils.LootTableUtils.between
import net.casual.arcade.utils.LootTableUtils.count
import net.casual.arcade.utils.LootTableUtils.createPool
import net.casual.arcade.utils.LootTableUtils.durability
import net.casual.arcade.utils.LootTableUtils.enchant
import net.casual.arcade.utils.LootTableUtils.exactly
import net.casual.arcade.utils.PlayerUtils.boostHealth
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.arcade.utils.PlayerUtils.server
import net.casual.arcade.utils.PlayerUtils.unboostHealth
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.suggestCommand
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.utils.toKey
import net.casual.championships.common.event.LevelFluidTrySpreadEvent
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.items.minigame.PlayerHeadItem
import net.casual.championships.common.items.minigame.recipes.GoldenHeadRecipe
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastInfo
import net.casual.championships.common.util.CasualPredicates.OBSERVEE_NOT_MINIGAME_SPECTATOR
import net.casual.championships.common.util.CasualPredicates.VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES
import net.casual.championships.common.util.RuleUtils
import net.casual.championships.common.util.casual
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.ItemTags
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.DirectionalPlaceContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.LootTable
import java.util.*
import kotlin.random.Random

class DuelMinigame(
    server: MinecraftServer,
    uuid: UUID,
    val duelSettings: DuelSettings,
    val duelArena: DuelArenasDataModule.DuelArena
): Minigame(server, uuid) {
    override val id = ID

    private val lootSeed = Random.nextLong()
    private val modifiableBlocks = HashSet<BlockPos>()
    private var emptyTicks = 0

    val level: ServerLevel = this.createLevel()

    override val settings = MinigameSettings(this)

    init {
        this.tickrate.useGlobalManager = false
    }

    override fun phases(): Collection<Phase<DuelMinigame>> {
        return DuelPhase.entries
    }

    @Listener
    private fun onInitialize(event: MinigameInitializeEvent) {
        this.settings.copyFrom(this.duelSettings)
        this.recipes.add(GoldenHeadRecipe.INSTANCE)

        this.effects.setGlowingPredicate({ observee, observer ->
            this.players.isPlaying(observee) && (this.players.isSpectating(observer) || this.duelSettings.glowing)
        }, false)

        this.levels.spawn = MinigameLevelManager.SpawnLocation.global(
            this.level.asLocation(this.duelArena.data.spawn.bottomCenter)
        )

        this.visuals.addNametag(CasualGuiUtils.createPlayingHealthTag(
            VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES.and(OBSERVEE_NOT_MINIGAME_SPECTATOR)
        ))
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

    @Listener(during = During(after = DUELING_ID))
    private fun onLevelBlockChanged(event: LevelBlockChangedEvent) {
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
             event.canHarmOtherBoolean = true
         }
    }

    @Listener(during = During(phases = [DUELING_ID]))
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val killer = player.killCredit

        // This will prevent loot being dropped
        this.players.setSpectating(player)

        if (this.duelSettings.playerDropsHead) {
            val head = PlayerHeadItem.create(player)
            if (killer is ServerPlayer) {
                if (!killer.inventory.add(head)) {
                    player.drop(head, true, false)
                }
            } else {
                player.drop(head, true, false)
            }
        }

        val remaining = if (!this.duelSettings.teams) this.players.playing else this.teams.getPlayingTeams()
        if (remaining.size <= 1) {
            this.setPhase(DuelPhase.Complete)
        }
    }

    @Listener(flags = ListenerFlags.HAS_PLAYER)
    private fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        player.lastDeathLocation.ifPresent { location ->
            val level = player.server.getLevel(location.dimension)
            if (level != null && this.levels.has(level) && player.isSpectator) {
                player.teleportTo(level.asLocation(location.pos.center))
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

        val stacks = getOrCreateLootTable(this.server.registryAccess()).getRandomItems(
            LootParams.Builder(player.level()).create(ContextKeySet.Builder().build()),
            this.lootSeed
        )

        player.clearPlayerInventory()
        for (stack in stacks) {
            val armor = stack.get(DataComponents.EQUIPPABLE)
            if (armor != null) {
                player.setItemSlot(armor.slot, stack)
                continue
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
        val dimension = casual(UUID.randomUUID().toString()).toKey(Registries.DIMENSION)
        this.duelArena.world.extract(this.server.getDimensionPath(dimension))
        val level = CustomLevelBuilder.build(this.server) {
            spoofedDimensionKey(casual("duel"))
            dimensionKey(dimension)
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(server))
            gameRules {  }
        }
        this.levels.add(level)
        return level
    }

    companion object {
        private var table: LootTable? = null
        val ID = casual("duel_minigame")

        private fun getOrCreateLootTable(provider: HolderLookup.Provider): LootTable {
            var table = table
            if (table != null) {
                return table
            }
            table = LootTableUtils.create {
                createPool {
                    createPool {
                        addItem(Items.IRON_SWORD) {
                            enchant(provider, exactly(10))
                            durability(between(0.8, 0.99F))
                            setWeight(4)
                        }
                        addItem(Items.DIAMOND_SWORD) {
                            enchant(provider, exactly(10))
                            durability(between(0.8, 0.99F))
                            setWeight(2)
                        }
                    }
                    createPool {
                        addItem(Items.IRON_PICKAXE) {
                            durability(between(0.8, 0.99F))
                        }
                    }
                    addItem(Items.STONE_AXE) {
                        durability(between(0.8, 0.99F))
                        setWeight(4)
                    }
                    addItem(Items.IRON_AXE) {
                        durability(between(0.8, 0.99F))
                        setWeight(2)
                    }
                }
                createPool {
                    addItem(Items.STONE_SHOVEL) {
                        durability(between(0.8, 0.99F))
                        setWeight(4)
                    }
                    addItem(Items.IRON_SHOVEL) {
                        durability(between(0.8, 0.99F))
                        setWeight(2)
                    }
                }
                createPool {
                    addItem(Items.SHIELD) {
                        durability(between(0.8, 0.99F))
                    }
                }
                createPool {
                    addItem(Items.CROSSBOW) {
                        setWeight(2)
                    }
                    addItem(Items.BOW) {
                        enchant(provider, exactly(10))
                        setWeight(4)
                    }
                }
                createPool {
                    setRolls(exactly(3))
                    addItem(Items.GOLDEN_APPLE) {
                        count(between(1, 2))
                        setWeight(4)
                    }
                    addItem(CasualItems.PLAYER_HEAD) {
                        setWeight(2)
                    }
                    addItem(CasualItems.GOLDEN_HEAD) {
                        setWeight(1)
                    }
                }
                createPool {
                    setRolls(exactly(4))
                    addItem(Items.OAK_PLANKS) {
                        count(between(32, 64))
                        setWeight(3)
                    }
                    addItem(Items.COBBLESTONE) {
                        count(between(32 ,64))
                        setWeight(4)
                    }
                    addItem(Items.SAND) {
                        count(between(16, 32))
                        setWeight(2)
                    }
                    addItem(Items.GRAVEL) {
                        count(between(16, 32))
                        setWeight(2)
                    }
                }
                createPool {
                    setRolls(exactly(5))
                    addItem(Items.COOKED_CHICKEN) {
                        count(between(3, 6))
                        setWeight(2)
                    }
                    addItem(Items.COOKED_BEEF) {
                        count(between(2, 4))
                        setWeight(2)
                    }
                    addItem(Items.SWEET_BERRIES) {
                        count(between(8, 12))
                        setWeight(3)
                    }
                    addItem(Items.APPLE) {
                        count(between(2, 4))
                        setWeight(2)
                    }
                }
                createPool {
                    addItem(Items.IRON_HELMET) {
                        durability(between(0.8, 0.99F))
                        enchant(provider, between(8, 10))
                        setWeight(4)
                    }
                    addItem(Items.DIAMOND_HELMET) {
                        durability(between(0.8, 0.99F))
                        enchant(provider, exactly(8))
                        setWeight(1)
                    }
                }
                createPool {
                    addItem(Items.IRON_CHESTPLATE) {
                        durability(between(0.8, 0.99F))
                        enchant(provider, between(8, 10))
                        setWeight(2)
                    }
                    addItem(Items.CHAINMAIL_CHESTPLATE) {
                        durability(between(0.8, 0.99F))
                        enchant(provider, between(10, 12))
                        setWeight(1)
                    }
                }
                createPool {
                    addItem(Items.IRON_LEGGINGS) {
                        durability(between(0.8, 0.99F))
                        enchant(provider, between(8, 10))
                    }
                }
                createPool {
                    addItem(Items.IRON_BOOTS) {
                        durability(between(0.8, 0.99F))
                        enchant(provider, between(8, 10))
                        setWeight(3)
                    }
                    addItem(Items.GOLDEN_BOOTS) {
                        durability(between(0.9, 0.99F))
                        enchant(provider, between(14, 18))
                        setWeight(1)
                    }
                }
                createPool {
                    setRolls(exactly(4))
                    addItem(Items.IRON_INGOT) {
                        count(between(6, 8))
                        setWeight(4)
                    }
                    addItem(Items.GOLD_INGOT) {
                        count(between(8, 10))
                        setWeight(4)
                    }
                    addItem(Items.DIAMOND) {
                        count(between(1, 2))
                        setWeight(1)
                    }
                }
                createPool {
                    addItem(Items.WATER_BUCKET) {

                    }
                }
                createPool {
                    setRolls(exactly(4))
                    addItem(Items.ARROW) {
                        count(between(8, 10))
                        setWeight(5)
                    }
                    addItem(Items.SPECTRAL_ARROW) {
                        count(between(6, 8))
                        setWeight(2)
                    }
                }
            }
            Companion.table = table
            return table
        }
    }
}