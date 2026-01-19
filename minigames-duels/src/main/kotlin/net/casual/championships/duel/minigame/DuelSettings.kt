package net.casual.championships.duel.minigame

import net.casual.arcade.minigame.settings.display.DisplayableSettings
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.bool
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.enumeration
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.float64
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.string
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.arcade.utils.convertCasing
import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.SnakeCase
import net.casual.championships.common.items.CasualGuiItems.ARENA
import net.casual.championships.common.items.CasualGuiItems.FLAG
import net.casual.championships.common.items.CasualGuiItems.GLOWING
import net.casual.championships.common.items.CasualGuiItems.GREEN_DIAGONAL
import net.casual.championships.common.items.CasualGuiItems.HEALTH_BOOST
import net.casual.championships.common.items.CasualGuiItems.LARGE
import net.casual.championships.common.items.CasualGuiItems.LARGE_SELECTED
import net.casual.championships.common.items.CasualGuiItems.MEDIUM
import net.casual.championships.common.items.CasualGuiItems.MEDIUM_SELECTED
import net.casual.championships.common.items.CasualGuiItems.NATURAL_REGEN
import net.casual.championships.common.items.CasualGuiItems.ONE_TIMES
import net.casual.championships.common.items.CasualGuiItems.ONE_TIMES_SELECTED
import net.casual.championships.common.items.CasualGuiItems.SMALL
import net.casual.championships.common.items.CasualGuiItems.SMALL_SELECTED
import net.casual.championships.common.items.CasualGuiItems.THREE_TIMES
import net.casual.championships.common.items.CasualGuiItems.THREE_TIMES_SELECTED
import net.casual.championships.common.items.CasualGuiItems.TWO_TIMES
import net.casual.championships.common.items.CasualGuiItems.TWO_TIMES_SELECTED
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.duel.arena.DuelArenaSize
import net.casual.championships.duel.arena.DuelArenaSize.*
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.item.component.DyedItemColor
import kotlin.enums.enumEntries

class DuelSettings(
    private val arenas: Collection<DuelArenasDataModule.ResolvedArenas>
): DisplayableSettings(CasualSettings.Defaults(Component.translatable("casual.gui.duel.settings").withMiniFont())) {
    val displayableTeams = bool {
        name = "teams"
        val flag = FLAG.named(Component.translatable("casual.gui.duel.settings.teams").withMiniFont())
        flag.set(DataComponents.DYED_COLOR, DyedItemColor(0xFF0000))
        flag.hideTooltip(DataComponents.DYED_COLOR)
        display = flag
        value = false
        defaults.options(this)
    }
    var teams by this.register(this.displayableTeams)

    val displayableHealth = float64 {
        name = "health"
        display = HEALTH_BOOST.named(Component.translatable("casual.gui.duel.settings.health").withMiniFont())
            .potion(Potions.HEALING)
            .hideTooltip(DataComponents.POTION_CONTENTS)
        value = 1.0
        option("normal", ONE_TIMES.named("Normal"), 0.0) { setting, _, _ ->
            (if (setting.get() == 0.0) ONE_TIMES_SELECTED else ONE_TIMES).named("Normal")
        }
        option("double", ONE_TIMES.named("Double"), 1.0) { setting, _, _ ->
            (if (setting.get() == 1.0) TWO_TIMES_SELECTED else TWO_TIMES).named("Double")
        }
        option("triple", ONE_TIMES.named("Triple"), 2.0) { setting, _, _ ->
            (if (setting.get() == 2.0) THREE_TIMES_SELECTED else THREE_TIMES).named("Triple")
        }
    }
    var health by this.register(this.displayableHealth)

    val displayableNaturalRegen = bool {
        name = "natural_regeneration"
        display = NATURAL_REGEN.named(Component.translatable("casual.gui.duel.settings.naturalRegeneration").withMiniFont())
        value = false
        defaults.options(this)
    }
    var naturalRegen by this.register(this.displayableNaturalRegen)

    val displayableGlowing = bool {
        name = "glowing"
        display = GLOWING.named(Component.translatable("casual.gui.duel.settings.glowing").withMiniFont())
        value = false
        defaults.options(this)
    }
    var glowing by this.register(this.displayableGlowing)

    val displayablePlayerDropHeads = bool {
        name = "player_drops_head"
        display = Items.PLAYER_HEAD.named(Component.translatable("casual.gui.duel.settings.playerHeadDrops").withMiniFont())
        value = true
        defaults.options(this)
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
        option("small", ONE_TIMES.named("Small"), Small) { setting, _, _ ->
            (if (setting.get() == Small) SMALL_SELECTED else SMALL).named("Small")
        }
        option("medium", ONE_TIMES.named("Medium"), Medium) { setting, _, _ ->
            (if (setting.get() == Medium) MEDIUM_SELECTED else MEDIUM).named("Medium")
        }
        option("large", ONE_TIMES.named("Large"), Large) { setting, _, _ ->
            (if (setting.get() == Large) LARGE_SELECTED else LARGE).named("Large")
        }
    }
    var arenaSize by this.register(this.displayableArenaSize)

    val displayableKit = enumeration<DuelKit> {
        name = "duel_kit"
        display = Items.IRON_SWORD.named(Component.translatable("casual.gui.duel.settings.kit").withMiniFont())
        value = enumEntries<DuelKit>().random()
        run {
            val entry = DuelKit.RandomGear
            val name = entry.name.convertCasing(PascalCase, SnakeCase)
            val fancyName = entry.getFancyName()
            option(name, Items.IRON_HELMET.named(fancyName), entry)
        }
        run {
            val entry = DuelKit.MaceCharged
            val name = entry.name.convertCasing(PascalCase, SnakeCase)
            val fancyName = entry.getFancyName()
            option(name, Items.MACE.named(fancyName), entry)
        }
    }
    var kit by this.register(this.displayableKit)

    fun getSelectedArena(): DuelArenasDataModule.DuelArena {
        val arena = this.arenas.first { it.name == this.arena }
        return arena.arenas[this.arenaSize]!!
    }
}