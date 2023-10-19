package net.casual.minigame.uhc.events

import net.casual.arcade.area.BoxedArea
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.utils.impl.Location
import net.casual.minigame.Dimensions
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.resources.UHCResources
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

object DefaultUHC: UHCEvent, Lobby {
    override val area by lazy { BoxedArea(Vec3i(0, 300, 0), 40, 10, Dimensions.getLobbyLevel()) }
    override val spawn = Location(this.area.level, Vec3(0.0, 302.0, 0.0), Vec2(0.0F, 0.0F))

    override fun getCountdown(): Countdown {
        return TitledCountdown.DEFAULT
    }

    override fun getTeamSize(): Int {
        return 5
    }

    override fun getLobby(): Lobby {
        return this
    }

    override fun getResourcePackHandler(): MinigameResources {
        return UHCResources
    }

    override fun initialise(uhc: UHCMinigame) {
        uhc.server.motd = "            §6፠ §bWelcome to Casual UHC! §6፠\n     §6Yes, it's back! Is your team prepared?"
    }
}