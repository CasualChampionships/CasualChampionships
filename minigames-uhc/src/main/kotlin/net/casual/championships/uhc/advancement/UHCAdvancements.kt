package net.casual.championships.uhc.advancement

import net.casual.arcade.utils.AdvancementUtils.setTitleAndDesc
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.arcade.utils.advancement.AdvancementCollection
import net.casual.championships.common.util.casual
import net.minecraft.advancements.AdvancementType
import net.minecraft.core.ClientAsset
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions

object UHCAdvancements: AdvancementCollection() {
    val ROOT = register {
        id = casual("uhc_root")
        display(Items.GOLDEN_APPLE)
        setTitleAndDesc("uhc.advancements.root")
        background = ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"))
    }

    val FIRST_BLOOD = register {
        parent(ROOT)
        id = casual("first_blood")
        display(Items.IRON_SWORD)
        setTitleAndDesc("uhc.advancements.firstBlood")
        toast()
        announce()
    }

    val OKAY_EZTAK = register {
        parent(FIRST_BLOOD)
        id = casual("okay_eztak")
        display(ItemUtils.createPlayerHead("eztak_red"))
        setTitleAndDesc("uhc.advancements.okayEztak")
        toast()
        announce()
    }

    val TELL_WITNESSES_THAT_I_WAS_MURDERED = register {
        parent(FIRST_BLOOD)
        id = casual("tell_witnesses_that_i_was_murdered")
        display(Items.EMERALD)
        setTitleAndDesc("uhc.advancements.tellWitnessesThatIWasMurdered")
        toast()
        announce()
    }

    val HEAVY_HITTER = register {
        parent(FIRST_BLOOD)
        id = casual("heavy_hitter")
        display(Items.ANVIL)
        setTitleAndDesc("uhc.advancements.heavyHitter")
        type = AdvancementType.CHALLENGE
        toast()
        announce()
    }

    val WINNER = register {
        parent(FIRST_BLOOD)
        id = casual("winner")
        display(Items.FIREWORK_ROCKET)
        setTitleAndDesc("uhc.advancements.winner")
        type = AdvancementType.CHALLENGE
        toast()
        announce()
    }

    val DEMOLITION_EXPERT = register {
        parent(ROOT)
        id = casual("demolition_expert")
        display(Items.TNT)
        setTitleAndDesc("uhc.advancements.demolitionExpert")
        toast()
        announce()
    }

    val PRINTER_ISNT_ALLOWED = register {
        parent(DEMOLITION_EXPERT)
        id = casual("printer_isnt_allowed")
        display(Items.RED_GLAZED_TERRACOTTA)
        setTitleAndDesc("uhc.advancements.printerIsntAllowed")
        toast()
        announce()
    }

    val WHATS_THIS = register {
        parent(DEMOLITION_EXPERT)
        id = casual("whats_this")
        display(Items.DIORITE)
        setTitleAndDesc("uhc.advancements.whatsThis")
        toast()
        announce()
    }

    val HUMAN_QUARRY = register {
        parent(DEMOLITION_EXPERT)
        id = casual("human_quarry")
        display(Items.DIAMOND_PICKAXE)
        setTitleAndDesc("uhc.advancements.humanQuarry")
        toast()
        announce()
    }

    val EARLY_EXIT = register {
        parent(ROOT)
        id = casual("early_exit")
        display(Items.POISONOUS_POTATO)
        setTitleAndDesc("uhc.advancements.earlyExit")
        toast()
        announce()
    }

    val WELL_THAT_WAS_A_BIT_SILLY = register {
        parent(EARLY_EXIT)
        id = casual("well_that_was_a_bit_silly")
        display(Items.BEDROCK)
        setTitleAndDesc("uhc.advancements.wellThatWasABitSilly")
        toast()
        announce()
    }

    val SKILL_ISSUE = register {
        parent(EARLY_EXIT)
        id = casual("skill_issue")
        display(Items.BONE)
        setTitleAndDesc("uhc.advancements.skillIssue")
        toast()
        announce()
    }

    val BEAR_CARED = register {
        parent(SKILL_ISSUE)
        id = casual("bear_cared")
        display(Items.SCULK_SENSOR)
        setTitleAndDesc("uhc.advancements.bearCared")
        toast()
        announce()
    }

    val COW_MOMENT = register {
        parent(SKILL_ISSUE)
        id = casual("cow_moment")
        display(Items.CARVED_PUMPKIN)
        setTitleAndDesc("uhc.advancements.cowMoment")
        toast()
        announce()
    }

    val WHERE_WAS_MY_PORTAL = register {
        parent(SKILL_ISSUE)
        id = casual("where_was_my_portal")
        display(Items.CRYING_OBSIDIAN)
        setTitleAndDesc("uhc.advancements.whereWasMyPortal")
        toast()
        announce()
    }

    val MOSTLY_HARMLESS = register {
        parent(EARLY_EXIT)
        id = casual("mostly_harmless")
        display(Items.FEATHER)
        setTitleAndDesc("uhc.advancements.mostlyHarmless")
        type = AdvancementType.CHALLENGE
        toast()
        announce()
    }

