package net.casual.championships.util

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.PlayerUtils.resetHealth
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.*

object CasualPlayerUtils {
    val HEALTH_BOOST: UUID = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57")

    fun ServerPlayer.isMessageGlobal(message: String, minigame: Minigame<*>): Boolean {
        val team = this.team
        return team === null || minigame.teams.isTeamIgnored(team) || message.startsWith('!')
    }

    fun ServerPlayer.isAliveSolo(): Boolean {
        if (!this.isSurvival) {
            return false
        }

        val team = this.team ?: return false
        for (name in team.players) {
            val player = PlayerUtils.player(name)
            if (player != null && player != this && player.isSurvival) {
                return false
            }
        }
        return true
    }

    fun ServerPlayer.updateGlowingTag() {
        this.setGlowingTag(!this.hasGlowingTag())
        this.setGlowingTag(!this.hasGlowingTag())
    }

    fun ServerPlayer.boostHealth(multiply: Double) {
        val instance = this.attributes.getInstance(Attributes.MAX_HEALTH)
        if (instance != null) {
            instance.removeModifier(HEALTH_BOOST)
            instance.addPermanentModifier(
                AttributeModifier(
                    HEALTH_BOOST,
                    "Health Boost",
                    multiply,
                    AttributeModifier.Operation.MULTIPLY_BASE
                )
            )
        }
    }
}