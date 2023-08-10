package net.casualuhc.uhcmod.managers

import com.google.gson.JsonObject
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.team.TeamCreatedEvent
import net.casualuhc.arcade.utils.ComponentUtils.bold
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.ComponentUtils.green
import net.casualuhc.arcade.utils.ComponentUtils.red
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.arcade.utils.PlayerUtils.location
import net.casualuhc.arcade.utils.PlayerUtils.teleportTo
import net.casualuhc.arcade.utils.TeamUtils
import net.casualuhc.arcade.utils.TeamUtils.addExtension
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.events.uhc.UHCActiveEvent
import net.casualuhc.uhcmod.events.uhc.UHCLobbyEvent
import net.casualuhc.uhcmod.events.uhc.UHCReadyEvent
import net.casualuhc.uhcmod.events.uhc.UHCSetupEvent
import net.casualuhc.uhcmod.extensions.PlayerFlag.Participating
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.TeamFlag.Ignored
import net.casualuhc.uhcmod.extensions.TeamFlag.Ready
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.TeamUHCExtension
import net.casualuhc.uhcmod.managers.PlayerManager.setForUHC
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*

object TeamManager {
    private var collisions = Team.CollisionRule.ALWAYS

    val configPath = Config.resolve("teams.json")

    fun getSpectatorTeam(): PlayerTeam {
        return Arcade.server.scoreboard.getPlayerTeam("Spectator")!!
    }

    fun Team.hasAlivePlayers(): Boolean {
        val names = this.players
        for (name in names) {
            val player = Arcade.server.playerList.getPlayerByName(name)
            if (player != null && player.isSurvival) {
                return true
            }
        }
        return false
    }

    fun isOneTeamRemaining(): Boolean {
        var team: Team? = null
        for (player in PlayerUtils.players()) {
            if (player.isSurvival) {
                team = if (team === null || team.flags.has(Ignored)) player.team else team
                if (team != null && !team.flags.has(Ignored) && team !== player.team) {
                    return false
                }
            }
        }
        return true
    }

    fun getAnyAliveTeam(): Team? {
        for (player in PlayerUtils.players()) {
            if (player.isSurvival) {
                return player.team
            }
        }
        return null
    }

    fun checkAllTeamsReady() {
        if (this.getUnreadyTeams().isNotEmpty()) {
            return
        }
        PlayerUtils.forEveryPlayer { player ->
            player.sendSystemMessage(Texts.LOBBY_READY_ALL_READY.copy().gold())
            if (player.hasPermissions(4)) {
                player.sendSystemMessage(Component.literal("[Click here to start]").green().withStyle { s ->
                    s.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc start force"))
                })
            }
        }
    }

    fun getUnreadyTeams(): List<PlayerTeam> {
        val teams = LinkedList<PlayerTeam>()
        val scoreboard = Arcade.server.scoreboard
        for (team in scoreboard.playerTeams) {
            var teamHasMember = false
            for (name in team.players) {
                val player = Arcade.server.playerList.getPlayerByName(name)
                if (player != null) {
                    teamHasMember = true
                    break
                }
            }

            if (teamHasMember && !team.flags.has(Ready) && !team.flags.has(Ignored)) {
                teams.add(team)
            }
        }
        return teams
    }

    fun forceAddPlayer(team: PlayerTeam, target: ServerPlayer, teleport: Boolean) {
        Arcade.server.scoreboard.addPlayerToTeam(target.scoreboardName, team)
        target.sendSystemMessage(Texts.UHC_ADDED_TO_TEAM.generate(team.formattedDisplayName))

        target.setForUHC(!target.flags.has(Participating))

        if (teleport) {
            for (player in PlayerUtils.players()) {
                if (team.players.contains(player.scoreboardName) && player.isSurvival && target != player) {
                    target.teleportTo(player.location)
                    break
                }
            }
        }
    }

    fun createTeams() {
        val scoreboard = Arcade.server.scoreboard
        for (team in LinkedList(scoreboard.playerTeams)) {
            scoreboard.removePlayerTeam(team)
        }

        DataManager.database.downloadTeams().thenAcceptAsync({
            for (team in it) {
                this.createTeamFromJson(team)
            }

            val operators = scoreboard.addPlayerTeam("Operator")
            operators.color = ChatFormatting.WHITE
            operators.playerPrefix = Component.literal("[OP] ")
            for (operator in Config.listOrDefault<String>("operators")) {
                scoreboard.addPlayerToTeam(operator, operators)
            }
            operators.collisionRule = Team.CollisionRule.NEVER
            operators.flags.set(Ignored, true)

            val spectators = scoreboard.addPlayerTeam("Spectator")
            spectators.color = ChatFormatting.DARK_GRAY
            spectators.collisionRule = Team.CollisionRule.NEVER
            spectators.flags.set(Ignored, true)

            PlayerUtils.forEveryPlayer { player ->
                if (player.team == null) {
                    scoreboard.addPlayerToTeam(player.scoreboardName, spectators)
                }
            }
        }, Arcade.server).exceptionally {
            UHCMod.logger.error("Failed to download teams from database", it)
            null
        }
    }

