package com.talk_space.security;
import com.talk_space.api.security.*;
import com.talk_space.repository.UserRepository;
import com.talk_space.model.domain.User;
import com.talk_space.model.enums.*;
import org.junit.jupiter.api.*;
import org.springframework.messaging.*;
import org.springframework.messaging.support.*;
import org.springframework.messaging.simp.stomp.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class SecureSocketInterceptorTest {
    JwtTokenUtils tokens=mock(JwtTokenUtils.class);
    UserRepository users=mock(UserRepository.class);
    SecureSocketInterceptor interceptor=new SecureSocketInterceptor(tokens,users);
    StompHeaderAccessor accessor(StompCommand command, boolean secure) {
        var a=StompHeaderAccessor.create(command);a.setSessionAttributes(new HashMap<>(Map.of("secureSocket",secure)));a.setLeaveMutable(true);return a;
    }
    Message<byte[]> message(StompHeaderAccessor a) { return MessageBuilder.createMessage(new byte[0],a.getMessageHeaders()); }
    @Test void secureSocketRequiresBearerToken() {
        assertThrows(MessageDeliveryException.class,()->interceptor.preSend(message(accessor(StompCommand.CONNECT,true)),null));
    }
    @Test void principalUsesVerifiedUsernameFromTokenRatherThanClientHeader() {
        var a=accessor(StompCommand.CONNECT,true);a.setNativeHeader("Authorization","Bearer token");
        var u=new User();u.setUserName("alice");u.setVerifyMail(true);u.setRole(Role.USER);u.setStatus(Status.ACTIVE);
        when(tokens.getName("token")).thenReturn("alice@gmail.com");when(users.findUserByEmail("alice@gmail.com")).thenReturn(Optional.of(u));
        interceptor.preSend(message(a),null);assertEquals("alice",a.getUser().getName());
    }
    @Test void bothSocketEndpointsRequireAuthentication() {
        assertThrows(MessageDeliveryException.class,()->interceptor.preSend(message(accessor(StompCommand.CONNECT,false)),null));
        assertThrows(MessageDeliveryException.class,()->interceptor.preSend(message(accessor(StompCommand.SUBSCRIBE,false)),null));
    }
    @Test void chatUsesPrivateQueuesOnlyAndPublicBroadcastIsRejected() {
        var privateQueue=accessor(StompCommand.SUBSCRIBE,true); privateQueue.setUser(()->"alice"); privateQueue.setDestination("/user/queue/messages");
        assertDoesNotThrow(()->interceptor.preSend(message(privateQueue),null));
        var broadcast=accessor(StompCommand.SEND,true); broadcast.setUser(()->"alice"); broadcast.setDestination("/topic/public");
        assertThrows(MessageDeliveryException.class,()->interceptor.preSend(message(broadcast),null));
        var guessed=accessor(StompCommand.SUBSCRIBE,true); guessed.setUser(()->"alice"); guessed.setDestination("/user/bob/queue/messages");
        assertThrows(MessageDeliveryException.class,()->interceptor.preSend(message(guessed),null));
    }
    @Test void authenticatedSocketCannotSubscribeToRawBrokerQueues() {
        var a=accessor(StompCommand.SUBSCRIBE,true);a.setUser(()->"alice");a.setDestination("/queue/connection-notifications");
        assertThrows(MessageDeliveryException.class,()->interceptor.preSend(message(a),null));
    }
}
