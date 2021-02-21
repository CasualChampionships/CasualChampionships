package net.casualuhc.uhcmod.gui.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.casualuhc.uhcmod.gui.button.StackButton.createButton;

public class SpectatorScreen extends InventoryScreen<PlayerInventory>{

    protected ServerPlayerEntity player;

    public SpectatorScreen(ServerPlayerEntity player){
        super(player.inventory);
        this.player = player;
        this.build();
    }

    protected void build(){
        inventory.clear();

        inventory.insertStack(0, createButton(Items.BLAZE_ROD, "Prev Team", () -> System.out.println("Blaze")));
        inventory.insertStack(8, createButton(Items.STICK, "Next Team", () -> System.out.println("Stick")));

        inventory.insertStack(9 + 1, createButton(Items.RED_STAINED_GLASS, "Subscribe to Cameraman"));
        inventory.insertStack(9 + 4, createButton(Items.ENDER_EYE, "Goto Cameraman"));
        inventory.insertStack(9 + 7, createButton(Items.ENDER_PEARL, "Goto Team"));

        int slot = 18;
        for (Team team : player.server.getScoreboard().getTeams()) {
            inventory.insertStack(slot++, createButton(Items.WHITE_CONCRETE, team.getName()));
        }

    }
}
