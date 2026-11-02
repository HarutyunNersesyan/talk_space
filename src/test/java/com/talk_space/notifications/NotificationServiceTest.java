package com.talk_space.notifications;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository; @Mock ApplicationEventPublisher events; @InjectMocks NotificationService service;
    @Test void anotherUsersNotificationCannotBeMarkedRead() {
        var n=new Notification();n.setRecipient("bob");when(repository.findById(1L)).thenReturn(Optional.of(n));
        assertThrows(ResponseStatusException.class,()->service.read("eve",1));verify(repository,never()).save(any());
    }
    @Test void creatingNotificationStoresBeforePublishingEvent() {
        service.create("bob","alice","CONNECTION_REQUEST",4L);
        var order=inOrder(repository,events);order.verify(repository).save(any());order.verify(events).publishEvent(any(NotificationService.Created.class));
    }
    @Test void readIsIdempotent() {
        var n=new Notification();n.setRecipient("bob");n.setReadAt(java.time.Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(n));service.read("bob",1);verify(repository,never()).save(any());
    }
}
