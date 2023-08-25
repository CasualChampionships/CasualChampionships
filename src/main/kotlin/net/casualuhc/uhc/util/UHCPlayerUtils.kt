package net.casualuhc.uhc.util

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.minigame.Minigame
import net.casualuhc.arcade.utils.ComponentUtils.bold
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.MinigameUtils.getMinigame
import net.casualuhc.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casualuhc.arcade.utils.PlayerUtils.grantAdvancement
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.uhc.UHCMod
import net.casualuhc.uhc.advancement.UHCAdvancements
import net.casualuhc.uhc.extensions.PlayerFlag.*
import net.casualuhc.uhc.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhc.extensions.PlayerUHCExtension.Companion.uhc
import net.casualuhc.uhc.extensions.TeamFlag.Eliminated
import net.casualuhc.uhc.extensions.TeamFlag.Ignored
import net.casualuhc.uhc.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhc.extensions.TeamUHCExtension.Companion.uhc
import net.casualuhc.uhc.minigame.uhc.UHCMinigame
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.Team
import java.util.*

object UHCPlayerUtils {
    private val HEALTH_BOOST = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57")

    fun ServerPlayer.isMessageGlobal(message: String): Boolean {
        val team = this.team
        return !UHCMod.minigame.isActivePhase() || team === null || team.flags.has(Ignored) || message.startsWith('!')
    }

    fun ServerPlayer.isAliveSolo(): Boolean {
        if (!this.isSurvival) {
            return false
        }

        val team = this.team ?: return false
        for (name in team.players) {
            val player = Arcade.server.playerList.getPlayerByName(name)
            if (player != null && player != this && player.isSurvival) {
                return false
            }
        }
        return true
    }

    fun ServerPlayer.resetUHCHealth() {
        val minigame = this.getMinigame()
        if (minigame !is UHCMinigame) {
            return
        }

        val instance = this.attributes.getInstance(Attributes.MAX_HEALTH)
        if (instance != null) {
            instance.removeModifier(HEALTH_BOOST)
            instance.addPermanentModifier(
                AttributeModifier(
                    HEALTH_BOOST,
                    "Health Boost",
                    minigame.settings.health,
                    AttributeModifier.Operation.MULTIPLY_BASE
                )
            )
        }
        this.health = this.maxHealth
        this.foodData.setSaturation(20.0F)
    }

    fun ServerPlayer.setForUHC(minigame: UHCMinigame, force: Boolean = true) {
        minigame.addPlayer(this)

        this.connection.send(ClientboundSetTitleTextPacket(Texts.LOBBY_GOOD_LUCK.copy().gold().bold()))
        this.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.MASTER, 1.0F, 1.0F)

        if (force) {
            this.clearPlayerInventory()
            this.resetUHCHealth()
            this.experienceLevel = 0
            this.experienceProgress = 0.0F
        }

        this.removeVehicle()
        this.setGlowingTag(false)

        val flags = this.flags
        flags.set(FullBright, true)

        val team = this.team
        if (team != null && !team.flags.has(Ignored)) {
            team.flags.set(Eliminated, false)

            if (team.players.size == 1) {
                this.grantAdvancement(UHCAdvancements.SOLOIST)
            }

            flags.set(Participating, true)
            flags.set(TeamGlow, true)

            this.uhc.originalTeam = team
            team.uhc.add(this.scoreboardName)

            this.addEffect(MobEffectInstance(DAMAGE_RESISTANCE, 200, 4, true, false))
            this.setGameMode(GameType.SURVIVAL)
            return
        }

        this.setGameMode(GameType.SPECTATOR)
    }

    fun ServerPlayer.updateGlowingTag() {
        this.setGlowingTag(!this.hasGlowingTag())
        this.setGlowingTag(!this.hasGlowingTag())
    }

    fun ServerPlayer.sendUHCResourcePack() {
        this.uhc.hasPack = false
        val info = UHCMod.minigame.event.getResourcePackHandler().getInfo(this)
        if (info !== null) {
            this.sendTexturePack(info.url, info.hash, info.isRequired, info.prompt)
        }
    }

    fun ServerPlayer.belongsToTeam(team: Team): Boolean {
        if (this.team === team) {
            return true
        }
        val original = this.uhc.originalTeam
        val players = team.uhc.players
        if (original == team) {
            if (!players.contains(this.scoreboardName)) {
                UHCMod.logger.warn(
                    "Player ${this.scoreboardName} had team ${team.name} registered, but team didn't recognise player??!"
                )
            }
            return true
        } else if (players.contains(this.scoreboardName)) {
            UHCMod.logger.warn(
                "Team ${team.name} had player ${this.scoreboardName} registered, but player didn't recognise team??!"
            )
            return true
        }
        return false
    }
}