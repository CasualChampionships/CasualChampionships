package net.casual.championships.uhc.minigame

import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.bool
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.enumeration
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.float32
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.float64
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.time
import net.casual.arcade.minigame.utils.defaultOptions
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.uhc.border.UHCBoundaryPhase
import net.casual.championships.uhc.recipe.FlowerPowerRecipe
import net.casual.championships.uhc.recipe.HeavyCoreRecipe
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions

class UHCSettings(private val uhc: UHCMinigame): CasualSettings(uhc) {
    var glowing by this.register(bool {
        name = "glowing"
        display = Items.GLOWSTONE_DUST.named("Glowing")
        value = false
        defaultOptions()
        listener { _, _, _ ->
            for (player in uhc.players.playing) {
                player.setGlowingTag(!player.hasGlowingTag())
                player.setGlowingTag(!player.hasGlowingTag())
            }
        }
    })

    var borderSizeMultiplier by this.register(float64 {
        name = "border_size_multiplier"
        display = CasualGuiItems.BORDER_RADIUS.named("Border Size Multiplier")
        value = 1.0
        option("one_third", Component.literal("0.33x Size"), 1.0 / 3.0)
        option("half", Component.literal("0.5x Size"), 0.5)
        option("two_thirds", Component.literal("0.66x Size"), 2.0 / 3.0)
        option("normal", Component.literal("1x Size"), 1.0)
        option("three_halves", Component.literal("1.5x Size"), 1.5)
        option("double", Component.literal("2x Size"), 2.0)
    })

    var borderTime by this.register(time {
        name = "border_completion_time"
        display = CasualGuiItems.BORDER_DISTANCE.named("Border Completion Time")
        value = UHCBoundaryPhase.TOTAL_TIME
        option("ten_minutes", Component.literal("10 Minutes"), 10.Minutes)
        option("thirty_minutes", Component.literal("30 Minutes"), 30.Minutes)
        option("two_hours", Component.literal("2 Hours"), 120.Minutes)
        option("total_time", Component.literal("Total Time"), UHCBoundaryPhase.TOTAL_TIME)
        option("two_and_half_hours", Component.literal("2.5 Hours"), 150.Minutes)
        option("three_hours", Component.literal("3 Hours"), 180.Minutes)
    })

    var startingDimension by this.register(enumeration<VanillaDimension> {
        name = "starting_dimension"
        display = Items.GRASS_BLOCK.named("Starting Dimension")
        value = VanillaDimension.Overworld
        defaultOptions()
    })

    var gracePeriod by this.register(time {
        name = "grace_period"
        display = Items.SHIELD.named("Grace Period")
        value = 30.Minutes
        option("one_minute", Component.literal("1 Minute"), 1.Minutes)
        option("two_minutes", Component.literal("2 Minutes"), 2.Minutes)
        option("five_minutes", Component.literal("5 Minutes"), 5.Minutes)
        option("ten_minutes", Component.literal("10 Minutes"), 10.Minutes)
        option("twenty_minutes", Component.literal("20 Minutes"), 20.Minutes)
        option("thirty_minutes", Component.literal("30 Minutes"), 30.Minutes)
    })

    var borderStartDelay by this.register(time {
        name = "border_start_delay"
        display = Items.STRUCTURE_VOID.named("Border Start Delay")
        value = 10.Minutes
        option("five_minutes", Component.literal("5 Minutes"), 5.Minutes)
        option("ten_minutes", Component.literal("10 Minutes"), 10.Minutes)
        option("twenty_minutes", Component.literal("20 Minutes"), 20.Minutes)
    })

    var portalEscapeTime by this.register(time {
        name = "portal_escape_time"
        display = Items.OBSIDIAN.named("Portal Escape Time")
        value = 30.Seconds
        option("none", Component.literal("None"), 0.Seconds)
        option("ten_seconds", Component.literal("10 Seconds"), 10.Seconds)
        option("twenty_seconds", Component.literal("20 Second"), 20.Seconds)
        option("thirty_seconds", Component.literal("30 Seconds"), 30.Seconds)
        option("sixty_seconds", Component.literal("60 Seconds"), 60.Seconds)
    })

    var bowCooldown by this.register(time {
        name = "bow_cooldown"
        display = Items.BOW.named("Bow Cooldown")
        value = 1.Seconds
        option("none", Component.literal("None"), 0.Seconds)
        option("half_second", Component.literal("0.5 Seconds"), 10.Ticks)
        option("one_second", Component.literal("1 Second"), 1.Seconds)
        option("two_seconds", Component.literal("2 Seconds"), 2.Seconds)
        option("three_seconds", Component.literal("3 Seconds"), 3.Seconds)
        option("five_seconds", Component.literal("5 Seconds"), 5.Seconds)
    })

    var health by this.register(float64 {
        name = "health"
        display = Items.POTION.named("Health").potion(Potions.HEALING)
            .hideTooltip(DataComponents.ATTRIBUTE_MODIFIERS)
        value = 1.0
        option("triple", Component.literal("Triple"), 2.0)
        option("double", Component.literal("Double"), 1.0)
        option("normal", Component.literal("Normal"), 0.0)
    })

    var endGameGlow by this.register(bool {
        name = "end_game_glow"
        display = Items.SPECTRAL_ARROW.named("End Game Glow")
        value = true
        defaultOptions()
    })

