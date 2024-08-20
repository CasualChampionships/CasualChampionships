package net.casual.championships.data

import com.mojang.authlib.GameProfile
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.uhc.UHCMinigame
import net.minecraft.server.MinecraftServer

interface DataManager {
    fun getParticipants(): Set<GameProfile>

    fun createTeams(server: MinecraftServer)

    fun syncUHCData(uhc: UHCMinigame)

    fun syncDuelData(duel: DuelMinigame)

    fun close()
}