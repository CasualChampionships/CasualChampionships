package net.casual.championships.uhc.advancement

import com.google.gson.JsonObject
import net.casual.arcade.events.player.*
import net.casual.arcade.minigame.annotation.HAS_PLAYER_PLAYING
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.stats.ArcadeStats
import net.casual.arcade.task.impl.PlayerTask
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.casual.arcade.utils.PlayerUtils.getKillCreditWith
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.StatUtils.increment
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.championships.common.event.PlayerCheatEvent
import net.casual.championships.uhc.BORDER_FINISHED_ID
import net.casual.championships.uhc.GRACE_ID
import net.casual.championships.uhc.UHCMinigame
import net.casual.championships.uhc.UHCStats
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FallingBlock

class UHCAdvancementManager(
    private val uhc: UHCMinigame
): MinigameEventListener {
    private val claimed = HashSet<UHCRaceAdvancement>()

    fun grantFinalAdvancements(winners: List<ServerPlayer>) {
        val alive = winners.filter { it.isSurvival }
        if (alive.size == 1) {
            alive[0].grantAdvancement(UHCAdvancements.LAST_MAN_STANDING)
        }

        for (player in winners) {
            player.grantAdvancement(UHCAdvancements.WINNER)
        }

        var lowest: PlayerAttacker? = null
        var highest: PlayerAttacker? = null
        for (player in this.uhc.getAllPlayers()) {
            val team = player.team
            if (team != null && this.uhc.teams.isTeamIgnored(team)) {
                val current = this.uhc.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_DEALT).value
                if (lowest === null) {
                    val first = PlayerAttacker(player, current)
                    lowest = first
                    highest = first
                }
                if (lowest.damage > current) {
                    lowest = PlayerAttacker(player, current)
                } else if (highest!!.damage < current) {
                    highest = PlayerAttacker(player, current)
                }
            }
        }
        if (lowest != null) {
            lowest.player.grantAdvancement(UHCAdvancements.MOSTLY_HARMLESS)
            highest!!.player.grantAdvancement(UHCAdvancements.HEAVY_HITTER)
        }
    }

    fun serialize(): JsonObject {
        val json = JsonObject()
        json.add("claimed", this.claimed.toJsonStringArray { it.name })
        return json
    }

    fun deserialize(json: JsonObject) {
        for (claimed in json.array("claimed")) {
            this.claimed.add(UHCRaceAdvancement.valueOf(claimed.asString))
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING, priority = 2000)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val relogs = this.uhc.stats.getOrCreateStat(event.player, ArcadeStats.RELOGS)

        // Wait for player to load in
        GlobalTickedScheduler.schedule(1.Seconds, PlayerTask(event.player) { player ->
            player.grantAdvancement(UHCAdvancements.COMBAT_LOGGER)
            if (relogs.value == 10) {
                player.grantAdvancement(UHCAdvancements.OK_WE_BELIEVE_YOU_NOW)
            }
        })

        val team = event.player.team
        if (team !== null && this.uhc.teams.isTeamEliminated(team)) {
            this.uhc.teams.removeEliminatedTeam(team)
            event.player.grantAdvancement(UHCAdvancements.TEAM_PLAYER)
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING)
    private fun onPlayerDeath(event: PlayerDeathEvent) {
        if (this.claimed.add(UHCRaceAdvancement.Death)) {
            event.player.grantAdvancement(UHCAdvancements.EARLY_EXIT)
        }

        if (event.source.`is`(DamageTypes.OUTSIDE_BORDER)) {
            event.player.grantAdvancement(UHCAdvancements.SKILL_ISSUE)
        }

        val killer = event.player.getKillCreditWith(event.source)
        if (this.claimed.add(UHCRaceAdvancement.Kill) && killer is ServerPlayer) {
            killer.grantAdvancement(UHCAdvancements.FIRST_BLOOD)
        }
        if (killer is Warden) {
            event.player.grantAdvancement(UHCAdvancements.BEAR_CARED)
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING)
    private fun onPlayerBlockPlaced(event: PlayerBlockPlacedEvent) {
        val state = event.state
        val block = state.block
        val context = event.context
        val pos = context.clickedPos
        val world = context.level
        if (block is FallingBlock && FallingBlock.isFree(world.getBlockState(pos.below()))) {
            event.player.grantAdvancement(UHCAdvancements.FALLING_BLOCK)
        } else if (block === Blocks.REDSTONE_WIRE) {
            event.player.grantAdvancement(UHCAdvancements.NOT_DUSTLESS)
        } else if (block === Blocks.TNT) {
            event.player.grantAdvancement(UHCAdvancements.DEMOLITION_EXPERT)
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING)
    private fun onPlayerCraft(event: PlayerCraftEvent) {
        if (event.stack.`is`(Items.CRAFTING_TABLE) && this.claimed.add(UHCRaceAdvancement.Craft)) {
            event.player.grantAdvancement(UHCAdvancements.WORLD_RECORD_PACE)
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING)
    private fun onPlayerLoot(event: PlayerLootEvent) {
        if (event.items.any { it.`is`(Items.ENCHANTED_GOLDEN_APPLE) }) {
            event.player.grantAdvancement(UHCAdvancements.DREAM_LUCK)
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING)
    private fun onPlayerTick(event: PlayerTickEvent) {
        val player = event.player
        val stat = this.uhc.stats.getOrCreateStat(player, UHCStats.HALF_HEART_TIME)
        if (player.gameMode.isSurvival && player.health <= 1.0F) {
            stat.increment()
            if (stat.value == 1200) {
                player.grantAdvancement(UHCAdvancements.ON_THE_EDGE)
            }
        } else {
            stat.modify { 0 }
        }
    }

    @Listener(phases = [GRACE_ID], flags = HAS_PLAYER_PLAYING)
    private fun onPlayerDamage(event: PlayerDamageEvent) {
        if (this.uhc.uptime < 1200 && event.source.`is`(DamageTypes.FALL) && event.amount > 0.0F) {
            event.player.grantAdvancement(UHCAdvancements.BROKEN_ANKLES)
        }
    }

    @Listener(before = BORDER_FINISHED_ID)
    private fun onPlayerChat(event: PlayerChatEvent) {
        val message = event.message.signedContent().lowercase()
        if (this.uhc.chat.isMessageGlobal(event.player, message)) {
            if (message.contains("jndi") && message.contains("ldap")) {
                event.player.grantAdvancement(UHCAdvancements.LDAP)
            }
            if (message.contains("basically")) {
                event.player.grantAdvancement(UHCAdvancements.BASICALLY)
            }
        }
    }

    @Listener(before = BORDER_FINISHED_ID, flags = HAS_PLAYER_PLAYING)
    private fun onPlayerBlockCollision(event: PlayerBlockCollisionEvent) {
        if (event.state.`is`(Blocks.SWEET_BERRY_BUSH)) {
            event.player.grantAdvancement(UHCAdvancements.EMBARRASSING)
        }
    }

    @Listener(flags = HAS_PLAYER_PLAYING)
    private fun onPlayerAdvancement(event: PlayerAdvancementEvent) {
        event.announce = event.announce && UHCAdvancements.isRegistered(event.advancement) && this.uhc.isPlaying(event.player)
    }

    @Listener(flags = HAS_PLAYER_PLAYING)
    private fun onPlayerCheat(event: PlayerCheatEvent) {
        event.player.grantAdvancement(UHCAdvancements.BUSTED)
    }

    private class PlayerAttacker(
        val player: ServerPlayer,
        val damage: Float
    )
}