    var friendlyPlayerGlow by this.register(bool {
        name = "friendly_player_glow"
        display = Items.GOLDEN_CARROT.named("Friendly Player Glow")
        value = true
        defaultOptions()
    })

    var playerDropsGapple by this.register(bool {
        name = "player_drops_gapple"
        display = Items.GOLDEN_APPLE.named("Player Drops Gapple")
        value = false
        defaultOptions()
    })

    var playerDropsHead by this.register(bool {
        name = "player_drops_head"
        display = Items.PLAYER_HEAD.named("Player Drops Head")
        value = true
        defaultOptions()
    })

    var opPotions by this.register(bool {
        name = "op_potions"
        display = Items.SPLASH_POTION.named("OP Potions").potion(Potions.STRONG_HARMING)
            .hideTooltip(DataComponents.ATTRIBUTE_MODIFIERS)
        value = false
        defaultOptions()
    })

    var generatePortals by this.register(bool {
        name = "generate_portals"
        display = Items.CRYING_OBSIDIAN.named("Generate Portals")
        value = true
        defaultOptions()
    })

    var soloBuff by this.register(bool {
        name = "solo_buff"
        display = Items.LINGERING_POTION.named("Solo Buff").potion(Potions.REGENERATION)
            .hideTooltip(DataComponents.ATTRIBUTE_MODIFIERS)
        value = true
        defaultOptions()
    })

    var replay by this.register(bool {
        name = "replay"
        display = Items.END_PORTAL_FRAME.named("Server Replay")
        value = true
        defaultOptions()
    })

    var instantSmeltOres by this.register(bool {
        name = "instant_smelt_ores"
        display = Items.FURNACE.named("Instant Smelt Ores")
        value = false
        defaultOptions()
    })

    var flowerPower by this.register(bool {
        name = "flower_power"
        display = Items.POPPY.named("Flower Power")
        value = false
        defaultOptions()
        listener { _, _, value ->
            val recipe = FlowerPowerRecipe.getOrCreate(uhc.server.registryAccess())
            if (value) {
                uhc.recipes.add(recipe)
                uhc.players.allProfiles.forEach { profile -> uhc.recipes.grant(profile.id, recipe.id) }
            } else {
                uhc.recipes.remove(recipe.id)
            }
        }
    })

    var bloodDiamonds by this.register(bool {
        name = "blood_diamonds"
        display = Items.DIAMOND.named("Blood Diamonds")
        value = false
        defaultOptions()
    })

    var heavyHeads by this.register(bool {
        name = "heavy_heads"
        display = Items.HEAVY_CORE.named("Heavy Heads")
        value = false
        defaultOptions()
        listener { _, _, value ->
            val recipe = HeavyCoreRecipe.INSTANCE
            if (value) {
                uhc.recipes.add(recipe)
                uhc.players.allProfiles.forEach { profile -> uhc.recipes.grant(profile.id, recipe.id) }
            } else {
                uhc.recipes.remove(recipe.id)
            }
        }
    })

    var headStart by this.register(bool {
        name = "head_start"
        display = Items.BUNDLE.named("Head Start")
        value = false
        defaultOptions()
    })

    var pedalToTheMetal by this.register(bool {
        name = "pedal_to_the_metal"
        display = Items.MINECART.named("Pedal to The Metal")
        value = false
        defaultOptions()
    })

    var tightlyBonded by this.register(bool {
        name = "tightly_bonded"
        display = Items.STRING.named("Tightly Bonded")
        value = false
        defaultOptions()
    })

    var nerfedPlayerDamage by this.register(float32 {
        name = "nerfed_player_damage"
        display = Items.WOODEN_SWORD.named("Nerfed player damage")
        value = 0.3F
        option("minus_10_percent", Component.literal("Minus 10%"), 0.1F)
        option("minus_30_percent", Component.literal("Minus 30%"), 0.3F)
        option("minus_50_percent", Component.literal("Minus 50%"), 0.5F)
    })

    var tmcStarterPack by this.register(bool {
        name = "tmc_starter_pack"
        display = Items.DYED_SHULKER_BOX.red.named("TMC Starter Pack")
            .lore(Component.literal("Gives players a shulker box with redstone goodies"))
        value = false
        defaultOptions()
    })

    var handsFree by this.register(bool {
        name = "hands_free"
        display = Items.GOLDEN_PICKAXE.named("Hands Free")
            .lore(Component.literal("Enables careful break"))
        value = false
        defaultOptions()
    })

    var lawsOfAviation by this.register(bool {
        name = "laws_of_aviation"
        display = Items.ELYTRA.named("Laws of Aviation")
            .lore(Component.literal("Gives players a broken elytra with mending"))
        value = false
        defaultOptions()
    })

    var mobMash by this.register(bool {
        name = "mob_mash"
        display = Items.CREEPER_HEAD.named("Mob Mash")
            .lore(Component.literal("Randomizes mob sizes"))
        value = false
        defaultOptions()
    })

    var helpingHand by this.register(bool {
        name = "helping_hand"
        display = Items.LEAD.named("Helping Hand")
            .lore(Component.literal("Allows players to pick up other players"))
        value = false
        defaultOptions()
    })

    var sharingIsCaring by this.register(bool {
        name = "sharing_is_caring"
        display = Items.DRAGON_BREATH.named("Sharing is Caring")
            .lore(Component.literal("Health is shared between all players on a team"))
        value = false
        defaultOptions()
    })
}