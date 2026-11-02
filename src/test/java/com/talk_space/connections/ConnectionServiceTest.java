package com.talk_space.connections;
import com.talk_space.repository.UserRepository;
import com.talk_space.model.domain.User;
import com.talk_space.model.enums.*;
import com.talk_space.notifications.NotificationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {
    @Mock ConnectionRepository connections; @Mock UserRepository users; @Mock NotificationService notifications; @Mock EntityManager em; @Mock NetworkAccess access;
    @InjectMocks ConnectionService service;
    Connection connection(Connection.State state) {
        var c=new Connection();c.setId(7L);c.setSender("alice");c.setReceiver("bob");c.setStatus(state);return c;
    }
    void locks() {
        when(users.lockMatchingViewer("alice")).thenReturn(Optional.of(new User()));
        when(users.lockMatchingViewer("bob")).thenReturn(Optional.of(new User()));
    }
    @Test void cannotRequestYourself() {
        assertThrows(ResponseStatusException.class,()->service.request("alice","alice"));verifyNoInteractions(connections,notifications);
    }
    @Test void recipientCanAcceptAndNotificationIsPersisted() {
        var c=connection(Connection.State.PENDING);when(connections.findById(7L)).thenReturn(Optional.of(c));locks();
        assertEquals(Connection.State.ACCEPTED,service.transition("bob",7,"accept").getStatus());
        verify(notifications).create("alice","bob","CONNECTION_ACCEPTED",7L);
    }
    @Test void senderCannotAcceptOwnRequest() {
        when(connections.findById(7L)).thenReturn(Optional.of(connection(Connection.State.PENDING)));locks();
        var ex=assertThrows(ResponseStatusException.class,()->service.transition("alice",7,"accept"));assertEquals(403,ex.getStatusCode().value());
        verifyNoInteractions(notifications);
    }
    @Test void outsiderCannotMutateOrObserveConnection() {
        when(connections.findById(7L)).thenReturn(Optional.of(connection(Connection.State.PENDING)));
        var ex=assertThrows(ResponseStatusException.class,()->service.transition("eve",7,"reject"));assertEquals(404,ex.getStatusCode().value());
        verifyNoInteractions(users,notifications);
    }
    @Test void acceptedConnectionCannotBeCancelled() {
        when(connections.findById(7L)).thenReturn(Optional.of(connection(Connection.State.ACCEPTED)));locks();
        assertThrows(ResponseStatusException.class,()->service.transition("alice",7,"cancel"));verifyNoInteractions(notifications);
    }
    @Test void repeatedAcceptDoesNotDuplicateNotification() {
        when(connections.findById(7L)).thenReturn(Optional.of(connection(Connection.State.ACCEPTED)));locks();
        service.transition("bob",7,"accept");verifyNoInteractions(notifications);
    }
    @Test void duplicatePendingRequestIsIdempotent() {
        locks();var target=new User();target.setStatus(Status.ACTIVE);target.setRole(Role.USER);target.setVerifyMail(true);
        when(users.findUserByUserName("bob")).thenReturn(Optional.of(target));
        when(connections.findByPairLowAndPairHigh("alice","bob")).thenReturn(Optional.of(connection(Connection.State.PENDING)));
        assertEquals(7L,service.request("alice","bob").getId());verifyNoInteractions(notifications);
    }
    @Test void pairLocksUseSameOrderForOppositeRequests() {
        locks();var target=new User();target.setStatus(Status.ACTIVE);target.setRole(Role.USER);target.setVerifyMail(true);
        when(users.findUserByUserName("alice")).thenReturn(Optional.of(target));
        when(connections.findByPairLowAndPairHigh("alice","bob")).thenReturn(Optional.of(connection(Connection.State.PENDING)));
        assertThrows(ResponseStatusException.class,()->service.request("bob","alice"));
        var order=inOrder(users);order.verify(users).lockMatchingViewer("alice");order.verify(users).lockMatchingViewer("bob");
    }
}
