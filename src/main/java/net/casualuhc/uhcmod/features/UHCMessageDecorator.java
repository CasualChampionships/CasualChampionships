package net.casualuhc.uhcmod.features;

import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public enum UHCMessageDecorator implements MessageDecorator {
    INSTANCE;

    @Override
    public CompletableFuture<Text> decorate(@Nullable ServerPlayerEntity sender, Text message) {
        String messageText = message.getString();
        if (messageText.startsWith("!")) {
            message = Text.literal(messageText.substring(1));
        }
        return CompletableFuture.completedFuture(message);
    }
}
