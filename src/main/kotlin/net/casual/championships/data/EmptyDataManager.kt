package net.casual.championships.data

import net.casual.championships.duel.DuelMinigame
import net.casual.championships.uhc.UHCMinigame
import net.minecraft.server.MinecraftServer

class EmptyDataManager: DataManager {
    override fun createTeams(server: MinecraftServer) {

    }

    override fun syncUHCData(uhc: UHCMinigame) {

    }

    override fun syncDuelData(duel: DuelMinigame) {

    }

    override fun close() {

    }
}