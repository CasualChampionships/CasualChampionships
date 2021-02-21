package net.casualuhc.uhcmod.gui.button;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.HashMap;

public class StackButton {

    public static HashMap<Integer, StackButton> buttons = new HashMap<>();
    public static final String TAG = "uhc-mod-button";

    protected Item item;
    protected Runnable handler = () -> {};
    protected String name;

    public static ItemStack createButton(Item item, String name){
        return new StackButton().item(item).name(name).asStack();
    }

    public static ItemStack createButton(Item item, String name, Runnable handler){
        return new StackButton().item(item).name(name).handler(handler).asStack();
    }

    public StackButton(){
        StackButton.buttons.put(this.hashCode(), this);
    }

    public StackButton name(String name){
        this.name = name;
        return this;
    }

    public StackButton item(Item item){
        this.item = item;
        return this;
    }

    public ItemStack asStack(){
        ItemStack stack = new ItemStack(this.item);
        stack.putSubTag(TAG,  IntTag.of(this.hashCode()));
        stack.setCustomName(new LiteralText(name).setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.WHITE)));
        return stack;
    }

    public StackButton handler(Runnable handler){
        this.handler = handler;
        return this;
    }

    public void handle(){
        this.handler.run();
    }

}
