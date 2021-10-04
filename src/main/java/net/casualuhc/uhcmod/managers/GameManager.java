package net.casualuhc.uhcmod.managers;

public class GameManager {

    private GameState gameState = GameState.SETUP;

    public static void setGameState(GameState gameState) {
        switch (gameState) {
            case SETUP:
                System.out.println("Setup");
                // Make Holding room
                // Pre-gen World
                // Setup Teams
                // Kman can add this :)
                break;
            case LOBBY:
                System.out.println("lobby");
                // Whitelist Everyone
                // Open the server for people to join
                // Kman can add this :)
                break;
            case VOTING:
                System.out.println("Voting");
                // Just ignore for now I guess
                break;
            case STARTING:
                System.out.println("Starting");
                // Coutdown to start
                // Spread out Teams
                // Switch state to Active
                // Kman can add this :)
                break;
            case ACTIVE:
                System.out.println("Active");
                // Set grace period to 10 mins
                // Do wb stuff
                // Handle Player deaths to drop golden apples when someone dies
                // Other stuff needed for games
                // Kman can add this :)
                break;
            case END:
                System.out.println("End");
                // I have no idea what to put here?
                // Announce team that one using sb titles
                break;
        }
    }
}
