package net.casualuhc.uhcmod.utils.Networking;

import net.casualuhc.uhcmod.UHCMod;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class UHCWebSocketClient extends WebSocketClient {
    public UHCWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connection to Discord bot has been established");
//        UHCMod.UHCSocketClient.send("Connected!");
    }

    @Override
    public void onMessage(String message) {
        System.out.print(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed");
    }

    @Override
    public void send(String text) {
        try {
            super.send(text);
        }
        catch (WebsocketNotConnectedException e) {
            UHCMod.UHCLogger.error("WebSocket Not Connected: {}", e.toString());
        }
    }

    @Override
    public void onError(Exception e) {
        UHCMod.UHCLogger.error("WebSocket Error: {}", e.toString());
    }
}

