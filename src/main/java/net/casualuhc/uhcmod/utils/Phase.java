package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.managers.VoteManager;
import net.casualuhc.uhcmod.managers.WorldBoarderManager;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public record Phase(Runnable runnable, int phaseNumber) {

    private static final ThreadGroup PHASE_THREAD_GROUP = new ThreadGroup("Phase Thread Group");

    public static final Phase
        NONE = new Phase(() -> { }, 0),
        SETUP = new Phase(Phase::setup, 1),
        LOBBY = new Phase(Phase::lobby, 2),
        VOTING = new Phase(Phase::voting, 3),
        READY = new Phase(Phase::ready, 4),
        START = new Phase(Phase::start, 5),
        ACTIVE = new Phase(Phase::active, 6),
        END = new Phase(Phase::end, 7);

    public void run() {
        PHASE_THREAD_GROUP.interrupt();
        GameManager.setCurrentPhase(this);
        this.runnable.run();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Phase phase) {
            return phase.phaseNumber == this.phaseNumber;
        }
        return false;
    }

    private static void setup() {
        GameManagerUtils.setBeforeGamerules();
        TeamManager.createTeams();
    }

    private static void lobby() {
        GameManagerUtils.generateLobby();
        PlayerUtils.forEveryPlayer(playerEntity -> playerEntity.teleport(UHCMod.UHCServer.getOverworld(), 0, 253, 0, 0, 0));
    }

    private static void voting() {
        VoteManager.resetAllVotes();
        PlayerUtils.forEveryPlayer(playerEntity -> {
            playerEntity.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("Voting Time!").formatted(Formatting.GREEN)));
            playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.0F, 1.0F);
            playerEntity.sendMessage(new LiteralText("Use the /vote command to vote"), false);
        });
    }

    private static void ready() {
        VoteManager.countVotes();
        TeamUtils.unReadyAllTeams();
        TeamUtils.sendReadyMessage();
    }

    private static void start() {
        GameManagerUtils.startCountDown(PHASE_THREAD_GROUP);
    }

    private static void active() {
        WorldBoarderManager.startWorldBorders(PHASE_THREAD_GROUP, false);
        GameManagerUtils.startPVPCountdown(PHASE_THREAD_GROUP);
        GameManagerUtils.setUHCGamerules();
    }

    private static void end() {
        GameManagerUtils.endUHC(PHASE_THREAD_GROUP);
    }

    public static ThreadGroup getPhaseThreadGroup() {
        return PHASE_THREAD_GROUP;
    }
}
