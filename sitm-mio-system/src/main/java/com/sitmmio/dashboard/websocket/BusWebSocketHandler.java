package com.sitmmio.dashboard.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BusWebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions =
            ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {

        sessions.add(session);

        System.out.println("Cliente conectado: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session,
                              WebSocketMessage<?> message) {

    }

    @Override
    public void handleTransportError(WebSocketSession session,
                                     Throwable exception) {

        System.out.println("Error websocket");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus closeStatus) {

        sessions.remove(session);

        System.out.println("Cliente desconectado");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void enviarUbicacionBus(String mensaje) {

        sessions.forEach(session -> {

            try {

                session.sendMessage(
                        new TextMessage(mensaje)
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

