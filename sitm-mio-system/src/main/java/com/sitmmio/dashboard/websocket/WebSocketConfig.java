package com.sitmmio.dashboard.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration("busMonitorWebSocketConfig")
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final BusWebSocketHandler handler;

    public WebSocketConfig(BusWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(handler, "/bus-monitor")
                .setAllowedOrigins("*");
    }
}
