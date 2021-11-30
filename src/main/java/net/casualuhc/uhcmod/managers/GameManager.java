package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.utils.Phase;

public class GameManager {
    private static Phase currentPhase = Phase.NONE;

    public static boolean isReadyForPlayers() {
        return currentPhase.phaseNumber() >= 2;
    }

    public static boolean isGameActive() {
        return currentPhase.phaseNumber() >= 5;
    }

    public static boolean isPhase(Phase phase) {
        return currentPhase.equals(phase);
    }

    public static void setCurrentPhase(Phase phase) {
        currentPhase = phase;
    }

    public static Phase getCurrentPhase() {
        return currentPhase;
    }
}

