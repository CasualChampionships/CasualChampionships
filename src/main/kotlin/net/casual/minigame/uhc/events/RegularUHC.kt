package net.casual.minigame.uhc.events

import net.casual.arcade.area.StructureArea
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.utils.StructureUtils
import net.casual.arcade.utils.impl.Location
import net.casual.minigame.Dimensions
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.resources.UHCResources
import net.casual.util.Config
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

class RegularUHC: UHCEvent, Lobby {
    override val area = StructureArea(
        StructureUtils.read(Config.resolve("lobbies/cherry_lobby.nbt")),
        Vec3i(0, 320, 0),
        Dimensions.getLobbyLevel()
    )
    override val spawn = Location(Dimensions.getLobbyLevel(), Vec3(-0.5, 310.0, 11.5), Vec2(180.0F, 0.0F))

    override fun getTeamSize(): Int {
        return 5
    }

    override fun getLobby(): Lobby {
        return this
    }

    override fun getCountdown(): Countdown {
        return TitledCountdown.DEFAULT
    }

    override fun getResourcePackHandler(): MinigameResources {
        return UHCResources
    }

    override fun initialise(uhc: UHCMinigame) {

    }
}