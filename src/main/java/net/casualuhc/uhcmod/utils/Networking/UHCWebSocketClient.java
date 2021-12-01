package net.casualuhc.uhcmod.utils.Networking;

import java.net.URI;

import net.casualuhc.uhcmod.UHCMod;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

public class UHCWebSocketClient extends WebSocketClient {
    public UHCWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public WebSocket getConnection() {
        return super.getConnection();
    }

    @Override
    public void onOpen(ServerHandshake handShakeData) {
        this.getConnection().send("Connected to the server");
    }

    @Override
    public void onMessage(String message) {
        // We can pass messages from discord to the in-game chat
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
    public void onClose(int code, String reason, boolean remote) { }

    @Override
    public void onError(Exception e) {
        UHCMod.UHCLogger.error("WebSocket Error: {}", e.toString());
    }
}
