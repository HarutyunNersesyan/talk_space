package com.talk_space.api.security;

import com.talk_space.repository.UserRepository;
import com.talk_space.model.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.support.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.*;

@Component @RequiredArgsConstructor
public class SecureSocketInterceptor implements ChannelInterceptor {
    private final JwtTokenUtils tokens;
    private final UserRepository users;
    private static final Set<String> SUBSCRIPTIONS=Set.of(
        "/user/queue/connection-notifications", "/user/queue/presence", "/user/queue/messages",
        "/user/queue/typing", "/user/queue/errors", "/user/queue/receipts");
    private static final Set<String> SENDS=Set.of("/app/presence.heartbeat", "/app/chat.send", "/app/typing");
    @Override public Message<?> preSend(Message<?> message,MessageChannel channel) {
        var headers=MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if (headers==null) return message;
        if (headers.getCommand()==StompCommand.CONNECT) {
            String header=headers.getFirstNativeHeader("Authorization");
            if (header==null || !header.startsWith("Bearer ")) throw new MessageDeliveryException("Bearer token required");
            try {
                var user=users.findUserByEmail(tokens.getName(header.substring(7))).filter(u->u.getStatus()==Status.ACTIVE
                    && u.getRole()==Role.USER && Boolean.TRUE.equals(u.getVerifyMail())).orElseThrow();
                headers.setUser(new UsernamePasswordAuthenticationToken(user.getUserName(),null,List.of()));
            } catch (RuntimeException ex) { throw new MessageDeliveryException("Invalid or unavailable account"); }
        } else if (headers.getCommand()==StompCommand.SEND || headers.getCommand()==StompCommand.SUBSCRIBE) {
            if (headers.getUser()==null) throw new MessageDeliveryException("Authentication required");
            boolean allowed=headers.getDestination()!=null && (headers.getCommand()==StompCommand.SUBSCRIBE ? SUBSCRIPTIONS.contains(headers.getDestination()) : SENDS.contains(headers.getDestination()));
            if (!allowed) throw new MessageDeliveryException("Destination is not allowed");
        }
        return message;
    }
}
