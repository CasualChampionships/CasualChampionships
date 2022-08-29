package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.managers.GameManager;
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
            GameManager.setPhase(SETUP);
            GameManager.setBeforeGamerules();
            TeamUtils.createTeams();
        });
        Events.ON_LOBBY.addListener(v -> {
            GameManager.setPhase(LOBBY);
            GameManager.generateLobby();
        });
        Events.ON_READY.addListener(v -> {
            GameManager.setPhase(READY);
            TeamUtils.unReadyAllTeams();
            TeamUtils.sendReadyMessage();
        });
        Events.ON_START.addListener(v -> {
            GameManager.setPhase(START);
            GameManager.startCountDown();
        });
        Events.ON_ACTIVE.addListener(v -> {
            GameManager.setPhase(ACTIVE);
            GameManager.startGracePeriod();
            GameManager.setUHCGamerules();
            PlayerUtils.forceUpdateGlowing();
        });
        Events.ON_END.addListener(v -> {
            GameManager.setPhase(END);
            GameManager.endUHC();
            PlayerUtils.forceUpdateGlowing();
        });
    }
}
