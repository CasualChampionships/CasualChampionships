package net.casualuhc.uhcmod.gui.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.casualuhc.uhcmod.gui.button.StackButton.createButton;

public class SpectatorScreen extends InventoryScreen<PlayerInventory>{

    protected ServerPlayerEntity player;

    public SpectatorScreen(ServerPlayerEntity player){
        super(player.inventory);
        this.player = player;
    }

    protected void build(){
        inventory.clear();

        inventory.insertStack(0, createButton(Items.BLAZE_ROD, () -> System.out.println("Blaze")));
        inventory.insertStack(8, createButton(Items.STICK, () -> System.out.println("Stick")));
    }
}
