package com.techrentalhub.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kênh để server đẩy tin nhắn về client (Prefix: /topic)
        config.enableSimpleBroker("/topic");
        
        // Prefix cho các message từ client gửi lên server (nếu có)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint để client kết nối tới server
        registry.addEndpoint("/ws-rental")
                .setAllowedOriginPatterns("*") // Cho phép mọi nguồn (nên giới hạn trong production)
                .withSockJS(); // Hỗ trợ fallback nếu browser không hỗ trợ WebSocket thuần
    }
}
