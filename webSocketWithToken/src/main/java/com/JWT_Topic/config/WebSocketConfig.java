package com.JWT_Topic.config;

import com.JWT_Topic.handler.ChatHandler;
import com.JWT_Topic.service.JWTService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JWTService jwtService;

    public WebSocketConfig(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/chat")
                .setAllowedOrigins("*");
    }

    @Bean
    WebSocketHandler chatHandler() {
        return new ChatHandler(jwtService);
    }
}
