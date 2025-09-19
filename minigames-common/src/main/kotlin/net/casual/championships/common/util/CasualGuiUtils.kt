package net.casual.championships.common.util

import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigamePauseEvent
import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.minigame.ready.MinigamePlayerReadyHandler
import net.casual.arcade.minigame.ready.ReadyChecker
import net.casual.arcade.minigame.utils.MinigameUtils.broadcastChangesToAdmin
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.TeamUtils.getHexColor
import net.casual.arcade.utils.chat.ChatFormatter
import net.casual.arcade.utils.chat.PlayerChatFormatter
import net.casual.arcade.utils.chat.PlayerFormattedChat
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.component.isEmpty
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.nametag.PlayerNametag
import net.casual.arcade.visuals.predicate.EntityObserverPredicate
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate.Companion.toPlayer
import net.casual.arcade.visuals.screen.PlayerInventoryViewGui
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.ui.CasualCountdown
import net.casual.championships.common.ui.CasualPlayerInventoryViewGui
import net.casual.championships.common.ui.CasualTeamReadyHandler
import net.casual.championships.common.ui.elements.*
import net.casual.championships.common.ui.game.TeamSelectorGui
import net.casual.championships.common.ui.tab.CasualPlayerListEntries
import net.casual.championships.common.ui.tab.SimpleCasualPlayerListEntries
import net.casual.championships.common.util.CasualComponents.Text.CASUAL
import net.casual.championships.common.util.CasualComponents.Text.CHAMPIONSHIPS
import net.casual.championships.common.util.CasualComponents.Text.KIWITECH
import net.casual.championships.common.util.CasualComponents.Text.SERVER_HOSTED_BY
import net.minecraft.ChatFormatting.*
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.DyedItemColor
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

    fun createPlayingNameTag(
        predicate: PlayerObserverPredicate = EntityObserverPredicate.visibleObservee().toPlayer()
    ): PlayerNametag {
        return PlayerNametag({ it.displayName!! }, predicate)
    }

    fun createPlayingHealthTag(
        predicate: PlayerObserverPredicate = CasualPredicates.VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES
    ): PlayerNametag {
        return PlayerNametag(
            { Component.literal(String.format("%.1f ", it.health / 2)).append(CasualComponents.Hud.HARDCORE_HEART) },
            predicate
        )
    }

    fun getBorderSidebarElements(buffer: Component): Array<PlayerSpecificElement<SidebarComponent>> {
        return arrayOf(
            BorderStatusElement(buffer).cached(),
            BorderDistanceElement(buffer),
            BorderSizeElement(buffer).cached()
        )
    }

    fun createTeamMinigameTabDisplay(minigame: Minigame): PlayerListDisplay {
        val display = PlayerListDisplay(CasualPlayerListEntries(minigame))
        addCasualFooterAndHeader(minigame, display)
        return display
    }

    fun createSimpleTabDisplay(minigame: Minigame): PlayerListDisplay {
        val display = PlayerListDisplay(SimpleCasualPlayerListEntries(minigame))
        addCasualFooterAndHeader(minigame, display)
        return display
    }

    fun addCasualFooterAndHeader(minigame: Minigame, display: PlayerListDisplay) {
        val hostedByKiwiTech = Component.empty()
            .append(SERVER_HOSTED_BY)
            .append(SpacingFontResources.spaced(4))
            .append(KIWITECH)

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
        val footer = spectatorAndAdmins.merge<_, Component>(baseFooter) { a, b ->
            a.map { Component.empty().append(it).append("\n\n") }.orElse(Component.empty()).append(b)
        }
        display.setDisplay(
            ComponentElements.of(Component.literal("\n").append(CASUAL).append(" ").append(CHAMPIONSHIPS).append("\n")),
            footer
        )
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
        minigame.ui.setPlayerListDisplay(this.createTeamMinigameTabDisplay(minigame))
        minigame.ui.readier = ReadyChecker(
            MinigamePlayerReadyHandler(minigame),
            CasualTeamReadyHandler(minigame)
        )
        minigame.ui.countdown = CasualCountdown

        minigame.ui.addNametag(this.createPlayingNameTag { observee, observer ->
            !observee.isInvisible && !minigame.effects.isInvisibleFor(observee, observer)
        })
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
}