package net.casual.championships.lobby.advancement

import net.casual.arcade.utils.AdvancementUtils.setTitleAndDesc
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.advancement.AdvancementCollection
import net.casual.championships.common.ui.minesweeper.MinesweeperItems
import net.casual.championships.common.util.casual
import net.minecraft.advancements.AdvancementType
import net.minecraft.core.ClientAsset
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items

object LobbyAdvancements: AdvancementCollection() {
    val ROOT = register {
        id = casual("lobby_root")
        display(Items.GOLDEN_APPLE)
        setTitleAndDesc("lobby.advancements.root")
        background = ClientAsset.ResourceTexture(ResourceLocation.withDefaultNamespace("gui/advancements/backgrounds/adventure"))
    }

    val UH_OH = register {
        parent(ROOT)
        id = casual("uh_oh")
        display(Items.BARRIER)
        setTitleAndDesc("lobby.advancements.uhOh")
        toast()
        announce()
    }

    val PARKOUR_MASTER = register {
        parent(UH_OH)
        id = casual("parkour_master")
        display(Items.NETHERITE_BOOTS)
        setTitleAndDesc("lobby.advancements.parkourMaster")
        type = AdvancementType.CHALLENGE
        toast()
        announce()
    }

    val NOT_NOW = register {
        parent(ROOT)
        id = casual("not_now")
        display(Items.NETHERITE_SWORD)
        setTitleAndDesc("lobby.advancements.notNow")
        announce()
    }

    val OFFICIALLY_BORED = register {
        parent(ROOT)
        id = casual("officially_bored")
        display(Items.COMMAND_BLOCK)
        setTitleAndDesc("lobby.advancements.officiallyBored")
        toast()
        announce()
    }

    val ADMIN_ABUSE = register {
        parent(OFFICIALLY_BORED)
        id = casual("admin_abuse")
        display(ItemUtils.createPlayerHead("senseiwells"))
        setTitleAndDesc("lobby.advancements.adminAbuse")
        toast()
        announce()
    }

    val GAMER = register {
        parent(ROOT)
        id = casual("gamer")
        display(MinesweeperItems.FLAG_COUNTER)
        setTitleAndDesc("lobby.advancements.gamer")
        toast()
        announce()
    }

    val YOU_SHALL_NOT_LEAVE = register {
        parent(ROOT)
        id = casual("you_shall_not_leave")
        display(Items.STRUCTURE_VOID)
        setTitleAndDesc("lobby.advancements.youShallNotLeave")
        toast()
        announce()
    }
}