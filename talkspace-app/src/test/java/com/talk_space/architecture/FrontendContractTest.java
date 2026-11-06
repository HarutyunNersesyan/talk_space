package com.talk_space.architecture;

import com.talk_space.modules.auth.internal.web.AuthController;
import com.talk_space.bootstrap.config.WebSocketConfig;
import com.talk_space.modules.auth.api.dto.TokenResponse;
import com.talk_space.modules.auth.api.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Regression guards for existing React frontend contracts. */
class FrontendContractTest {
    @Test
    void authenticationEndpointIsUnchanged() throws Exception {
        assertEquals("/account", AuthController.class.getAnnotation(RequestMapping.class).value()[0]);
        var method = AuthController.class.getMethod("login", UserDto.class, jakarta.servlet.http.HttpServletRequest.class, String.class);
        assertEquals("/auth", method.getAnnotation(PostMapping.class).value()[0]);
        assertEquals(ResponseEntity.class, method.getReturnType());
        assertNotNull(method.getParameters()[0].getAnnotation(RequestBody.class));
    }

    @Test
    void logoutEndpointIsUnchanged() throws Exception {
        var method = AuthController.class.getMethod("legacyLogout");
        assertEquals("/profile/logout", method.getAnnotation(GetMapping.class).value()[0]);
    }

    @Test
    void loginResponseJsonFieldsAreUnchanged() throws Exception {
        assertNotNull(TokenResponse.class.getDeclaredField("status"));
        assertNotNull(TokenResponse.class.getDeclaredField("token"));
        assertNotNull(TokenResponse.class.getDeclaredField("userName"));
    }

    @Test
    void websocketDestinationsAreUnchanged() {
        var config = new WebSocketConfig();
        var endpoints = mock(StompEndpointRegistry.class);
        var registration = mock(org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration.class);
        when(endpoints.addEndpoint(anyString())).thenReturn(registration);
        when(registration.addInterceptors(any())).thenReturn(registration);
        org.springframework.test.util.ReflectionTestUtils.setField(config,"frontendOrigin","http://localhost:3000");
        when(registration.setAllowedOrigins("http://localhost:3000")).thenReturn(registration);
        config.registerStompEndpoints(endpoints);
        verify(endpoints).addEndpoint("/ws");
        verify(registration, times(2)).withSockJS();
        verify(endpoints).addEndpoint("/ws-secure");
    }
}
