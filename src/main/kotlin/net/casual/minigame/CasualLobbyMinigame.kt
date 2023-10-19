package net.casual.minigame

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.command
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.minigame.uhc.gui.LobbyBossBar
import net.casual.util.Config
import net.casual.util.CasualUtils
import net.casual.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.Team

class CasualLobbyMinigame(
    server: MinecraftServer,
    override val lobby: Lobby
): LobbyMinigame(server) {
    override val id = CasualUtils.id("casual_lobby")

    private val bossbar = LobbyBossBar().apply { then(::completeBossBar) }

    override fun initialise() {
        super.initialise()
        this.events.register<MinigameAddPlayerEvent> { this.onPlayerAdded(it) }
    }

    override fun onStart() {
        this.addBossbar(this.bossbar)
        for (player in this.getPlayers()) {
            if (!player.hasPermissions(4)) {
                player.setGameMode(GameType.ADVENTURE)
            }
        }

        for (team in this.getPlayerTeams()) {
            team.collisionRule = Team.CollisionRule.NEVER
        }
    }

    override fun onStartCountdown() {
        this.removeBossbar(this.bossbar)

        for (team in this.getPlayerTeams()) {
            team.collisionRule = Team.CollisionRule.ALWAYS
        }
    }

    override fun moveToNextMinigame() {
        CasualMinigame.setNewMinigameAndStart(this.getNextMinigame()!!)
    }

    override fun createLobbyCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return super.createLobbyCommand().requires { it.hasPermission(4) }.then(
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
        val command = "[Click to start countdown]".literal().lime().command("/lobby countdown")
        val component = message.append(command)
        for (player in this.getPlayers()) {
            if (player.hasPermissions(4)) {
                player.sendSystemMessage(component)
            }
        }
    }

    private fun onPlayerAdded(event: MinigameAddPlayerEvent) {
        val player = event.player
        player.sendSystemMessage(Texts.LOBBY_WELCOME.append(" Casual Championships").gold())
        if (!player.hasPermissions(2)) {
            player.setGameMode(GameType.ADVENTURE)
        } else if (Config.dev) {
            player.sendSystemMessage(Component.literal("Minigames are in dev mode!").red())
        }

        val scoreboard = this.server.scoreboard
        if (player.team == null) {
            val spectator = scoreboard.getPlayerTeam("Spectator")
            if (spectator != null) {
                scoreboard.addPlayerToTeam(player.scoreboardName, spectator)
            }
        }
    }
}