package net.casual.championships.duel

import net.casual.arcade.area.templates.PlaceableAreaTemplate
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
import net.casual.championships.common.minigame.CasualSettings
import net.casual.championships.duel.arena.ArenaSize
import net.casual.championships.duel.arena.DuelArenasTemplate
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import kotlin.enums.enumEntries

class DuelSettings(
    private val arenas: List<DuelArenasTemplate>
): DisplayableSettings(CasualSettings.Defaults(Component.translatable("casual.gui.duel.settings").mini())) {
    var teams by this.register(bool {
        name = "teams"
        display = Items.GREEN_BANNER.named("Teams")
        value = false
        defaults.options(this)
    })

    var health by this.register(float64 {
        name = "health"
        display = Items.POTION.named("Health").potion(Potions.HEALING).hideAttributeTooltips()
        value = 1.0
        option("normal", Items.RED_STAINED_GLASS_PANE.named("Normal"), 0.0)
        option("double", Items.YELLOW_STAINED_GLASS_PANE.named("Double"), 1.0)
        option("triple", Items.GREEN_STAINED_GLASS_PANE.named("Triple"), 2.0)
    })

    var naturalRegen by this.register(bool {
        name = "natural_regeneration"
        display = Items.GOLDEN_APPLE.named("Natural Regeneration")
        value = false
        defaults.options(this)
    })

    val glowing by this.register(bool {
        name = "glowing"
        display = Items.SPECTRAL_ARROW.named("Glowing")
        value = false
        defaults.options(this)
    })

    var playerDropsHead by this.register(bool {
        name = "player_drops_head"
        display = Items.PLAYER_HEAD.named("Player Drops Head")
        value = true
        defaults.options(this)
    })

    var arena by this.register(string {
        name = "arena"
        display = Items.DEEPSLATE_TILES.named("Arena")
        value = arenas.randomOrNull()?.name ?: ""
        for (arena in arenas) {
            option(arena.name, arena.display, arena.name)
        }
    })

    var arenaSize by this.register(enumeration<ArenaSize> {
        name = "arena_size"
        display = Items.POLISHED_DEEPSLATE_STAIRS.named("Arena Size")
        value = enumEntries<ArenaSize>().random()
        defaults.options(this, ArenaSize::class.java, ArenaSize::display)
    })

    fun getArenaTemplate(): ArenaTemplate {
        val arena = this.arenas.find { it.name == this.arena } ?: return ArenaTemplate.DEFAULT
        return arena.getArenaTemplateFor(this.arenaSize)
    }
}