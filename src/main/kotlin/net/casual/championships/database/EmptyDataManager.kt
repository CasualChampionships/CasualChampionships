package net.casual.championships.database

import net.casual.championships.duel.DuelMinigame
import net.casual.championships.uhc.UHCMinigame
import net.minecraft.server.MinecraftServer

class EmptyDataManager: CasualDataManager {
    override fun createTeams(server: MinecraftServer) {

    }

    override fun syncUHCData(uhc: UHCMinigame) {

    }

    override fun syncDuelData(duel: DuelMinigame) {

    }

    override fun close() {

    }
}