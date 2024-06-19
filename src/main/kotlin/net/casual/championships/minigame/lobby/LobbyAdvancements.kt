package net.casual.championships.minigame.lobby

import net.casual.arcade.advancements.AdvancementBuilder
import net.casual.arcade.utils.AdvancementUtils.setTitleAndDesc
import net.casual.championships.CasualMod
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items

object LobbyAdvancements {
    private val ALL = LinkedHashSet<AdvancementHolder>()

    val ROOT = register {
        id = CasualMod.id("root")
        display(Items.GOLDEN_APPLE)
        setTitleAndDesc("lobby.advancements.root")
        background = ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png")
    }

    val UH_OH = register {
        parent(ROOT)
        id = CasualMod.id("uh_oh")
        display(Items.BARRIER)
        setTitleAndDesc("lobby.advancements.uhOh")
        toast()
        announce()
    }

    val PARKOUR_MASTER = register {
        parent(UH_OH)
        id = CasualMod.id("parkour_master")
        display(Items.NETHERITE_BOOTS)
        setTitleAndDesc("lobby.advancements.parkourMaster")
        type = AdvancementType.CHALLENGE
        toast()
        announce()
    }

    val NOT_NOW = register {
        parent(ROOT)
        id = CasualMod.id("not_now")
        display(Items.NETHERITE_SWORD)
        setTitleAndDesc("lobby.advancements.notNow")
        toast()
        announce()
    }

    val OFFICIALLY_BORED = register {
        parent(ROOT)
        id = CasualMod.id("officially_bored")
        display(Items.COMMAND_BLOCK)
        setTitleAndDesc("lobby.advancements.officiallyBored")
        toast()
        announce()
    }

    fun register(builder: AdvancementBuilder.() -> Unit): AdvancementHolder {
        val advancement = AdvancementBuilder.create(builder).build()
        ALL.add(advancement)
        return advancement
    }

    fun getAllAdvancements(): Collection<AdvancementHolder> {
        return ALL
    }
}