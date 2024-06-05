package net.casual.championships.uhc.advancement

import net.casual.arcade.advancements.AdvancementBuilder
import net.casual.arcade.utils.AdvancementUtils.setTitleAndDesc
import net.casual.arcade.utils.ItemUtils.potion
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
            setTitleAndDesc("uhc.advancements.root")
            background = ResourceLocation("textures/gui/advancements/backgrounds/adventure.png")
        }
        FIRST_BLOOD = register {
            parent(ROOT)
            id = id("first_blood")
            display(Items.IRON_SWORD)
            setTitleAndDesc("uhc.advancements.firstBlood")
            toast()
            announce()
        }
        EARLY_EXIT = register {
            parent(ROOT)
            id = id("early_exit")
            display(Items.POISONOUS_POTATO)
            setTitleAndDesc("uhc.advancements.earlyExit")
            toast()
            announce()
        }
        MOSTLY_HARMLESS = register {
            parent(EARLY_EXIT)
            id = id("mostly_harmless")
            display(Items.FEATHER)
            setTitleAndDesc("uhc.advancements.mostlyHarmless")
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        HEAVY_HITTER = register {
            parent(FIRST_BLOOD)
            id = id("heavy_hitter")
            display(Items.ANVIL)
            setTitleAndDesc("uhc.advancements.heavyHitter")
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        WINNER = register {
            parent(FIRST_BLOOD)
            id = id("winner")
            display(Items.TOTEM_OF_UNDYING)
            setTitleAndDesc("uhc.advancements.winner")
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        COMBAT_LOGGER = register {
            parent(ROOT)
            id = id("combat_logger")
            display(Items.WOODEN_SWORD)
            setTitleAndDesc("uhc.advancements.combatLogger")
            toast()
            announce()
        }
        NOT_DUSTLESS = register {
            parent(ROOT)
            id = id("thats_not_dustless")
            display(Items.REDSTONE)
            setTitleAndDesc("uhc.advancements.notDustless")
            toast()
            announce()
        }
        PARKOUR_MASTER = register {
            parent(ROOT)
            id = id("parkour_master")
            display(Items.NETHERITE_BOOTS)
            setTitleAndDesc("uhc.advancements.parkourMaster")
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        WORLD_RECORD_PACE = register {
            parent(ROOT)
            id = id("world_record_pace")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.SWIFTNESS)
            setTitleAndDesc("uhc.advancements.worldRecordPace")
            toast()
            announce()
        }
        EMBARRASSING = register {
            parent(ROOT)
            id = id("thats_embarrassing")
            display(Items.SWEET_BERRIES)
            setTitleAndDesc("uhc.advancements.thatsEmbarrassing")
            toast()
            announce()
        }
        BUSTED = register {
            parent(ROOT)
            id = id("busted")
            display(Items.BARRIER)
            setTitleAndDesc("uhc.advancements.busted")
            toast()
            announce()
        }
        DEMOLITION_EXPERT = register {
            parent(EARLY_EXIT)
            id = id("demolition_expert")
            display(Items.TNT)
            setTitleAndDesc("uhc.advancements.demolitionExpert")
            toast()
            announce()
        }
        OK_WE_BELIEVE_YOU_NOW = register {
            parent(COMBAT_LOGGER)
            id = id("ok_we_believe_you_now")
            display(Items.WOODEN_HOE)
            setTitleAndDesc("uhc.advancements.okWeBelieveYouNow")
            toast()
            announce()
        }
        FALLING_BLOCK = register {
            parent(NOT_DUSTLESS)
            id = id("falling_block")
            display(Items.SAND)
            setTitleAndDesc("uhc.advancements.fallingBlock")
            toast()
            announce()
        }
        DREAM_LUCK = register {
            parent(BUSTED)
            id = id("dream_luck")
            display(Items.ENCHANTED_GOLDEN_APPLE)
            setTitleAndDesc("uhc.advancements.dreamLuck")
            toast()
            announce()
        }
        BROKEN_ANKLES = register {
            parent(EMBARRASSING)
            id = id("broken_ankles")
            display(Items.LEATHER_BOOTS)
            setTitleAndDesc("uhc.advancements.brokenAnkles")
            toast()
            announce()
        }
        ON_THE_EDGE = register {
            parent(BROKEN_ANKLES)
            id = id("on_the_edge")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.STRONG_HARMING)
            setTitleAndDesc("uhc.advancements.onTheEdge")
            toast()
            announce()
        }
        SKILL_ISSUE = register {
            parent(EARLY_EXIT)
            id = id("skill_issue")
            display(Items.BONE)
            setTitleAndDesc("uhc.advancements.skillIssue")
            toast()
            announce()
        }
        SOLOIST = register {
            parent(ROOT)
            id = id("soloist")
            display(Items.PLAYER_HEAD)
            setTitleAndDesc("uhc.advancements.soloist")
            toast()
            announce()
        }
        NOT_NOW = register {
            parent(ROOT)
            id = id("not_now")
            display(Items.NETHERITE_SWORD)
            setTitleAndDesc("uhc.advancements.notNow")
            toast()
            announce()
        }
        LDAP = register {
            parent(NOT_NOW)
            id = id("ldap")
            display(Items.EMERALD_BLOCK)
            setTitleAndDesc("uhc.advancements.ldap")
            toast()
            announce()
        }
        OFFICIALLY_BORED = register {
            parent(WORLD_RECORD_PACE)
            id = id("officially_bored")
            display(Items.COMMAND_BLOCK)
            setTitleAndDesc("uhc.advancements.officiallyBored")
            toast()
            announce()
        }
        DISTRACTED = register {
            parent(OFFICIALLY_BORED)
            id = id("distracted")
            display(Items.CHAIN_COMMAND_BLOCK)
            setTitleAndDesc("uhc.advancements.distracted")
            toast()
            announce()
        }
        UH_OH = register {
            parent(LDAP)
            id = id("uh_oh")
            display(Items.BARRIER)
            setTitleAndDesc("uhc.advancements.uhOh")
            toast()
            announce()
        }
        BASICALLY = register {
            parent(LDAP)
            id = id("basically")
            display(Items.WHITE_WOOL)
            setTitleAndDesc("uhc.advancements.basically")
            toast()
            announce()
        }
        TEAM_PLAYER = register {
            parent(SOLOIST)
            id = id("team_player")
            display(Items.LIME_BANNER)
            setTitleAndDesc("uhc.advancements.teamPlayer")
            toast()
            announce()
        }
        FIND_THE_BUTTON = register {
            parent(OFFICIALLY_BORED)
            id = id("find_the_button")
            display(Items.STONE_BUTTON)
            setTitleAndDesc("uhc.advancements.findTheButton")
            type = AdvancementType.CHALLENGE
            toast()
            announce()
        }
        LAST_MAN_STANDING = register {
            parent(SOLOIST)
            id = id("soloist")
            display(Items.ARROW)
            setTitleAndDesc("uhc.advancements.lastManStanding")
            toast()
            announce()
        }
        BEAR_CARED = register {
            parent(EMBARRASSING)
            id = id("bear_cared")
            display(Items.SCULK_SENSOR)
            setTitleAndDesc("uhc.advancements.bearCared")
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
