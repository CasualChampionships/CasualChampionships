package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.utils.Event.Events;

public enum Phase {
    NONE,
    SETUP,
    LOBBY,
    READY,
    START,
    ACTIVE,
    END;

    static {
        Events.ON_SETUP.addListener(v -> {
            GameManager.INSTANCE.setCurrentPhase(SETUP);
            GameManager.INSTANCE.setBeforeGamerules();
            TeamManager.createTeams();
        });
        Events.ON_LOBBY.addListener(v -> {
            GameManager.INSTANCE.setCurrentPhase(LOBBY);
            GameManager.INSTANCE.generateLobby();
        });
        Events.ON_READY.addListener(v -> {
            GameManager.INSTANCE.setCurrentPhase(READY);
            TeamUtils.unReadyAllTeams();
            TeamUtils.sendReadyMessage();
        });
        Events.ON_START.addListener(v -> {
            GameManager.INSTANCE.setCurrentPhase(START);
            GameManager.INSTANCE.startCountDown();
        });
        Events.ON_ACTIVE.addListener(v -> {
            GameManager.INSTANCE.setCurrentPhase(ACTIVE);
            GameManager.INSTANCE.startGracePeriod();
            GameManager.INSTANCE.setUHCGamerules();
            PlayerUtils.forceUpdateGlowing();
        });
        Events.ON_END.addListener(v -> {
            GameManager.INSTANCE.setCurrentPhase(END);
            GameManager.INSTANCE.endUHC();
            PlayerUtils.forceUpdateGlowing();
        });
    }
}