    val COMBAT_LOGGER = register {
        parent(ROOT)
        id = casual("combat_logger")
        display(Items.WOODEN_SWORD)
        setTitleAndDesc("uhc.advancements.combatLogger")
        toast()
        announce()
    }

    val OK_WE_BELIEVE_YOU_NOW = register {
        parent(COMBAT_LOGGER)
        id = casual("ok_we_believe_you_now")
        display(Items.WOODEN_HOE)
        setTitleAndDesc("uhc.advancements.okWeBelieveYouNow")
        toast()
        announce()
    }

    val NOT_DUSTLESS = register {
        parent(ROOT)
        id = casual("thats_not_dustless")
        display(Items.REDSTONE)
        setTitleAndDesc("uhc.advancements.notDustless")
        toast()
        announce()
    }

    val UPDATE_DEPRESSION = register {
        parent(NOT_DUSTLESS)
        id = casual("update_depression")
        display(Items.POWERED_RAIL)
        setTitleAndDesc("uhc.advancements.updateDepression")
        toast()
        announce()
    }

    val SLIMESTONER = register {
        parent(NOT_DUSTLESS)
        id = casual("slimestoner")
        display(Items.SLIME_BLOCK)
        setTitleAndDesc("uhc.advancements.slimestoner")
        toast()
        announce()
    }

    val DOES_THIS_WORK_ON_PAPER = register {
        parent(SLIMESTONER)
        id = casual("does_this_work_on_paper")
        display(Items.PAPER)
        setTitleAndDesc("uhc.advancements.doesThisWorkOnPaper")
        toast()
        announce()
    }

    val STORAGE_TECH_IS_MY_PASSION = register {
        parent(NOT_DUSTLESS)
        id = casual("storage_tech_is_my_passion")
        display(Items.HOPPER)
        setTitleAndDesc("uhc.advancements.storageTechIsMyPassion")
        toast()
        announce()
    }

    val MAIN_STORAGE = register {
        parent(STORAGE_TECH_IS_MY_PASSION)
        id = casual("main_storage")
        display(Items.CHEST)
        setTitleAndDesc("uhc.advancements.mainStorage")
        toast()
        announce()
    }

    val BEE_NES = register {
        parent(NOT_DUSTLESS)
        id = casual("bee_nes")
        display(Items.BEE_NEST)
        setTitleAndDesc("uhc.advancements.beeNes")
        toast()
        announce()
    }

    val FALLING_BLOCK = register {
        parent(NOT_DUSTLESS)
        id = casual("falling_block")
        display(Items.SAND)
        setTitleAndDesc("uhc.advancements.fallingBlock")
        toast()
        announce()
    }

    val WORLD_RECORD_PACE = register {
        parent(ROOT)
        id = casual("world_record_pace")
        display = Items.SPLASH_POTION.defaultInstance.potion(Potions.SWIFTNESS)
        setTitleAndDesc("uhc.advancements.worldRecordPace")
        toast()
        announce()
    }

    val SPEEDRUN_ANY_PERCENT = register {
        parent(WORLD_RECORD_PACE)
        id = casual("speedrun_any_percent")
        display = Items.END_PORTAL_FRAME.defaultInstance
        setTitleAndDesc("uhc.advancements.speedrunAnyPercent")
        toast()
        announce()
    }

    val BRAVE_CHOICE = register {
        parent(SPEEDRUN_ANY_PERCENT)
        id = casual("brave_choice")
        display(Items.END_STONE)
        setTitleAndDesc("uhc.advancements.braveChoice")
        toast()
        announce()
    }

    val DO_THE_IMPOSSIBLE = register {
        parent(BRAVE_CHOICE)
        id = casual("do_the_impossible")
        display = Items.DRAGON_HEAD.defaultInstance
        setTitleAndDesc("uhc.advancements.doTheImpossible")
        toast()
        announce()
    }

    val EMBARRASSING = register {
        parent(ROOT)
        id = casual("thats_embarrassing")
        display(Items.SWEET_BERRIES)
        setTitleAndDesc("uhc.advancements.thatsEmbarrassing")
        toast()
        announce()
    }

    val FORGOT_YOUR_DOOR = register {
        parent(EMBARRASSING)
        id = casual("forgot_your_door")
        display(Items.OAK_DOOR)
        setTitleAndDesc("uhc.advancements.forgotYourDoor")
        toast()
        announce()
    }

    val CONCRETE_SMELTER = register {
        parent(EMBARRASSING)
        id = casual("concrete_smelter")
        display(Items.PINK_CONCRETE_POWDER)
        setTitleAndDesc("uhc.advancements.concreteSmelter")
        toast()
        announce()
    }

    val NO_ONE_ASKED = register {
        parent(EMBARRASSING)
        id = casual("no_one_asked")
        display(Items.BLUE_BED)
        setTitleAndDesc("uhc.advancements.noOneAsked")
        toast()
        announce()
    }

    val VELIZARD = register {
        parent(NO_ONE_ASKED)
        id = casual("velizard")
        display(Items.WHITE_BED)
        setTitleAndDesc("uhc.advancement.velizard")
        toast()
        announce()
    }

