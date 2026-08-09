package net.casual.championships.common.util

import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.guis.presets.PlayerInventoryViewGui
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigamePauseEvent
import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.minigame.utils.MinigameUtils.broadcastChangesToAdmin
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.chat.PlayerChatFormatter
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.component.isEmpty
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.player.sendSound
import net.casual.arcade.utils.scoreboard.getHexColor
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.ready.chat.TeamChatReadyBroadcaster
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.tab.DynamicVirtualPlayerList
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.ui.CasualCountdown
import net.casual.championships.common.ui.CasualPlayerInventoryViewGui
import net.casual.championships.common.ui.elements.*
import net.casual.championships.common.ui.game.TeamSelectorGui
import net.casual.championships.common.ui.tab.CasualPlayerListEntries
import net.casual.championships.common.ui.tab.SimpleCasualPlayerListEntries
import net.casual.championships.common.util.CasualComponents.Text.CASUAL
import net.casual.championships.common.util.CasualComponents.Text.CHAMPIONSHIPS
import net.minecraft.ChatFormatting.*
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.component.DyedItemColor
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

object CasualGuiUtils {
    val INFO_ANNOUNCEMENT = ChatFormatter.createAnnouncement(Component.literal("[Info]").gold().bold().withMiniFont())
    val GAME_ANNOUNCEMENT = ChatFormatter.createAnnouncement(Component.literal("[Game]").lime().bold().withMiniFont())
    val READY_ANNOUNCEMENT = ChatFormatter.createAnnouncement(Component.literal("[Ready]").lime().bold().withMiniFont())

    fun MinigameChatManager.broadcastInfo(
        component: Component,
        players: Iterable<ServerPlayer> = this.getAllPlayers(),
        sound: Sound = Sound(CasualSounds.GLOBAL_SERVER_NOTIFICATION_LOW)
    ) {
        this.broadcastWithSound(component, sound, players, INFO_ANNOUNCEMENT)
    }

    fun MinigameChatManager.broadcastGame(
        component: Component,
        players: Iterable<ServerPlayer> = this.getAllPlayers(),
        sound: Sound = Sound(CasualSounds.GLOBAL_SERVER_NOTIFICATION)
    ) {
        this.broadcastWithSound(component, sound, players, GAME_ANNOUNCEMENT)
    }

    fun MinigameChatManager.broadcastWithSound(
        component: Component,
        sound: Sound = Sound(CasualSounds.GLOBAL_SERVER_NOTIFICATION),
        players: Iterable<ServerPlayer> = this.getAllPlayers(),
        formatter: ChatFormatter? = this.systemChatFormatter
    ) {
        this.broadcastTo(component, players, formatter)
        for (player in players) {
            player.sendSound(sound)
        }
    }

    fun createNametag(minigame: Minigame): Nametag {
        return object: Nametag {
            override fun getComponent(observee: Entity): Component {
                return observee.displayName
            }

            override fun isObservable(observee: Entity, observer: Observer): Boolean {
                check(observee is ServerPlayer)
                if (observee.isInvisible) {
                    return false
                }
                val player = observer.asPlayerOrNull() ?: return true
                return !minigame.effects.isInvisibleFor(observee, player)
            }
        }
    }

    fun createPlayingHealthTag(minigame: Minigame): Nametag {
        return object: Nametag {
            override fun getComponent(observee: Entity): Component {
                check(observee is ServerPlayer)
                return Component.literal(String.format("%.1f ", observee.health / 2)).append(CasualComponents.Hud.HARDCORE_HEART)
            }

            override fun isObservable(observee: Entity, observer: Observer): Boolean {
                check(observee is ServerPlayer)
                if (observee.isInvisible) {
                    return false
                }
                if (minigame.players.isSpectating(observee)) {
                    return false
                }
                val player = observer.asPlayerOrNull() ?: return true
                return player.isSpectator || (observee.team != null && observee.team == player.team)
            }
        }
    }

    fun getBorderSidebarElements(buffer: Component): Array<PlayerSpecificElement<SidebarComponent>> {
        return arrayOf(
            BorderStatusElement(buffer).cached(),
            BorderDistanceElement(buffer),
            BorderSizeElement(buffer).cached()
        )
    }

    fun createTeamMinigameTabDisplay(minigame: Minigame): DynamicVirtualPlayerList {
        val display = DynamicVirtualPlayerList(minigame.server, CasualPlayerListEntries(minigame))
        addCasualFooterAndHeader(minigame, display)
        return display
    }

