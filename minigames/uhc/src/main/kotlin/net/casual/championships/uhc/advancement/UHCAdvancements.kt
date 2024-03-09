package net.casual.championships.uhc.advancement

import net.casual.arcade.advancements.AdvancementBuilder
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.championships.uhc.UHCComponents
import net.casual.championships.uhc.UHCMod.id
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions

object UHCAdvancements {
    private val ALL = LinkedHashSet<AdvancementHolder>()

    val ROOT: AdvancementHolder
    val FIRST_BLOOD: AdvancementHolder
    val EARLY_EXIT: AdvancementHolder
    val MOSTLY_HARMLESS: AdvancementHolder
    val HEAVY_HITTER: AdvancementHolder
    val WINNER: AdvancementHolder
    val COMBAT_LOGGER: AdvancementHolder
    val ON_THE_EDGE: AdvancementHolder
    val NOT_DUSTLESS: AdvancementHolder
    val PARKOUR_MASTER: AdvancementHolder
    val WORLD_RECORD_PACE: AdvancementHolder
    val EMBARRASSING: AdvancementHolder
    val BUSTED: AdvancementHolder
    val DEMOLITION_EXPERT: AdvancementHolder
    val OK_WE_BELIEVE_YOU_NOW: AdvancementHolder
    val FALLING_BLOCK: AdvancementHolder
    val DREAM_LUCK: AdvancementHolder
    val BROKEN_ANKLES: AdvancementHolder
    val SKILL_ISSUE: AdvancementHolder
    val SOLOIST: AdvancementHolder
    val LDAP: AdvancementHolder
    val NOT_NOW: AdvancementHolder
    val OFFICIALLY_BORED: AdvancementHolder
    val FIND_THE_BUTTON: AdvancementHolder
    val DISTRACTED: AdvancementHolder
    val UH_OH: AdvancementHolder
    val BASICALLY: AdvancementHolder
    val TEAM_PLAYER: AdvancementHolder
    val LAST_MAN_STANDING: AdvancementHolder
    val BEAR_CARED: AdvancementHolder

