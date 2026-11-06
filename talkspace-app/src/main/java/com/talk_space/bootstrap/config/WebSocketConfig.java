package com.talk_space.bootstrap.config;
import com.talk_space.modules.auth.internal.security.SecureSocketInterceptor;
import com.talk_space.modules.auth.internal.security.SecureSocketHandshake;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @org.springframework.beans.factory.annotation.Autowired
    private com.talk_space.modules.auth.internal.security.SecureSocketInterceptor secureSocketInterceptor;

    @org.springframework.beans.factory.annotation.Value("${app.auth.frontend-origin:http://localhost:3000}")
    private String frontendOrigin;

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(secureSocketInterceptor);
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-secure")
                .addInterceptors(new com.talk_space.modules.auth.internal.security.SecureSocketHandshake())
                .setAllowedOrigins(frontendOrigin)
                .withSockJS();
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin)
                .withSockJS();
    }
}