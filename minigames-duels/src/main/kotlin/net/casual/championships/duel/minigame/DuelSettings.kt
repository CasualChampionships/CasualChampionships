package net.casual.championships.duel.minigame

import net.casual.arcade.minigame.settings.display.DisplayableSettings
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.bool
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.enumeration
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.float64
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.string
import net.casual.arcade.minigame.utils.defaultOptions
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.championships.common.items.CasualGuiItems.ARENA
import net.casual.championships.common.items.CasualGuiItems.FLAG
import net.casual.championships.common.items.CasualGuiItems.GLOWING
import net.casual.championships.common.items.CasualGuiItems.GREEN_DIAGONAL
import net.casual.championships.common.items.CasualGuiItems.HEALTH_BOOST
import net.casual.championships.common.items.CasualGuiItems.NATURAL_REGEN
import net.casual.championships.duel.arena.DuelArenaSize
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.component.DyedItemColor
import kotlin.enums.enumEntries

class DuelSettings(
    private val arenas: Collection<DuelArenasDataModule.ResolvedArenas>
): DisplayableSettings(Component.translatable("casual.gui.duel.settings").withMiniFont()) {
    val displayableTeams = bool {
        name = "teams"
        val flag = FLAG.named(Component.translatable("casual.gui.duel.settings.teams").withMiniFont())
        flag.set(DataComponents.DYED_COLOR, DyedItemColor(0xFF0000))
        flag.hideTooltip(DataComponents.DYED_COLOR)
        display = flag
        value = false
        defaultOptions()
    }
    var teams by this.register(this.displayableTeams)

    val displayableHealth = float64 {
        name = "health"
        display = HEALTH_BOOST.named(Component.translatable("casual.gui.duel.settings.health").withMiniFont())
            .potion(Potions.HEALING)
            .hideTooltip(DataComponents.POTION_CONTENTS)
        value = 1.0
        option("normal", Component.literal("normal"), 0.0)
        option("double", Component.literal("double"), 1.0)
        option("triple", Component.literal("triple"), 2.0)
    }
    var health by this.register(this.displayableHealth)

    val displayableNaturalRegen = bool {
        name = "natural_regeneration"
        display = NATURAL_REGEN.named(Component.translatable("casual.gui.duel.settings.naturalRegeneration").withMiniFont())
        value = false
        defaultOptions()
    }
    var naturalRegen by this.register(this.displayableNaturalRegen)

    val displayableGlowing = bool {
        name = "glowing"
        display = GLOWING.named(Component.translatable("casual.gui.duel.settings.glowing").withMiniFont())
        value = false
        defaultOptions()
    }
    var glowing by this.register(this.displayableGlowing)

    val displayablePlayerDropHeads = bool {
        name = "player_drops_head"
        display = Items.PLAYER_HEAD.named(Component.translatable("casual.gui.duel.settings.playerHeadDrops").withMiniFont())
        value = true
        defaultOptions()
    }
    var playerDropsHead by this.register(this.displayablePlayerDropHeads)

    val displayableArena = string {
        name = "arena"
        display = ARENA.named(Component.translatable("casual.gui.duel.settings.arena").withMiniFont())
        value = arenas.randomOrNull()?.name ?: ""
        for (arena in arenas) {
            option(arena.name, arena.display, arena.name)
        }
    }
    var arena by this.register(this.displayableArena)

    val displayableArenaSize = enumeration<DuelArenaSize> {
        name = "arena_size"
        display = GREEN_DIAGONAL.named(Component.translatable("casual.gui.duel.settings.arenaSize").withMiniFont())
        value = enumEntries<DuelArenaSize>().random()
        defaultOptions(id = { size -> size.name.lowercase() })
    }
    var arenaSize by this.register(this.displayableArenaSize)

    fun getSelectedArena(): DuelArenasDataModule.DuelArena {
        val arena = this.arenas.first { it.name == this.arena }
        return arena.arenas[this.arenaSize]!!
    }
}