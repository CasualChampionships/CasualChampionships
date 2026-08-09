package net.casual.championships.common.ui.tab

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import kotlinx.coroutines.Deferred
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.resources.font.heads.PixelGridHeadComponents
import net.casual.arcade.resources.font.heads.getHeadFor
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.italicize
import net.casual.arcade.utils.coroutine.async
import net.casual.arcade.utils.coroutine.getNow
import net.casual.arcade.utils.scoreboard.color
import net.casual.arcade.utils.server.ServerSingleton
import net.casual.arcade.virtual.visuals.tab.PlayerListEntries
import net.casual.arcade.virtual.visuals.tab.TeamListEntries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.time.Duration

open class CasualPlayerListEntries(
    private val minigame: Minigame
): TeamListEntries() {
    override fun getTeams(server: MinecraftServer): Collection<PlayerTeam> {
        return this.minigame.teams.getAllNonSpectatorOrAdminTeams().sortedWith(NAME_ORDER)
    }

    override fun formatTeamName(server: MinecraftServer, team: PlayerTeam): MutableComponent {
        return super.formatTeamName(server, team).color(team).withMiniFont()
    }

    override fun createPlayerEntry(
        server: MinecraftServer,
        username: String,
        team: PlayerTeam,
        player: ServerPlayer?
    ): PlayerListEntries.Entry {
        val name = Component.literal(username)
        val head = if (player != null) {
            if (this.minigame.players.isPlaying(player)) {
                name.color(team)
                PixelGridHeadComponents.getHeadOrDefaultFor(player)
            } else {
                name.italicize().color(0x919191)
                GREYSCALE_CACHE.get(username).getNow(PixelGridHeadComponents.get(services = server.services()).getDefault())
            }
        } else {
            name.color(0x808080)
            GREYSCALE_CACHE.get(username).getNow(PixelGridHeadComponents.get(services = server.services()).getDefault())
        }
        return PlayerListEntries.Entry.fromComponent(
            Component.empty().append(head).append(SpacingFontResources.spaced(2)).append(name.withMiniFont())
        )
    }

    companion object {
        private val GREYSCALE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(60))
            .build(object: CacheLoader<String, Deferred<Component>>() {
                override fun load(key: String): Deferred<Component> {
                    val server = ServerSingleton.get()
                    return server.async {
                        PixelGridHeadComponents.get(services = server.services()).getHeadFor(key)
                    }
                }
            })
    }
}