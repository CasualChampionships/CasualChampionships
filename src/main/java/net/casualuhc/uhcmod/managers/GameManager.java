package net.casualuhc.uhcmod.managers;

import net.casualuhc.uhcmod.helpers.SetupHelper;
import net.casualuhc.uhcmod.interfaces.GameStateFunction;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class GameManager {

    private final GameStateFunction gameStateFunction;

    public GameManager(GameStateFunction gameStateFunction) {
        this.gameStateFunction = gameStateFunction;
    }

    public GameStateFunction getFunction() {
        return this.gameStateFunction;
    }

    public static GameManager SETUP = new GameManager(() -> {
        System.out.println("Setup");
        // Make Holding room
        // Pre-gen World
        // Setup Teams
        // Kman can add this :)
    });

    public static GameManager VOTING = new GameManager(() -> {
        System.out.println("VOTE");
        // Just ignore for now I guess
    });

    public static GameManager LOBBY = new GameManager(() -> {
        System.out.println("LOBBY");
        // Whitelist Everyone
        // Open the server for people to join
        // Kman can add this :)
    });

    public static GameManager STARTING = new GameManager(() -> {
        System.out.println("START");
        // Coutdown to start
        // Spread out Teams
        // Switch state to Active
        // Kman can add this :)
    });

    public static GameManager ACTIVE = new GameManager(() -> {
        System.out.println("ACTIVE");
        // Set grace period to 10 mins
        // Do wb stuff
        // Handle Player deaths to drop golden apples when someone dies
        // Other stuff needed for games
        // Kman can add this :)
    });

    public static GameManager END = new GameManager(() -> {
        System.out.println("END");
        // I have no idea what to put here?
        // Announce team that one using sb titles
    });
}

