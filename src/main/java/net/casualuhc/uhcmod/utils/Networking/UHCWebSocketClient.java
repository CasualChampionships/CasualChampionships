package net.casualuhc.uhcmod.utils.Networking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class UHCWebSocketClient extends WebSocketClient {
    private static final Logger NET_LOGGER = LogManager.getLogger("UHC Network");

    public UHCWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handShakeData) {
    }

    @Override
    public void onMessage(String message) {
        System.out.print(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        NET_LOGGER.info("Closed");
    }

    @Override
    public void send(String text) {
        try {
            super.send(text);
        }
        catch (WebsocketNotConnectedException e) {

        }
    }

    @Override
    public void onError(Exception e) {
        NET_LOGGER.error("WebSocket Error: {}", e.toString());
    }
}

