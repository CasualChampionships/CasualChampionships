package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.UHCEvents;

public enum Phase {
    NONE,
    SETUP,
    LOBBY,
    READY,
    START,
    ACTIVE,
    END;

    static {
        EventHandler.register(new UHCEvents() {
            @Override
            public void onSetup() {
                GameManager.setPhase(SETUP);
                GameManager.setBeforeGamerules();
                TeamUtils.createTeams();
            }

            @Override
            public void onLobby() {
                GameManager.setPhase(LOBBY);
                GameManager.generateLobby();
            }

            @Override
            public void onReady() {
                GameManager.setPhase(READY);
                TeamUtils.unReadyAllTeams();
                TeamUtils.sendReadyMessage();
            }

            @Override
            public void onStart() {
                GameManager.setPhase(START);
                GameManager.startCountDown();
            }

            @Override
            public void onActive() {
                GameManager.setPhase(ACTIVE);
                PlayerExtension.reset();
                GameManager.resetTrackers();
                GameManager.startGracePeriod();
                GameManager.setUHCGamerules();
                PlayerUtils.forceUpdateGlowing();
                PlayerUtils.forceUpdateFullBright();
            }

            @Override
            public void onEnd() {
                GameManager.setPhase(END);
                GameManager.endUHC();
                PlayerUtils.forceUpdateGlowing();
            }
        });
    }
}
