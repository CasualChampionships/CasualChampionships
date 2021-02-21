package net.casualuhc.uhcmod.gui.button;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntTag;

import java.util.HashMap;

public class StackButton {

    public static HashMap<Integer, StackButton> buttons = new HashMap<>();
    public static final String TAG = "uhc-mod-button";

    protected Item item;
    protected Runnable handler = () -> {};

    public static ItemStack createButton(Item item){
        return new StackButton().item(item).asStack();
    }

    public static ItemStack createButton(Item item, Runnable handler){
        return new StackButton().item(item).handler(handler).asStack();
    }

    public StackButton(){
        StackButton.buttons.put(this.hashCode(), this);
    }

    protected StackButton item(Item item){
        this.item = item;
        return this;
    }

    protected ItemStack asStack(){
        ItemStack stack = new ItemStack(this.item);
        stack.putSubTag(TAG,  IntTag.of(this.hashCode()));
        return stack;
    }

    protected StackButton handler(Runnable handler){
        this.handler = handler;
        return this;
    }

    public void handle(){
        this.handler.run();
    }

}
