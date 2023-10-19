package net.casual.minigame.uhc.advancement

import net.casual.arcade.advancements.AdvancementBuilder
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.util.CasualUtils.id
import net.casual.util.Texts
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.FrameType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions

object UHCAdvancements {
    private val ALL = HashSet<Advancement>()

    val ROOT: Advancement
    val FIRST_BLOOD: Advancement
    val EARLY_EXIT: Advancement
    val MOSTLY_HARMLESS: Advancement
    val HEAVY_HITTER: Advancement
    val WINNER: Advancement
    val COMBAT_LOGGER: Advancement
    val ON_THE_EDGE: Advancement
    val NOT_DUSTLESS: Advancement
    val PARKOUR_MASTER: Advancement
    val WORLD_RECORD_PACE: Advancement
    val EMBARRASSING: Advancement
    val BUSTED: Advancement
    val DEMOLITION_EXPERT: Advancement
    val OK_WE_BELIEVE_YOU_NOW: Advancement
    val FALLING_BLOCK: Advancement
    val DREAM_LUCK: Advancement
    val BROKEN_ANKLES: Advancement
    val SKILL_ISSUE: Advancement
    val SOLOIST: Advancement
    val LDAP: Advancement
    val NOT_NOW: Advancement
    val OFFICIALLY_BORED: Advancement
    val FIND_THE_BUTTON: Advancement
    val DISTRACTED: Advancement
    val UH_OH: Advancement
    val BASICALLY: Advancement
    val TEAM_PLAYER: Advancement
    val LAST_MAN_STANDING: Advancement

