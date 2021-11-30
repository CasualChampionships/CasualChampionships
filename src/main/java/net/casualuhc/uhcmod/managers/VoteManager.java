package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.*;

public class VoteManager {
    private static final Set<VoteManager> voteManagers = new HashSet<>();

    private final Map<String, String> voteMap = new HashMap<>();

    public VoteManager() {
        voteManagers.add(this);
    }

    public void setVote(String setting, String option) {
        this.voteMap.put(setting, option);
    }

    public Map<String, String> getVotes() {
        return this.voteMap;
    }

    private void resetVotes() {
        this.voteMap.clear();
    }

    public static void resetAllVotes() {
        voteManagers.forEach(VoteManager::resetVotes);
    }

    public static void countVotes() {
        GameSettings.gameSettingMap.forEach((s, gameSetting) -> {
            Map<String, Integer> votes = new HashMap<>();
            voteManagers.forEach(voteManager -> {
                String argument = voteManager.getVotes().get(gameSetting.getName());
                if (argument != null) {
                    votes.computeIfPresent(argument, (s1, integer) -> integer + 1);
                    votes.computeIfAbsent(argument, s1 -> 1);
                }
            });
            Random random = new Random();
            Map.Entry<String, Integer> mostVotes = null;
            for (Map.Entry<String, Integer> entry : votes.entrySet()) {
                if (mostVotes == null || mostVotes.getValue() > entry.getValue()) {
                    mostVotes = entry;
                }
                else if (mostVotes.getValue().equals(entry.getValue())) {
                    mostVotes = random.nextBoolean() ? mostVotes : entry;
                }
            }
            if (mostVotes != null) {
                PlayerUtils.messageEveryPlayer(new LiteralText("%s will be set to %s".formatted(gameSetting.getName(), mostVotes.getKey())).formatted(Formatting.GREEN));
                gameSetting.setValueFromOption(mostVotes.getKey());
            }
            else {
                PlayerUtils.messageEveryPlayer(new LiteralText("%s received no votes, it will remain as default".formatted(gameSetting.getName())).formatted(Formatting.GOLD));
            }
        });
        resetAllVotes();
    }
}
