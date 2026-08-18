package net.casual.championships.common.minigame

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.settings.MinigameSettings
import net.minecraft.network.chat.Component

open class CasualSettings(minigame: Minigame): MinigameSettings(minigame, Component.literal("Casual Minigame Settings"))