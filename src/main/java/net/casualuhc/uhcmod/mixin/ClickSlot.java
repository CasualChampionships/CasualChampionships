package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.gui.button.StackButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public class ClickSlot {

    @Shadow private ItemStack stack;

    @Inject(method = "apply", at= @At("HEAD"))
    public void apply(ServerPlayPacketListener serverPlayPacketListener, CallbackInfo cb){
        Tag tag = this.stack.getOrCreateTag().get(StackButton.TAG);
        if (tag == null) return;
        StackButton button = StackButton.buttons.get(((IntTag) tag).getInt());
        if (button == null) return;
        button.handle();
    }

}
