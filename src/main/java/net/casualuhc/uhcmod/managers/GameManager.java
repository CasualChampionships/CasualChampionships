package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.GameManagerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;

public class GameManager {

    private static final ThreadGroup GAME_MANGER_THREAD_GROUP = new ThreadGroup("Game Manger Thread Group");

    public static Phase currentPhase = Phase.NONE;

    /**
     * These are the phases for the UHC, each has a runnable and a number
     * The runnables can be run from anywhere and the current Phase is tracked in GameManger
     *
     * @author Sensei
     */
    public enum Phase {
        NONE(() -> { }, 0),

        SETUP(() -> {
            GameManagerUtils.generateLobby();
            GameManagerUtils.setBeforeGamerules();
            TeamManager.createTeams();
        }, 1),

        LOBBY(() -> {
            // Whitelist Everyone
            // Open the server for people to join
            // Kman can add this :)
        }, 2),

        VOTING(() -> {
            // Just ignore for now I guess
        }, 3),

        READY(() -> {
            TeamUtils.unReadyAllTeams();
            TeamUtils.sendReadyMessage();
        }, 4),

        START(() -> {
            GameManagerUtils.startCountDown(GAME_MANGER_THREAD_GROUP);
        }, 5),

        ACTIVE(() -> {
            GameManagerUtils.startPVPCountdown(GAME_MANGER_THREAD_GROUP);
            GameManagerUtils.setUHCGamerules();
            // Do wb stuff
            // Other stuff needed for games
            // Kman can add this :)
        }, 6),

        END(() -> {
            UHCMod.UHCLogger.info("END");
            // I have no idea what to put here?
            // Announce team that one using sb titles
        }, 7);

        private final Runnable gameRunnable;
        private final int phaseNumber;

        Phase(Runnable runnable, int phaseNumber) {
            this.gameRunnable = runnable;
            this.phaseNumber = phaseNumber;
        }

        public void run() {
            GameManager.GAME_MANGER_THREAD_GROUP.interrupt();
            GameManager.currentPhase = this;
            this.gameRunnable.run();
        }

        public int getPhaseNumber() {
            return phaseNumber;
        }
    }
}

