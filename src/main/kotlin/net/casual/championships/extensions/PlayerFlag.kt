package net.casual.championships.extensions

import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.championships.util.CasualPlayerUtils.updateGlowingTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
import net.minecraft.world.effect.MobEffects.NIGHT_VISION

enum class PlayerFlag {
    Participating,
    Won,
    TeamGlow {
        override fun trigger(player: ServerPlayer, value: Boolean) {
            val team = player.team ?: return player.updateGlowingTag()
            for (member in team.getOnlinePlayers()) {
                member.updateGlowingTag()
            }
        }
    },
    FullBright {
        override fun trigger(player: ServerPlayer, value: Boolean) {
            if (value) {
                player.addEffect(MobEffectInstance(NIGHT_VISION, INFINITE_DURATION, 0, false, false))
            } else {
                player.removeEffect(NIGHT_VISION)
            }
        }
    },
    WasInBorder;

    open fun trigger(player: ServerPlayer, value: Boolean) {

    }
}