package net.casual.championships.uhc.utils

import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.championships.common.minigame.rules.Rules
import net.casual.championships.common.minigame.rules.RulesProvider
import net.casual.championships.common.util.RuleUtils
import net.casual.championships.common.util.RuleUtils.addRule
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object UHCRules: RulesProvider {
    override fun getRules(): Rules {
        return Rules.build {
            addRule("uhc.rules.announcement", 1)
            addRule("uhc.rules.mods", 3)
            addRule("uhc.rules.exploits", 2)
            addRule("uhc.rules.pvp", 3, 6)
            addRule("uhc.rules.gameplay", 3)
            addRule("uhc.rules.glowing", 1)
            addRule("uhc.rules.heads", 2)
            addRule("uhc.rules.chat", 2)
            rule {
                title = RuleUtils.formatTitle(Component.translatable("uhc.rules.spectators"))
                val rules = getSpectatorRules()
                entry {
                    line(rules[0])
                    line(rules[1])
                    line(Component.empty())
                }
            }
            addRule("uhc.rules.gentleman", 1)
            rule {
                title = RuleUtils.formatTitle(Component.translatable("uhc.rules.reminders"))
                entry {
                    val teamglow = Component.literal("/uhc teamglow").mini().bold().color(0x65b7db)
                    val fullbright = Component.literal("/uhc fullbright").mini().bold().color(0x65b7db)
                    val pos = Component.literal("/uhc pos").mini().bold().color(0x65b7db)
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.1", teamglow)))
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.2", fullbright)))
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.3", pos)))
                }
                entry {
                    val prefix = Component.literal("!").mini().bold().color(0x65b7db)
                    val chat = Component.literal("/chat").mini().bold().color(0x65b7db)
                    line(RuleUtils.formatLine(Component.translatable("uhc.rules.reminders.4", prefix, chat)))
                }
            }
            addRule("uhc.rules.questions", 1)
            addRule("uhc.rules.finally", 1)
        }
    }

    fun getSpectatorRules(): List<MutableComponent> {
        val s = Component.literal("/s").mini().bold().color(0x65b7db)
        val sneak = Component.keybind("key.sneak").mini().bold().color(0x65b7db)
        return listOf(
            RuleUtils.formatLine(Component.translatable("uhc.rules.spectators.1")),
            RuleUtils.formatLine(Component.translatable("uhc.rules.spectators.2", s)),
            RuleUtils.formatLine(Component.translatable("uhc.rules.spectators.3", sneak))
        )
    }
}