    init {
        ROOT = register {
            id = id("root")
            display(Items.GOLDEN_APPLE)
            title = UHCComponents.ADVANCEMENT_ROOT
            description = UHCComponents.ADVANCEMENT_ROOT_DESC
            background = ResourceLocation("textures/gui/advancements/backgrounds/adventure.png")
        }
        FIRST_BLOOD = register {
            parent(ROOT)
            id = id("first_blood")
            display(Items.IRON_SWORD)
            title = UHCComponents.ADVANCEMENT_FIRST_BLOOD
            description = UHCComponents.ADVANCEMENT_FIRST_BLOOD_DESC
            toast()
            announce()
        }
        EARLY_EXIT = register {
            parent(ROOT)
            id = id("early_exit")
            display(Items.POISONOUS_POTATO)
            title = UHCComponents.ADVANCEMENT_EARLY_EXIT
            description = UHCComponents.ADVANCEMENT_EARLY_EXIT_DESC
            toast()
            announce()
        }
        MOSTLY_HARMLESS = register {
            parent(EARLY_EXIT)
            id = id("mostly_harmless")
            display(Items.FEATHER)
            title = UHCComponents.ADVANCEMENT_MOSTLY_HARMLESS
            description = UHCComponents.ADVANCEMENT_MOSTLY_HARMLESS_DESC
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        HEAVY_HITTER = register {
            parent(FIRST_BLOOD)
            id = id("heavy_hitter")
            display(Items.ANVIL)
            title = UHCComponents.ADVANCEMENT_HEAVY_HITTER
            description = UHCComponents.ADVANCEMENT_HEAVY_HITTER_DESC
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        WINNER = register {
            parent(FIRST_BLOOD)
            id = id("winner")
            display(Items.TOTEM_OF_UNDYING)
            title = UHCComponents.ADVANCEMENT_WINNER
            description = UHCComponents.ADVANCEMENT_WINNER_DESC
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        COMBAT_LOGGER = register {
            parent(ROOT)
            id = id("combat_logger")
            display(Items.WOODEN_SWORD)
            title = UHCComponents.ADVANCEMENT_COMBAT_LOGGER
            description = UHCComponents.ADVANCEMENT_COMBAT_LOGGER_DESC
            toast()
            announce()
        }
        NOT_DUSTLESS = register {
            parent(ROOT)
            id = id("thats_not_dustless")
            display(Items.REDSTONE)
            title = UHCComponents.ADVANCEMENT_NOT_DUSTLESS
            description = UHCComponents.ADVANCEMENT_NOT_DUSTLESS_DESC
            toast()
            announce()
        }
        PARKOUR_MASTER = register {
            parent(ROOT)
            id = id("parkour_master")
            display(Items.NETHERITE_BOOTS)
            title = UHCComponents.ADVANCEMENT_PARKOUR_MASTER
            description = UHCComponents.ADVANCEMENT_PARKOUR_MASTER_DESC
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        WORLD_RECORD_PACE = register {
            parent(ROOT)
            id = id("world_record_pace")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.SWIFTNESS)
            title = UHCComponents.ADVANCEMENT_WORLD_RECORD_PACE
            description = UHCComponents.ADVANCEMENT_WORLD_RECORD_PACE_DESC
            toast()
            announce()
        }
        EMBARRASSING = register {
            parent(ROOT)
            id = id("thats_embarrassing")
            display(Items.SWEET_BERRIES)
            title = UHCComponents.ADVANCEMENT_EMBARRASSING
            description = UHCComponents.ADVANCEMENT_EMBARRASSING_DESC
            toast()
            announce()
        }
        BUSTED = register {
            parent(ROOT)
            id = id("busted")
            display(Items.BARRIER)
            title = UHCComponents.ADVANCEMENT_BUSTED
            description = UHCComponents.ADVANCEMENT_BUSTED_DESC
            toast()
            announce()
        }
        DEMOLITION_EXPERT = register {
            parent(EARLY_EXIT)
            id = id("demolition_expert")
            display(Items.TNT)
            title = UHCComponents.ADVANCEMENT_DEMOLITION_EXPERT
            description = UHCComponents.ADVANCEMENT_DEMOLITION_EXPERT_DESC
            toast()
            announce()
        }
        OK_WE_BELIEVE_YOU_NOW = register {
            parent(COMBAT_LOGGER)
            id = id("ok_we_believe_you_now")
            display(Items.WOODEN_HOE)
            title = UHCComponents.ADVANCEMENT_WE_BELIEVE
            description = UHCComponents.ADVANCEMENT_WE_BELIEVE_DESC
            toast()
            announce()
        }
        FALLING_BLOCK = register {
            parent(NOT_DUSTLESS)
            id = id("falling_block")
            display(Items.SAND)
            title = UHCComponents.ADVANCEMENT_FALLING_BLOCK
            description = UHCComponents.ADVANCEMENT_FALLING_BLOCK_DESC
            toast()
            announce()
        }
        DREAM_LUCK = register {
            parent(BUSTED)
            id = id("dream_luck")
            display(Items.ENCHANTED_GOLDEN_APPLE)
            title = UHCComponents.ADVANCEMENT_DREAM_LUCK
            description = UHCComponents.ADVANCEMENT_DREAM_LUCK_DESC
            toast()
            announce()
        }
        BROKEN_ANKLES = register {
            parent(EMBARRASSING)
            id = id("broken_ankles")
            display(Items.LEATHER_BOOTS)
            title = UHCComponents.ADVANCEMENT_BROKEN_ANKLES
            description = UHCComponents.ADVANCEMENT_BROKEN_ANKLES_DESC
            toast()
            announce()
        }
        ON_THE_EDGE = register {
            parent(BROKEN_ANKLES)
            id = id("on_the_edge")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.STRONG_HARMING)
            title = UHCComponents.ADVANCEMENT_ON_THE_EDGE
            description = UHCComponents.ADVANCEMENT_ON_THE_EDGE_DESC
            toast()
            announce()
        }
        SKILL_ISSUE = register {
            parent(EARLY_EXIT)
            id = id("skill_issue")
            display(Items.BONE)
            title = UHCComponents.ADVANCEMENT_SKILL_ISSUE
            description = UHCComponents.ADVANCEMENT_SKILL_ISSUE_DESC
            toast()
            announce()
        }
        SOLOIST = register {
            parent(ROOT)
            id = id("soloist")
            display(Items.PLAYER_HEAD)
            title = UHCComponents.ADVANCEMENT_SOLOIST
            description = UHCComponents.ADVANCEMENT_SOLOIST_DESC
            toast()
            announce()
        }
        NOT_NOW = register {
            parent(ROOT)
            id = id("not_now")
            display(Items.NETHERITE_SWORD)
            title = UHCComponents.ADVANCEMENT_NOT_NOW
            description = UHCComponents.ADVANCEMENT_NOT_NOW_DESC
            toast()
            announce()
        }
        LDAP = register {
            parent(NOT_NOW)
            id = id("ldap")
            display(Items.EMERALD_BLOCK)
            title = UHCComponents.ADVANCEMENT_KATIE
            description = UHCComponents.ADVANCEMENT_KATIE_DESC
            toast()
            announce()
        }
        OFFICIALLY_BORED = register {
            parent(WORLD_RECORD_PACE)
            id = id("officially_bored")
            display(Items.COMMAND_BLOCK)
            title = UHCComponents.ADVANCEMENT_BORED
            description = UHCComponents.ADVANCEMENT_BORED_DESC
            toast()
            announce()
        }
        DISTRACTED = register {
            parent(OFFICIALLY_BORED)
            id = id("distracted")
            display(Items.CHAIN_COMMAND_BLOCK)
            title = UHCComponents.ADVANCEMENT_DISTRACTED
            description = UHCComponents.ADVANCEMENT_DISTRACTED_DESC
            toast()
            announce()
        }
        UH_OH = register {
            parent(LDAP)
            id = id("uh_oh")
            display(Items.BARRIER)
            title = UHCComponents.ADVANCEMENT_UH_OH
            description = UHCComponents.ADVANCEMENT_UH_OH_DESC
            toast()
            announce()
        }
        BASICALLY = register {
            parent(LDAP)
            id = id("basically")
            display(Items.WHITE_WOOL)
            title = UHCComponents.ADVANCEMENT_BASICALLY
            description = UHCComponents.ADVANCEMENT_BASICALLY_DESC
            toast()
            announce()
        }
        TEAM_PLAYER = register {
            parent(SOLOIST)
            id = id("team_player")
            display(Items.LIME_BANNER)
            title = UHCComponents.ADVANCEMENT_TEAM_PLAYER
            description = UHCComponents.ADVANCEMENT_TEAM_PLAYER_DESC
            toast()
            announce()
        }
        FIND_THE_BUTTON = register {
            parent(OFFICIALLY_BORED)
            id = id("find_the_button")
            display(Items.STONE_BUTTON)
            title = UHCComponents.ADVANCEMENT_FIND_THE_BUTTON
            description = UHCComponents.ADVANCEMENT_FIND_THE_BUTTON_DESC
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        LAST_MAN_STANDING = register {
            parent(SOLOIST)
            id = id("soloist")
            display(Items.ARROW)
            title = UHCComponents.ADVANCEMENT_LAST_MAN
            description = UHCComponents.ADVANCEMENT_LAST_MAN_DESC
            toast()
            announce()
        }
        BEAR_CARED = register {
            parent(EMBARRASSING)
            id = id("bear_cared")
            display(Items.SCULK_SENSOR)
            title = UHCComponents.ADVANCEMENT_BEAR_CARED
            description = UHCComponents.ADVANCEMENT_BEAR_CARED_DESC
            toast()
            announce()
        }
    }

    fun register(builder: AdvancementBuilder.() -> Unit): AdvancementHolder {
        val advancement = AdvancementBuilder.create(builder).build()
        ALL.add(advancement)
        return advancement
    }

    fun isRegistered(advancement: AdvancementHolder): Boolean {
        return ALL.contains(advancement)
    }

    fun getAllAdvancements(): Collection<AdvancementHolder> {
        return ALL
    }
}
