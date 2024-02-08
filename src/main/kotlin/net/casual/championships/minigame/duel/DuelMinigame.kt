package net.casual.championships.minigame.duel

import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.player.PlayerDeathEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.utils.LevelUtils
import net.casual.arcade.utils.LootTableUtils
import net.casual.arcade.utils.LootTableUtils.addItem
import net.casual.arcade.utils.LootTableUtils.between
import net.casual.arcade.utils.LootTableUtils.count
import net.casual.arcade.utils.LootTableUtils.createPool
import net.casual.arcade.utils.LootTableUtils.durability
import net.casual.arcade.utils.LootTableUtils.enchant
import net.casual.arcade.utils.LootTableUtils.exactly
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.casual.championships.items.CasualItems
import net.casual.championships.minigame.uhc.resources.UHCResources
import net.casual.championships.util.CasualPlayerUtils.boostHealth
import net.casual.championships.util.CasualUtils
import net.casual.championships.util.HeadUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import kotlin.random.Random

class DuelMinigame(
    server: MinecraftServer,
    val duelSettings: DuelSettings,
    val complete: () -> Unit
): Minigame<DuelMinigame>(server) {
    override val id: ResourceLocation = CasualUtils.id("duel_minigame")

    val level = this.createRandomOverworld()

    override val settings = object: MinigameSettings(this@DuelMinigame) {
        override fun menu(): MenuProvider {
            return duelSettings.menu()
        }
    }

    init {
        this.settings.copyFrom(this.duelSettings)

        this.addResources(UHCResources)
    }

    override fun getPhases(): Collection<MinigamePhase<DuelMinigame>> {
        return DuelPhase.values().toList()
    }

    fun setPlayingPlaying(player: ServerPlayer) {
        player.setGameMode(GameType.SURVIVAL)
        player.boostHealth(this.duelSettings.health)
        player.resetHealth()

        player.setGlowingTag(this.duelSettings.glowing)

        val stacks = duelLoot.getRandomItems(
            LootParams.Builder(player.serverLevel()).create(LootContextParamSet.builder().build()),
            Random.nextLong()
        )

        player.clearPlayerInventory()
        for (stack in stacks) {
            val item = stack.item
            if (item is ArmorItem) {
                player.setItemSlot(item.type.slot, stack)
                continue
            }
            player.inventory.add(stack)
        }
    }

    private fun createRandomOverworld(): ServerLevel {
        val handle = Fantasy.get(this.server).openTemporaryWorld(
            RuntimeWorldConfig()
                .setSeed(Random.nextLong())
                .setShouldTickTime(true)
                .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
                .setGenerator(LevelUtils.overworld().chunkSource.generator)
        )
        this.addLevel(handle)
        return handle.asWorld()
    }

    @Listener
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val killer = player.killCredit

        this.makeSpectator(player)

        if (this.duelSettings.playerDropsHead) {
            val head = HeadUtils.createConsumablePlayerHead(player)
            if (killer is ServerPlayer) {
                if (!killer.inventory.add(head)) {
                    player.drop(head, true, false)
                }
            } else {
                player.drop(head, true, false)
            }
        }
    }

    @Listener
    private fun onMinigameClose(event: MinigameCloseEvent) {
        for (player in this.getAllPlayers()) {
            player.setGlowingTag(false)
        }
    }

    companion object {
        private val duelLoot = this.createToolTable()

        private fun createToolTable(): LootTable {
            return LootTableUtils.create {
                createPool {
                    createPool {
                        addItem(Items.IRON_SWORD) {
                            enchant(exactly(10))
                            durability(between(0.8, 0.99F))
                            setWeight(4)
                        }
                        addItem(Items.DIAMOND_SWORD) {
                            enchant(exactly(10))
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
                        enchant(exactly(10))
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
                        enchant(between(8, 10))
                        setWeight(4)
                    }
                    addItem(Items.DIAMOND_HELMET) {
                        durability(between(0.8, 0.99F))
                        enchant(exactly(8))
                        setWeight(1)
                    }
                }
                createPool {
                    addItem(Items.IRON_CHESTPLATE) {
                        durability(between(0.8, 0.99F))
                        enchant(between(8, 10))
                        setWeight(2)
                    }
                    addItem(Items.CHAINMAIL_CHESTPLATE) {
                        durability(between(0.8, 0.99F))
                        enchant(between(10, 12))
                        setWeight(1)
                    }
                }
                createPool {
                    addItem(Items.IRON_LEGGINGS) {
                        durability(between(0.8, 0.99F))
                        enchant(between(8, 10))
                    }
                }
                createPool {
                    addItem(Items.IRON_BOOTS) {
                        durability(between(0.8, 0.99F))
                        enchant(between(8, 10))
                        setWeight(3)
                    }
                    addItem(Items.GOLDEN_BOOTS) {
                        durability(between(0.9, 0.99F))
                        enchant(between(14, 18))
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
        }
    }
}