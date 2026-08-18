package net.casual.championships.uhc.utils

import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.join
import net.casual.championships.common.minigame.rules.MinigameRuleEntry
import net.casual.championships.common.minigame.rules.MinigameRules
import net.casual.championships.common.minigame.rules.MinigameRulesProvider
import net.casual.championships.common.util.addRule
import net.casual.championships.common.util.formatLine
import net.casual.championships.common.util.formatTitle
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object UHCMinigameRules: MinigameRulesProvider {
    override fun getRules(): MinigameRules {
        return MinigameRules.build {
            addRule("uhc.rules.announcement", 1)
            addRule("uhc.rules.mods", 3)
            addRule("uhc.rules.exploits", 2)
            addRule("uhc.rules.pvp", 3, 6)
            addRule("uhc.rules.gameplay", 3)
            addRule("uhc.rules.glowing", 1)
            addRule("uhc.rules.heads", 2)
            addRule("uhc.rules.chat", 2)
            rule {
                title = formatTitle(Component.translatable("uhc.rules.spectators"))
                val rules = getSpectatorRules()
                entry {
                    line(rules.lines[0])
                    line(rules.lines[1])
                    line(Component.empty())
                }
            }
            addRule("uhc.rules.gentleman", 1)
            rule {
                title = formatTitle(Component.translatable("uhc.rules.reminders"))
                entry {
                    val teamglow = Component.literal("/uhc teamglow").withMiniFont().bold().color(0x65b7db)
                    val fullbright = Component.literal("/uhc fullbright").withMiniFont().bold().color(0x65b7db)
                    val pos = Component.literal("/uhc pos").withMiniFont().bold().color(0x65b7db)
                    line(formatLine(Component.translatable("uhc.rules.reminders.1", teamglow)))
                    line(formatLine(Component.translatable("uhc.rules.reminders.2", fullbright)))
                    line(formatLine(Component.translatable("uhc.rules.reminders.3", pos)))
                }
                entry {
                    val prefix = Component.literal("!").withMiniFont().bold().color(0x65b7db)
                    val chat = Component.literal("/chat").withMiniFont().bold().color(0x65b7db)
                    line(formatLine(Component.translatable("uhc.rules.reminders.4", prefix, chat)))
                }
            }
            addRule("uhc.rules.questions", 1)
            addRule("uhc.rules.finally", 1)
        }
    }

    fun getFormattedSpectatorRules(): MutableComponent {
        val rules = this.getSpectatorRules()
        return rules.lines.join(Component.literal("\n\n"))
    }

    private fun getSpectatorRules(): MinigameRuleEntry {
        val s = Component.literal("/s").withMiniFont().bold().color(0x65b7db)
        val sneak = Component.keybind("key.sneak").withMiniFont().bold().color(0x65b7db)
        val builder = MinigameRuleEntry.Builder()
        builder.line(builder.formatLine(Component.translatable("uhc.rules.spectators.1")))
        builder.line(builder.formatLine(Component.translatable("uhc.rules.spectators.2", s)))
        builder.line(builder.formatLine(Component.translatable("uhc.rules.spectators.3", sneak)))
        return builder.build()
    }
}