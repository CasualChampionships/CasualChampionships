package net.casual.championships.common.ui

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.collect.Iterables
import net.casual.arcade.gui.tab.PlayerListEntries
import net.casual.arcade.gui.tab.PlayerListEntries.Entry
import net.casual.arcade.gui.tab.TeamListEntries
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.resources.font.heads.PlayerHeadComponents
import net.casual.arcade.resources.font.heads.PlayerHeadFont
import net.casual.arcade.resources.font.padding.PaddingNoSplitFontResources
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.greyscale
import net.casual.arcade.utils.ComponentUtils.italicise
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.PlayerUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.math.max

class CasualPlayerListEntries(
    private val minigame: Minigame<*>
): TeamListEntries() {
    override fun getTeams(server: MinecraftServer): Collection<PlayerTeam> {
        return this.minigame.teams.getAllNonSpectatorOrAdmin().sortedWith(NAME_ORDER)
    }

    override fun formatTeamName(server: MinecraftServer, team: PlayerTeam): MutableComponent {
        return super.formatTeamName(server, team).mini()
    }

    override fun formatPlayerName(server: MinecraftServer, username: String, team: PlayerTeam): MutableComponent {
        val playing = this.isPlayerPlaying(username)
        val name = Component.literal(username)
        val head = when (playing) {
            true -> {
                name.withStyle(team.color)
                PlayerHeadComponents.getHeadOrDefault(username)
            }
            false -> {
                name.italicise().colour(0x919191)
                GREYSCALE_CACHE.get(username).getNow(PlayerHeadFont.STEVE_HEAD)
            }
            else -> {
                name.colour(0x808080)
                GREYSCALE_CACHE.get(username).getNow(PlayerHeadFont.STEVE_HEAD)
            }
        }
        return Component.empty().append(head).append(ComponentUtils.space(2)).append(name.mini())
    }

    private fun isPlayerPlaying(username: String): Boolean? {
        val player = PlayerUtils.player(username)
        if (player != null) {
            return this.minigame.isPlaying(player)
        }
        return null
    }

    companion object {
        private val GREYSCALE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(60))
            .build(object: CacheLoader<String, CompletableFuture<Component>>() {
                override fun load(key: String): CompletableFuture<Component> {
                    return PlayerHeadComponents.getHead(key).thenApply { it.greyscale() }
                }
            })
    }
}