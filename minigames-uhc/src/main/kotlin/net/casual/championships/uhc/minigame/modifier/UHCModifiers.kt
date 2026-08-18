package net.casual.championships.uhc.minigame.modifier

import net.casual.arcade.events.server.block.BlockDropLootEvent
import net.casual.arcade.events.server.block.BrewingStandBrewEvent
import net.casual.arcade.events.server.entity.EntityBeforeLootEvent
import net.casual.arcade.events.server.entity.EntityStartTrackingEvent
import net.casual.arcade.events.server.level.LevelLootEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.events.threading.ThreadingTarget
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.entity.setVelocityAndMark
import net.casual.arcade.utils.player.dropItemStackIntoInventory
import net.casual.arcade.utils.registries.isOf
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.championships.common.event.ChunkGenerationMobSpawnEvent
import net.casual.championships.common.event.CreateTradeOfferEvent
import net.casual.championships.common.items.minigame.PlayerHeadItem
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.GAME_OVER_ID
import net.casual.championships.uhc.minigame.UHCMinigame
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potion
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3
import kotlin.jvm.optionals.getOrNull
import kotlin.math.roundToInt

class UHCModifiers(
    private val uhc: UHCMinigame
): MinigameEventListener {
    fun onPlayerEliminated(player: ServerPlayer, killer: Entity?) {
        if (killer is ServerPlayer) {
            this.onKilled(killer, player)
        }

        if (this.uhc.settings.playerDropsGapple) {
            player.drop(Items.GOLDEN_APPLE.defaultInstance, true, false)
        }

        if (this.uhc.settings.playerDropsHead) {
            val head = PlayerHeadItem.create(player)
            if (killer is ServerPlayer) {
                if (!killer.inventory.add(head)) {
                    player.drop(head, true, false)
                }
            } else {
                player.drop(head, true, false)
            }
        }
    }

    private fun onKilled(player: ServerPlayer, killed: ServerPlayer) {
        val opposing = killed.team ?: return
        if (this.uhc.settings.soloBuff) {
            // Opposing team has many players
            if (opposing.getOnlinePlayers().count(this.uhc.players::isPlaying) > 0) {
                val team = player.team ?: return
                // Killer is solo
                if (team.getOnlinePlayers().count(this.uhc.players::isPlaying) == 1) {
                    player.addEffect(MobEffectInstance(MobEffects.REGENERATION, 60, 2))
                }
            }
        }
    }

    @Listener
    private fun onPlayerAttack(event: PlayerAttackEvent) {
        if (this.uhc.tags.has(event.player, NERFED) && event.target is ServerPlayer) {
            event.damage *= 1 - this.uhc.settings.nerfedPlayerDamage
        }
    }

    @Listener
    private fun onPlayerInteract(event: PlayerEntityInteractionEvent) {
        val (player, target) = event
        if (this.uhc.settings.helpingHand && player.mainHandItem.isEmpty && target is ServerPlayer) {
            if (player.team == target.team) {
                target.startRiding(player)
            }
        }
    }

    @Listener
    private fun onPlayerTryAttack(event: PlayerTryAttackEvent) {
        val (player, target) = event
        if (this.uhc.settings.helpingHand && player.mainHandItem.isEmpty && target.vehicle == player) {
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
    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        val (player, stack) = event
        if (stack.isOf(Items.BOW)) {
            player.cooldowns.addCooldown(stack, this.uhc.settings.bowCooldown.ticks)
        }
    }

    @Listener
    private fun onBlockMined(event: PlayerBlockMinedEvent) {
        val (player, _, state, _) = event
        if (this.uhc.settings.bloodDiamonds) {
            if (state.isOf(ConventionalBlockTags.DIAMOND_ORES)) {
                player.hurtServer(player.level(), player.damageSources().magic(), 1.0F)
            }
        }
    }

    @Listener
    private fun onBlockDrop(event: BlockDropLootEvent) {
        if (!this.uhc.settings.instantSmeltOres) {
            return
        }

        val entity = event.params.getOptionalParameter(LootContextParams.THIS_ENTITY)
        if (entity is ServerPlayer) {
            if (event.state.isOf(ConventionalBlockTags.ORES)) {
                val tool = event.params.getOptionalParameter(LootContextParams.TOOL) ?: return
                val enchantments = tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                val silkTouch = enchantments.keySet().any { it.isOf(Enchantments.SILK_TOUCH) }
                if (silkTouch) {
                    return
                }
                event.drops = event.drops.map { item ->
                    val input = SingleRecipeInput(item)
                    val recipe = this.uhc.server.recipeManager.getRecipeFor(
                        RecipeType.SMELTING, input, event.level
                    ).getOrNull() ?: return@map item
                    val output = recipe.value.assemble(input)
                    output.copyWithCount(item.count)
                }
            }
        }
    }

    @Listener
    private fun onPlayerBlockDropLoot(event: PlayerBlockDropLootEvent) {
        val (player) = event
        if (this.uhc.settings.handsFree && player.isShiftKeyDown) {
            event.drops.forEach { player.dropItemStackIntoInventory(it) { } }
        }
    }

    @Listener
    private fun onEntityTrack(event: EntityStartTrackingEvent) {
        val (entity) = event
        if (this.uhc.settings.mobMash && entity is LivingEntity && entity !is ServerPlayer) {
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
    private fun onBrewingStandBrew(event: BrewingStandBrewEvent) {
        if (!this.uhc.settings.opPotions) {
            val ingredient = event.entity.getItem(3)
            if (ingredient.isOf(Items.GLOWSTONE_DUST) || ingredient.isOf(Items.GLISTERING_MELON_SLICE)) {
                event.cancel()
            }
        }
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

    @Listener
    private fun onCreateTradeOffer(event: CreateTradeOfferEvent) {
        val offer = event.offer
        val potion = offer.get(DataComponents.POTION_CONTENTS)?.potion()?.getOrNull()
        if (potion != null) {
            offer.set(DataComponents.POTION_CONTENTS, PotionContents(this.replacePotion(potion)))
        }
    }

    @Listener(during = During(before = GAME_OVER_ID))
    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event
        if (this.uhc.players.isSpectating(player)) {
            return
        }

        val teammates = player.team?.getOnlinePlayers()?.filter { it.isAlive } ?: return
        if (this.uhc.settings.pedalToTheMetal) {
            val speedy = teammates.all { teammate -> teammate == player || !teammate.closerThan(player, 50.0) }
            if (speedy) {
                player.addEffect(MobEffectInstance(
                    MobEffects.SPEED, 5.Seconds.ticks + 5, 0, false, false, false
                ))
            }
        }
        if (this.uhc.settings.tightlyBonded) {
            val bonded = teammates.all { teammate -> teammate.closerThan(player, 25.0) }
            if (bonded) {
                player.addEffect(MobEffectInstance(
                    MobEffects.RESISTANCE, 5.Seconds.ticks + 5, 0, false, false, false
                ))
            }
        }
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

    companion object {
        private const val MOB_SPAWN_PROBABILITY = 1.0F / 2.0F
        private val MOB_LOOT_MULTIPLIER = (1.0F / MOB_SPAWN_PROBABILITY).roundToInt()

        private val MOB_MASH = casual("mob_mash")

        val NERFED = casual("nerfed")
    }
}
