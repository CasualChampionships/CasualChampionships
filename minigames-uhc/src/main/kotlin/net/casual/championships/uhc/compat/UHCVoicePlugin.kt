package net.casual.championships.uhc.compat

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import de.maxhenkel.voicechat.api.*
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent
import net.casual.arcade.commands.*
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.events.server.player.PlayerTeamLeaveEvent
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.championships.uhc.CasualUHC
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.collections.HashMap

// TODO: Maybe we separate this out into common to add
//   support for voicechat in other minigames?
object UHCVoicePlugin: VoicechatPlugin {
    private lateinit var api: VoicechatServerApi

    override fun getPluginId(): String {
        return CasualUHC.MOD_ID
    }

    override fun initialize(api: VoicechatApi) {
        this.api = api as VoicechatServerApi
    }

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(PlayerConnectedEvent::class.java, this::onPlayerConnectedEvent)
        GlobalEventHandler.Server.register<MinigameInitializeEvent> { (minigame) ->
            if (minigame is UHCMinigame) {
                this.initialize(minigame)
            }
        }
    }

    private fun initialize(uhc: UHCMinigame) {
        val groups = HashMap<Team, Group>()
        uhc.events.register<MinigameSetSpectatingEvent> { (_, player) ->
            uhc.addToAppropriateGroup(player, groups)
        }
        uhc.events.register<MinigameSetPlayingEvent> { (_, player) ->
            uhc.addToAppropriateGroup(player, groups)
        }
        uhc.events.register<PlayerVoicechatConnectedEvent> { (player) ->
            uhc.addToAppropriateGroup(player, groups)
        }
        uhc.events.register<PlayerTeamJoinEvent>(phase = PlayerTeamJoinEvent.PHASE_POST) { (player) ->
            uhc.addToAppropriateGroup(player, groups)
        }
        uhc.events.register<PlayerTeamLeaveEvent> { (player) ->
            player.setVoicechatGroup(null)
        }
        uhc.events.register<MinigameRemovePlayerEvent> { (_, player) ->
            player.setVoicechatGroup(null)
        }
        uhc.events.register<MinigameCloseEvent>(priority = -10) {
            uhc.players.forEach { it.setVoicechatGroup(null) }
            groups.values.forEach { api.removeGroup(it.id) }
        }

        uhc.commands.register(this.createCommandTree(groups))
    }

    private fun onPlayerConnectedEvent(event: PlayerConnectedEvent) {
        val player = event.connection.player.player as? ServerPlayer ?: return
        GlobalEventHandler.Server.broadcast(PlayerVoicechatConnectedEvent(player, event.connection))
    }

    private fun UHCMinigame.addToAppropriateGroup(player: ServerPlayer, groups: HashMap<Team, Group>) {
        val team = when {
            this.players.isSpectating(player) -> this.teams.getSpectatorTeam()
            else -> player.team ?: return
        }
        val group = this.getVoiceGroupOfTeam(team, groups)
        player.setVoicechatGroup(group)
    }

    private fun UHCMinigame.getVoiceGroupOfTeam(team: Team, groups: HashMap<Team, Group>): Group {
        return when {
            this.teams.isSpectatorTeam(team) || this.teams.isAdminTeam(team) -> groups.computeIfAbsent(team) {
                createDefaultGroupWithType(Group.Type.NORMAL, team.name)
            }
            else -> groups.computeIfAbsent(team) {
                createDefaultGroupWithType(Group.Type.ISOLATED, team.name)
            }
        }
    }

    private fun ServerPlayer.setVoicechatGroup(group: Group?) {
        api.getConnectionOf(this.uuid)?.group = group
    }

    private fun createDefaultGroupWithType(type: Group.Type, name: String): Group {
        val password = UUID.randomUUID().toString()
        return this.api.groupBuilder()
            .setName(name)
            .setType(type)
            .setPersistent(true)
            .setHidden(true)
            .setPassword(password)
            .build()
    }

    private fun createCommandTree(groups: HashMap<Team, Group>): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("uhc") {
            literal("voicechat") {
                requiresAdminOrPermission()
                literal("join") {
                    argument("group", StringArgumentType.word()) {
                        suggests { _ -> groups.values.map { it.name } }
                        argument("player", EntityArgument.player()) {
                            executes { joinVoiceGroup(it, groups) }
                        }
                        executes { joinVoiceGroup(it, groups, it.source.playerOrException) }
                    }
                }
            }
        }
    }

    private fun joinVoiceGroup(
        context: CommandContext<CommandSourceStack>,
        groups: HashMap<Team, Group>,
        player: ServerPlayer = EntityArgument.getPlayer(context, "player"),
    ): Int {
        val groupName = StringArgumentType.getString(context, "group")
        val group = groups.values.firstOrNull { it.name.equals(groupName) }
            ?: return context.source.fail("Could not find a group named ${groupName}!")

        player.setVoicechatGroup(group)
        return context.source.success("Successfully added ${player.scoreboardName} to $groupName")
    }

    private data class PlayerVoicechatConnectedEvent(
        override val player: ServerPlayer,
        val connection: VoicechatConnection
    ): PlayerEvent
}