    fun setCollisions(shouldCollide: Boolean) {
        this.collisions = if (shouldCollide) Team.CollisionRule.ALWAYS else Team.CollisionRule.NEVER
        for (team in Arcade.server.scoreboard.playerTeams) {
            team.collisionRule = this.collisions
        }
    }

    fun spawnFakeUHCPlayers(): Boolean {
        if (!UHCManager.isLobbyPhase()) {
            return false
        }
        // val players = Arcade.server.playerList
        // val location = UHCManager.event.getLobbyHandler().getSpawn()
        // for (team in TeamUtils.teams()) {
        //     for (name in team.players) {
        //         if (players.getPlayerByName(name) == null) {
        //             EntityPlayerMPFake.createFake(
        //                 name,
        //                 Arcade.server,
        //                 location.x,
        //                 location.y,
        //                 location.z,
        //                 location.yaw.toDouble(),
        //                 location.pitch.toDouble(),
        //                 location.level.dimension(),
        //                 GameType.ADVENTURE,
        //                 false
        //             )
        //         }
        //     }
        // }
        return true
    }

    fun killAllFakePlayers() {
        PlayerUtils.forEveryPlayer { player ->
            // if (player is EntityPlayerMPFake) {
            //     player.kill()
            // }
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<TeamCreatedEvent> { this.onTeamCreated(it) }
        GlobalEventHandler.register<UHCSetupEvent> { this.createTeams() }
        GlobalEventHandler.register<UHCReadyEvent> { this.onUHCReady() }
        GlobalEventHandler.register<UHCLobbyEvent> { this.onUHCLobby() }
        GlobalEventHandler.register<UHCActiveEvent> { this.onUHCActive() }
    }

    private fun onTeamCreated(event: TeamCreatedEvent) {
        event.team.addExtension(TeamFlagsExtension())
        event.team.addExtension(TeamUHCExtension())
    }

    private fun onUHCReady() {
        for (team in TeamUtils.teams()) {
            team.flags.set(Ready, false)
        }

        val bar = Component.literal("══════════════════").gold()
        val yes = Component.literal("[").append(Texts.LOBBY_YES).append("]").bold().green().withStyle {
            it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes"))
        }
        val no = Component.literal("[").append(Texts.LOBBY_NO).append("]").bold().red().withStyle {
            it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no"))
        }
        val readyMessage = bar.copy()
            .append("\n      ")
            .append(Texts.LOBBY_READY_QUESTION)
            .append("\n\n\n       ")
            .append(yes)
            .append("        ")
            .append(no)
            .append("\n\n\n")
            .append(bar)

        PlayerUtils.forEveryPlayer { player ->
            val team = player.team
            if (team != null && !team.flags.has(Ignored)) {
                player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.MASTER, 1.0F, 1.0F)
                player.sendSystemMessage(readyMessage)
            }
        }
    }

    private fun onUHCLobby() {
        this.setCollisions(false)
        for (team in Arcade.server.scoreboard.playerTeams) {
            team.nameTagVisibility = Team.Visibility.ALWAYS
        }
    }

    private fun onUHCActive() {
        this.setCollisions(true)
        for (team in Arcade.server.scoreboard.playerTeams) {
            if (team.flags.has(Ignored)) {
                team.nameTagVisibility = Team.Visibility.NEVER
            }
        }
    }

    private fun createTeamFromJson(json: JsonObject) {
        if (json.has("_id") && json.has("colour") && json.has("prefix") && json.has("members")) {
            val teamName = json["_id"].asString
            val colour = json["colour"].asString
            val prefix = "[${json["prefix"].asString}] "
            val members = json["members"].asJsonArray
            val scoreboard = Arcade.server.scoreboard
            var team = scoreboard.getPlayerTeam(teamName)
            if (team == null) {
                team = scoreboard.addPlayerTeam(teamName)!!
            }
            team.playerPrefix = Component.literal(prefix)
            val formatting = ChatFormatting.getByName(colour)
            if (formatting != null) {
                team.color = formatting
            }

            for (member in members) {
                scoreboard.addPlayerToTeam(member.asString, team)
            }
            team.isAllowFriendlyFire = false
            team.collisionRule = collisions
        }
    }
}