    fun createSimpleTabDisplay(minigame: Minigame): DynamicVirtualPlayerList {
        val display = DynamicVirtualPlayerList(minigame.server, SimpleCasualPlayerListEntries(minigame))
        addCasualFooterAndHeader(minigame, display)
        return display
    }

    fun addCasualFooterAndHeader(minigame: Minigame, list: DynamicVirtualPlayerList) {
        val hostedByKiwiTech = Component.empty()
            .append(CasualComponents.Text.SERVER_HOSTED_BY)

        val baseFooter = PlayerSpecificElement { player ->
            val ping = player.connection.latency()
            val colour = when {
                ping < 80 -> DARK_GREEN
                ping < 120 -> GREEN
                ping < 180 -> YELLOW
                ping < 240 -> RED
                else -> DARK_RED
            }
            val formatted = Component.literal("$ping").withStyle(colour)
            Component.empty()
                .append(Component.translatable("casual.tab.ping", formatted).withMiniFont())
                .append("\n")
                .append(hostedByKiwiTech)
        }

        val spectatorAndAdmins = SpectatorAndAdminTeamsComponentElement(minigame).cached()
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val footer = spectatorAndAdmins.merge<_, Component>(baseFooter) { a, b ->
            a.map { Component.empty().append(it).append("\n\n") }.orElse(Component.empty()).append(b)
        }
        list.header.set(Component.literal("\n").append(CASUAL).append(" ").append(CHAMPIONSHIPS).append("\n"))
        list.setHeader(footer)
    }

    fun createTeamSelectionGui(minigame: Minigame, player: ServerPlayer): TeamSelectorGui {
        val selections = minigame.teams.getAllNonSpectatorOrAdminTeams().sortedBy { it.name }.map {
            val flag = CasualGuiItems.FLAG
            val color = it.getHexColor()
            if (color != null) {
                flag.set(DataComponents.DYED_COLOR, DyedItemColor(color))
                flag.hideTooltip(DataComponents.DYED_COLOR)
            }
            flag.set(DataComponents.CUSTOM_NAME, it.formattedDisplayName.withMiniFont())
            TeamSelectorGui.Selection(it, flag)
        }
        return TeamSelectorGui(player, selections)
    }

    fun createPlayerInventoryViewGui(observee: ServerPlayer, observer: ServerPlayer): PlayerInventoryViewGui {
        return CasualPlayerInventoryViewGui(observee, observer)
    }

    fun setMinigameUI(minigame: Minigame) {
        minigame.settings.broadcastChangesToAdmin()
        minigame.visuals.setPlayerListDisplay(this.createTeamMinigameTabDisplay(minigame))
        minigame.visuals.teamReadyBroadcaster = TeamChatReadyBroadcaster(
            this.createReadyUnicast(minigame), this.createReadyMulticast(minigame)
        )
        minigame.visuals.countdown = CasualCountdown

        minigame.visuals.addNametag(this.createNametag(minigame))
        minigame.events.register<MinigameAddPlayerEvent> {
            it.player.team?.nameTagVisibility = Team.Visibility.NEVER
        }
        minigame.events.register<PlayerTeamJoinEvent> {
            it.team.nameTagVisibility = Team.Visibility.NEVER
        }
        minigame.chat.systemChatFormatter = ChatFormatter {
            ChatFormatter.SYSTEM.format(Component.empty().append(it).withMiniFont())
        }
        minigame.chat.globalChatFormatter = object: PlayerChatFormatter {
            override fun format(player: ServerPlayer, message: PlayerFormattedChat): PlayerFormattedChat {
                val team = player.team
                val prefixed = when {
                    !message.prefix.isEmpty() || team == null -> message
                    else -> message.copy(prefix = team.formattedDisplayName)
                }
                return PlayerChatFormatter.Global.format(player, prefixed)
            }
        }

        minigame.events.register<MinigamePauseEvent> {
            minigame.chat.broadcastWithSound(
                Component.literal("Minigame is now paused"),
                Sound(CasualSounds.GAME_PAUSED)
            )
        }
    }

    private fun createReadyUnicast(minigame: Minigame): (PlayerTeam, Component) -> Unit {
        return { team, message ->
            minigame.chat.broadcastWithSound(message, players = team.getOnlinePlayers(), formatter = READY_ANNOUNCEMENT)
        }
    }

    private fun createReadyMulticast(minigame: Minigame): (Component) -> Unit {
        return { message -> minigame.chat.broadcast(message, formatter = READY_ANNOUNCEMENT) }
    }
}