    init {
        ROOT = register {
            id = id("root")
            display(Items.GOLDEN_APPLE)
            title = Texts.ADVANCEMENT_ROOT
            description = Texts.ADVANCEMENT_ROOT_DESC
            background = ResourceLocation("textures/gui/advancements/backgrounds/adventure.png")
            impossible()
        }
        FIRST_BLOOD = register {
            parent = ROOT
            id = id("first_blood")
            display(Items.IRON_SWORD)
            title = Texts.ADVANCEMENT_FIRST_BLOOD
            description = Texts.ADVANCEMENT_FIRST_BLOOD_DESC
            toast()
            announce()
            impossible()
        }
        EARLY_EXIT = register {
            parent = ROOT
            id = id("early_exit")
            display(Items.POISONOUS_POTATO)
            title = Texts.ADVANCEMENT_EARLY_EXIT
            description = Texts.ADVANCEMENT_EARLY_EXIT_DESC
            toast()
            announce()
            impossible()
        }
        MOSTLY_HARMLESS = register {
            parent = EARLY_EXIT
            id = id("mostly_harmless")
            display(Items.FEATHER)
            title = Texts.ADVANCEMENT_MOSTLY_HARMLESS
            description = Texts.ADVANCEMENT_MOSTLY_HARMLESS_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }
        HEAVY_HITTER = register {
            parent = FIRST_BLOOD
            id = id("heavy_hitter")
            display(Items.ANVIL)
            title = Texts.ADVANCEMENT_HEAVY_HITTER
            description = Texts.ADVANCEMENT_HEAVY_HITTER_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }
        WINNER = register {
            parent = FIRST_BLOOD
            id = id("winner")
            display(Items.TOTEM_OF_UNDYING)
            title = Texts.ADVANCEMENT_WINNER
            description = Texts.ADVANCEMENT_WINNER_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }
        COMBAT_LOGGER = register {
            parent = ROOT
            id = id("combat_logger")
            display(Items.WOODEN_SWORD)
            title = Texts.ADVANCEMENT_COMBAT_LOGGER
            description = Texts.ADVANCEMENT_COMBAT_LOGGER_DESC
            toast()
            announce()
            impossible()
        }
        NOT_DUSTLESS = register {
            parent = ROOT
            id = id("thats_not_dustless")
            display(Items.REDSTONE)
            title = Texts.ADVANCEMENT_NOT_DUSTLESS
            description = Texts.ADVANCEMENT_NOT_DUSTLESS_DESC
            toast()
            announce()
            impossible()
        }
        PARKOUR_MASTER = register {
            parent = ROOT
            id = id("parkour_master")
            display(Items.NETHERITE_BOOTS)
            title = Texts.ADVANCEMENT_PARKOUR_MASTER
            description = Texts.ADVANCEMENT_PARKOUR_MASTER_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }
        WORLD_RECORD_PACE = register {
            parent = ROOT
            id = id("world_record_pace")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.SWIFTNESS)
            title = Texts.ADVANCEMENT_WORLD_RECORD_PACE
            description = Texts.ADVANCEMENT_WORLD_RECORD_PACE_DESC
            toast()
            announce()
            impossible()
        }
        EMBARRASSING = register {
            parent = ROOT
            id = id("thats_embarrassing")
            display(Items.SWEET_BERRIES)
            title = Texts.ADVANCEMENT_EMBARRASSING
            description = Texts.ADVANCEMENT_EMBARRASSING_DESC
            toast()
            announce()
            impossible()
        }
        BUSTED = register {
            parent = ROOT
            id = id("busted")
            display(Items.BARRIER)
            title = Texts.ADVANCEMENT_BUSTED
            description = Texts.ADVANCEMENT_BUSTED_DESC
            toast()
            announce()
            impossible()
        }
        DEMOLITION_EXPERT = register {
            parent = EARLY_EXIT
            id = id("demolition_expert")
            display(Items.TNT)
            title = Texts.ADVANCEMENT_DEMOLITION_EXPERT
            description = Texts.ADVANCEMENT_DEMOLITION_EXPERT_DESC
            toast()
            announce()
            impossible()
        }
        OK_WE_BELIEVE_YOU_NOW = register {
            parent = COMBAT_LOGGER
            id = id("ok_we_believe_you_now")
            display(Items.WOODEN_HOE)
            title = Texts.ADVANCEMENT_WE_BELIEVE
            description = Texts.ADVANCEMENT_WE_BELIEVE_DESC
            toast()
            announce()
            impossible()
        }
        FALLING_BLOCK = register {
            parent = NOT_DUSTLESS
            id = id("falling_block")
            display(Items.SAND)
            title = Texts.ADVANCEMENT_FALLING_BLOCK
            description = Texts.ADVANCEMENT_FALLING_BLOCK_DESC
            toast()
            announce()
            impossible()
        }
        DREAM_LUCK = register {
            parent = BUSTED
            id = id("dream_luck")
            display(Items.ENCHANTED_GOLDEN_APPLE)
            title = Texts.ADVANCEMENT_DREAM_LUCK
            description = Texts.ADVANCEMENT_DREAM_LUCK_DESC
            toast()
            announce()
            impossible()
        }
        BROKEN_ANKLES = register {
            parent = EMBARRASSING
            id = id("broken_ankles")
            display(Items.LEATHER_BOOTS)
            title = Texts.ADVANCEMENT_BROKEN_ANKLES
            description = Texts.ADVANCEMENT_BROKEN_ANKLES_DESC
            toast()
            announce()
            impossible()
        }
        ON_THE_EDGE = register {
            parent = BROKEN_ANKLES
            id = id("on_the_edge")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.STRONG_HARMING)
            title = Texts.ADVANCEMENT_ON_THE_EDGE
            description = Texts.ADVANCEMENT_ON_THE_EDGE_DESC
            toast()
            announce()
            impossible()
        }
        SKILL_ISSUE = register {
            parent = EARLY_EXIT
            id = id("skill_issue")
            display(Items.BONE)
            title = Texts.ADVANCEMENT_SKILL_ISSUE
            description = Texts.ADVANCEMENT_SKILL_ISSUE_DESC
            toast()
            announce()
            impossible()
        }
        SOLOIST = register {
            parent = ROOT
            id = id("soloist")
            display(Items.PLAYER_HEAD)
            title = Texts.ADVANCEMENT_SOLOIST
            description = Texts.ADVANCEMENT_SOLOIST_DESC
            toast()
            announce()
            impossible()
        }
        NOT_NOW = register {
            parent = ROOT
            id = id("not_now")
            display(Items.NETHERITE_SWORD)
            title = Texts.ADVANCEMENT_NOT_NOW
            description = Texts.ADVANCEMENT_NOT_NOW_DESC
            toast()
            announce()
            impossible()
        }
        LDAP = register {
            parent = NOT_NOW
            id = id("ldap")
            display(Items.EMERALD_BLOCK)
            title = Texts.ADVANCEMENT_KATIE
            description = Texts.ADVANCEMENT_KATIE_DESC
            toast()
            announce()
            impossible()
        }
        OFFICIALLY_BORED = register {
            parent = WORLD_RECORD_PACE
            id = id("officially_bored")
            display(Items.COMMAND_BLOCK)
            title = Texts.ADVANCEMENT_BORED
            description = Texts.ADVANCEMENT_BORED_DESC
            toast()
            announce()
            impossible()
        }
        DISTRACTED = register {
            parent = OFFICIALLY_BORED
            id = id("distracted")
            display(Items.CHAIN_COMMAND_BLOCK)
            title = Texts.ADVANCEMENT_DISTRACTED
            description = Texts.ADVANCEMENT_DISTRACTED_DESC
            toast()
            announce()
            impossible()
        }
        UH_OH = register {
            parent = LDAP
            id = id("uh_oh")
            display(Items.BARRIER)
            title = Texts.ADVANCEMENT_UH_OH
            description = Texts.ADVANCEMENT_UH_OH_DESC
            toast()
            announce()
            impossible()
        }
        BASICALLY = register {
            parent = LDAP
            id = id("basically")
            display(Items.WHITE_WOOL)
            title = Texts.ADVANCEMENT_BASICALLY
            description = Texts.ADVANCEMENT_BASICALLY_DESC
            toast()
            announce()
            impossible()
        }
        TEAM_PLAYER = register {
            parent = SOLOIST
            id = id("team_player")
            display(Items.LIME_BANNER)
            title = Texts.ADVANCEMENT_TEAM_PLAYER
            description = Texts.ADVANCEMENT_TEAM_PLAYER_DESC
            toast()
            announce()
            impossible()
        }
        FIND_THE_BUTTON = register {
            parent = OFFICIALLY_BORED
            id = id("find_the_button")
            display(Items.STONE_BUTTON)
            title = Texts.ADVANCEMENT_FIND_THE_BUTTON
            description = Texts.ADVANCEMENT_FIND_THE_BUTTON_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }
        LAST_MAN_STANDING = register {
            parent = SOLOIST
            id = id("soloist")
            display(Items.ARROW)
            title = Texts.ADVANCEMENT_LAST_MAN
            description = Texts.ADVANCEMENT_LAST_MAN_DESC
            toast()
            announce()
            impossible()
        }
    }

    fun register(builder: AdvancementBuilder.() -> Unit): Advancement {
        val advancement = AdvancementBuilder.create(builder).build()
        ALL.add(advancement)
        return advancement
    }

    fun isRegistered(advancement: Advancement): Boolean {
        return ALL.contains(advancement)
    }

    fun getAllAdvancements(): Collection<Advancement> {
        return ALL
    }
}
