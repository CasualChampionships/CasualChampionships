package net.casual.championships.duel

import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.settings.display.MenuGameSettingBuilder.Companion.bool
import net.casual.arcade.settings.display.MenuGameSettingBuilder.Companion.enumeration
import net.casual.arcade.settings.display.MenuGameSettingBuilder.Companion.float64
import net.casual.arcade.settings.display.MenuGameSettingBuilder.Companion.string
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ItemUtils.hideAttributeTooltips
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.potion
import net.casual.championships.common.arena.ArenaTemplate
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.items.MenuItem.Companion.CROSS
import net.casual.championships.common.items.MenuItem.Companion.GREY_CROSS
import net.casual.championships.common.items.MenuItem.Companion.GREY_TICK
import net.casual.championships.common.items.MenuItem.Companion.LARGE
import net.casual.championships.common.items.MenuItem.Companion.LARGE_SELECTED
import net.casual.championships.common.items.MenuItem.Companion.MEDIUM
import net.casual.championships.common.items.MenuItem.Companion.MEDIUM_SELECTED
import net.casual.championships.common.items.MenuItem.Companion.ONE_TIMES
import net.casual.championships.common.items.MenuItem.Companion.ONE_TIMES_SELECTED
import net.casual.championships.common.items.MenuItem.Companion.SMALL
import net.casual.championships.common.items.MenuItem.Companion.SMALL_SELECTED
import net.casual.championships.common.items.MenuItem.Companion.THREE_TIMES
import net.casual.championships.common.items.MenuItem.Companion.THREE_TIMES_SELECTED
import net.casual.championships.common.items.MenuItem.Companion.TICK
import net.casual.championships.common.items.MenuItem.Companion.TWO_TIMES
import net.casual.championships.common.items.MenuItem.Companion.TWO_TIMES_SELECTED
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.common.util.CommonComponents.DISABLE
import net.casual.championships.common.util.CommonComponents.DISABLED
import net.casual.championships.common.util.CommonComponents.ENABLE
import net.casual.championships.common.util.CommonComponents.ENABLED
import net.casual.championships.duel.arena.ArenaSize
import net.casual.championships.duel.arena.ArenaSize.*
import net.casual.championships.duel.arena.DuelArenasTemplate
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import kotlin.enums.enumEntries

class DuelSettings(
    private val arenas: List<DuelArenasTemplate>
): DisplayableSettings(CasualSettings.Defaults(Component.translatable("casual.gui.duel.settings").mini())) {
    val displayableTeams = bool {
        name = "teams"
        display = Items.GREEN_BANNER.named(Component.translatable("casual.gui.duel.settings.teams").mini())
        value = false
        defaults.options(this)
    }
    var teams by this.register(this.displayableTeams)

    val displayableHealth = float64 {
        name = "health"
        display = Items.POTION.named(Component.translatable("casual.gui.duel.settings.health").mini())
            .potion(Potions.HEALING)
            .hideAttributeTooltips()
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
        display = Items.GOLDEN_APPLE.named(Component.translatable("casual.gui.duel.settings.naturalRegeneration").mini())
        value = false
        defaults.options(this)
    }
    var naturalRegen by this.register(this.displayableNaturalRegen)

    val displayableGlowing = bool {
        name = "glowing"
        display = Items.SPECTRAL_ARROW.named(Component.translatable("casual.gui.duel.settings.glowing").mini())
        value = false
        defaults.options(this)
    }
    var glowing by this.register(this.displayableGlowing)

    val displayablePlayerDropHeads = bool {
        name = "player_drops_head"
        display = Items.PLAYER_HEAD.named(Component.translatable("casual.gui.duel.settings.playerHeadDrops").mini())
        value = true
        defaults.options(this)
    }
    var playerDropsHead by this.register(this.displayablePlayerDropHeads)

    val displayableArena = string {
        name = "arena"
        display = MenuItem.ARENA.named(Component.translatable("casual.gui.duel.settings.arena").mini())
        value = arenas.randomOrNull()?.name ?: ""
        for (arena in arenas) {
            option(arena.name, arena.display, arena.name)
        }
    }
    var arena by this.register(this.displayableArena)

    val displayableArenaSize = enumeration<ArenaSize> {
        name = "arena_size"
        display = MenuItem.GREEN_DIAGONAL.named(Component.translatable("casual.gui.duel.settings.arenaSize").mini())
        value = enumEntries<ArenaSize>().random()
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

    fun getArenaTemplate(): ArenaTemplate {
        val arena = this.arenas.find { it.name == this.arena } ?: return ArenaTemplate.DEFAULT
        return arena.getArenaTemplateFor(this.arenaSize)
    }
}