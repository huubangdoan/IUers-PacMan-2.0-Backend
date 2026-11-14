package com.Iuer.Pacman.config;

import com.Iuer.Pacman.game.GameWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSocket
public class GameWebSocketConfig implements WebSocketConfigurer {
    private final GameWebSocketHandler handler;
    private final List<String> allowedOrigins;

    public GameWebSocketConfig(
            GameWebSocketHandler handler,
            @Value("${app.websocket.allowed-origins:http://localhost:5173}") String origins
    ) {
        this.handler = handler;
        this.allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/game")
                .setAllowedOrigins(allowedOrigins.toArray(String[]::new));
    }
}