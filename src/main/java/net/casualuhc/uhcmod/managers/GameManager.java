package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.GameManagerHelper;

public class GameManager {

    public static Phase currentPhase = Phase.NONE;

    public enum Phase {
        NONE(() -> { }),

        SETUP(() -> {
            UHCMod.UHCLogger.info("SETUP");
            GameManagerHelper.generateLobby();
            GameManagerHelper.setGamerules();
            TeamManager.createTeams();
        }),

        VOTING(() -> {
            UHCMod.UHCLogger.info("VOTE");
            // Just ignore for now I guess
        }),

        LOBBY(() -> {
            UHCMod.UHCLogger.info("LOBBY");
            // Whitelist Everyone
            // Open the server for people to join
            // Kman can add this :)
        }),

        STARTING(() -> {
            UHCMod.UHCLogger.info("START");
            GameManagerHelper.sendReadyMessage();
            // Coutdown to start
            // Spread out Teams
            // Switch state to Active
            // Kman can add this :)
        }),

        ACTIVE(() -> {
            UHCMod.UHCLogger.info("ACTIVE");
            // Set grace period to 10 mins
            // Do wb stuff
            // Handle Player deaths to drop golden apples when someone dies
            // Other stuff needed for games
            // Kman can add this :)
        }),

        END(() -> {
            UHCMod.UHCLogger.info("END");
            // I have no idea what to put here?
            // Announce team that one using sb titles
        });

        private final Runnable gameRunnable;

        Phase(Runnable runnable) {
            this.gameRunnable = runnable;
        }

        public void run() {
            GameManager.currentPhase = this;
            this.gameRunnable.run();
        }
    }
}

