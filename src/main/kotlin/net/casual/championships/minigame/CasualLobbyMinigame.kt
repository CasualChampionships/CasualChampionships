package net.casual.championships.minigame

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameAddSpectatorEvent
import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.championships.extensions.TeamFlag
import net.casual.championships.extensions.TeamFlagsExtension.Companion.flags
import net.casual.championships.managers.TeamManager.getOrCreateSpectatorTeam
import net.casual.championships.minigame.uhc.gui.LobbyBossBar
import net.casual.championships.minigame.uhc.resources.UHCResources
import net.casual.championships.util.Config
import net.casual.championships.util.CasualUtils
import net.casual.championships.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

class CasualLobbyMinigame(
    server: MinecraftServer,
    override val lobby: Lobby
): LobbyMinigame(server) {
    override val id = CasualUtils.id("casual_lobby")

    private val bossbar = LobbyBossBar().apply { then(::completeBossBar) }

    init {
        this.initialize()
    }

    override fun getResources(): MinigameResources {
        return UHCResources
    }

    override fun initialize() {
        super.initialize()
        this.ui.setTabDisplay(CasualMinigames.createTabDisplay())
    }

    override fun onStart() {
        this.ui.addBossbar(this.bossbar)
        for (player in this.getNonAdminPlayers()) {
            player.setGameMode(GameType.ADVENTURE)
        }

        for (team in this.getPlayerTeams()) {
            team.collisionRule = Team.CollisionRule.NEVER
        }
    }

    override fun onStartCountdown() {
        this.ui.removeBossbar(this.bossbar)

        for (team in this.getPlayerTeams()) {
            team.collisionRule = Team.CollisionRule.ALWAYS
        }
    }

    override fun moveToNextMinigame() {
        CasualMinigames.setNewMinigameAndStart(this.getNextMinigame()!!)
        
        this.close()
    }

    override fun getTeamsToReady(): Collection<PlayerTeam> {
        return this.getPlayerTeams().filter { !it.flags.has(TeamFlag.Ignored) }
    }

    override fun createLobbyCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return super.createLobbyCommand().then(
            Commands.literal("start").then(
                Commands.literal("in").then(
                    Commands.argument("time", IntegerArgumentType.integer(1)).then(
                        Commands.argument("unit", EnumArgument.enumeration(MinecraftTimeUnit::class.java)).executes(this::setTime)
                    )
                )
            )
        )
    }

    private fun setTime(context: CommandContext<CommandSourceStack>): Int {
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration(context, "unit", MinecraftTimeUnit::class.java)
        val duration = unit.duration(time)
        this.bossbar.setDuration(duration)
        return context.source.success("Countdown will begin in $time ${unit.name}")
    }

    private fun completeBossBar() {
        val message = "Lobby waiting period has finished. ".literal()
        val teams = "[Click to ready teams]".literal().lime().command("/lobby ready teams")
        val players = "[Click to ready players]".literal().lime().command("/lobby ready players")
        val component = message.append(teams).append(" or ").append(players)

        for (player in this.getAdminPlayers()) {
            player.sendSystemMessage(component)
        }
    }

    @Listener
    private fun onPlayerAdded(event: MinigameAddPlayerEvent) {
        val player = event.player

        player.sendSystemMessage(Texts.LOBBY_WELCOME.append(" Casual Championships").gold())
        if (!this.isAdmin(player)) {
            player.setGameMode(GameType.ADVENTURE)
        } else if (Config.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        }

        val team = player.team
        if (team == null || team.flags.has(TeamFlag.Ignored)) {
            this.makeSpectator(player)
        }
    }

    @Listener
    private fun onMakeSpectator(event: MinigameAddSpectatorEvent) {
        val player = event.player
        if (player.team == null) {
            val scoreboard = this.server.scoreboard
            scoreboard.addPlayerToTeam(event.player.scoreboardName, scoreboard.getOrCreateSpectatorTeam())
        }
    }
}