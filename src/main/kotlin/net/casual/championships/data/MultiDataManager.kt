package net.casual.championships.data

import com.mojang.authlib.GameProfile
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.uhc.UHCMinigame
import net.minecraft.server.MinecraftServer

class MultiDataManager(
    private val managers: List<DataManager>
): DataManager {
    override fun getParticipants(): Set<GameProfile> {
        val profiles = HashSet<GameProfile>()
        for (manager in this.managers) {
            profiles.addAll(manager.getParticipants())
        }
        return profiles
    }

    override fun createTeams(server: MinecraftServer) {
        for (manager in this.managers) {
            manager.createTeams(server)
        }
    }

    override fun syncUHCData(uhc: UHCMinigame) {
        for (manager in this.managers) {
            manager.syncUHCData(uhc)
        }
    }

    override fun syncDuelData(duel: DuelMinigame) {
        for (manager in this.managers) {
            manager.syncDuelData(duel)
        }
    }

    override fun close() {
        for (manager in this.managers) {
            manager.close()
        }
    }

    companion object {
        fun of(vararg managers: DataManager): MultiDataManager {
            return MultiDataManager(managers.toList())
        }
    }
}