    val BROKEN_ANKLES = register {
        parent(EMBARRASSING)
        id = casual("broken_ankles")
        display(Items.LEATHER_BOOTS)
        setTitleAndDesc("uhc.advancements.brokenAnkles")
        toast()
        announce()
    }

    val ON_THE_EDGE = register {
        parent(BROKEN_ANKLES)
        id = casual("on_the_edge")
        display = Items.SPLASH_POTION.defaultInstance.potion(Potions.STRONG_HARMING)
        setTitleAndDesc("uhc.advancements.onTheEdge")
        toast()
        announce()
    }

    val DOES_YOUR_PINKIE_HURT_YET = register {
        parent(ON_THE_EDGE)
        id = casual("does_your_pinkie_hurt_yet")
        display = Items.POTION.defaultInstance.potion(Potions.INVISIBILITY)
        setTitleAndDesc("uhc.advancements.doesYourPinkieHurtYet")
        toast()
        announce()
    }

    val BUSTED = register {
        parent(ROOT)
        id = casual("busted")
        display(Items.STRUCTURE_VOID)
        setTitleAndDesc("uhc.advancements.busted")
        toast()
        announce()
    }

    val HOWD_THAT_HAPPEN = register {
        parent(BUSTED)
        id = casual("howd_that_happen")
        display = Items.POTION.defaultInstance.potion(Potions.HEALING)
        setTitleAndDesc("uhc.advancements.howdThatHappen")
        toast()
        announce()
    }

    val DREAM_LUCK = register {
        parent(BUSTED)
        id = casual("dream_luck")
        display(Items.ENCHANTED_GOLDEN_APPLE)
        setTitleAndDesc("uhc.advancements.dreamLuck")
        toast()
        announce()
    }

    val PERFECTLY_BALANCED = register {
        parent(DREAM_LUCK)
        id = casual("perfectly_balanced")
        display(Items.TOTEM_OF_UNDYING)
        setTitleAndDesc("uhc.advancements.perfectlyBalanced")
        toast()
        announce()
    }

    val SOLOIST = register {
        parent(ROOT)
        id = casual("soloist")
        display(Items.PLAYER_HEAD)
        setTitleAndDesc("uhc.advancements.soloist")
        toast()
        announce()
    }

    val TEAM_PLAYER = register {
        parent(SOLOIST)
        id = casual("team_player")
        display(Items.LIME_BANNER)
        setTitleAndDesc("uhc.advancements.teamPlayer")
        toast()
        announce()
    }

    val LAST_MAN_STANDING = register {
        parent(SOLOIST)
        id = casual("last_man_standing")
        display(Items.ARROW)
        setTitleAndDesc("uhc.advancements.lastManStanding")
        toast()
        announce()
    }

    val LDAP = register {
        parent(ROOT)
        id = casual("ldap")
        display(Items.EMERALD_BLOCK)
        setTitleAndDesc("uhc.advancements.ldap")
        toast()
        announce()
    }

    val BASICALLY = register {
        parent(LDAP)
        id = casual("basically")
        display(Items.WHITE_WOOL)
        setTitleAndDesc("uhc.advancements.basically")
        toast()
        announce()
    }

    val ADVANCEMENT_HUNTER = register {
        parent(ROOT)
        id = casual("advancement_hunter")
        display(Items.GOLD_BLOCK)
        setTitleAndDesc("uhc.advancements.advancementHunter")
        toast()
        announce()
    }

    val WHATS_THAT_ABOUT_BARRIERS = register {
        parent(ADVANCEMENT_HUNTER)
        id = casual("whats_that_about_barriers")
        display(Items.BARRIER)
        setTitleAndDesc("uhc.advancements.whatsThatAboutBarriers")
        toast()
        announce()
    }

    val TRIAL_MEMBER = register {
        parent(ADVANCEMENT_HUNTER)
        id = casual("trial_member")
        display(Items.OMINOUS_TRIAL_KEY)
        setTitleAndDesc("uhc.advancements.trialMember")
        toast()
        announce()
    }

    val COOL_HOUSE_BRO = register {
        parent(ADVANCEMENT_HUNTER)
        id = casual("cool_house_bro")
        display(Items.DARK_OAK_PLANKS)
        setTitleAndDesc("uhc.advancements.coolHouseBro")
        toast()
        announce()
    }

    val THE_END_IS_NEAR = register {
        parent(ADVANCEMENT_HUNTER)
        id = casual("the_end_is_near")
        display(Items.ENDER_EYE)
        setTitleAndDesc("uhc.advancements.theEndIsNear")
        toast()
        announce()
    }

    val WART_HOARDER = register {
        parent(ROOT)
        id = casual("wart_hoarder")
        display(Items.NETHER_WART)
        setTitleAndDesc("uhc.advancements.wartHoarder")
        toast()
        announce()
    }

    val SPAWNER_SABOTEUR = register {
        parent(WART_HOARDER)
        id = casual("spawner_saboteur")
        display(Items.BLAZE_POWDER)
        setTitleAndDesc("uhc.advancements.spawnerSaboteur")
        toast()
        announce()
    }
}
