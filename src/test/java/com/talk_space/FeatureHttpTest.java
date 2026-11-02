package com.talk_space;
import com.talk_space.model.domain.User;
import com.talk_space.model.enums.*;
import com.talk_space.repository.UserRepository;
import com.talk_space.notifications.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class FeatureHttpTest {
    @Autowired MockMvc http;
    @Autowired UserRepository users;
    @Autowired NotificationRepository notifications;
    @BeforeEach void setup() {
        for(String name:List.of("alice","bob")) {
            User u=new User();u.setFirstName("Alice");u.setLastName("Smith");u.setUserName(name);
            u.setBirthDate(LocalDate.of(2000,1,1));u.setEmail(name+"@gmail.com");u.setGender(Gender.FEMALE);
            u.setPassword("hash");u.setRole(Role.USER);u.setStatus(Status.ACTIVE);u.setVerifyMail(true);users.save(u);
        }
        users.flush();
    }
    @Test void createsConnectionForAuthenticatedActorOnly() throws Exception {
        http.perform(post("/api/public/connections").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("sender").value("alice"))
            .andExpect(jsonPath("receiver").value("bob")).andExpect(jsonPath("status").value("PENDING"));
        http.perform(get("/api/public/connections/pending-count").with(user("bob@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("count").value(1));
        http.perform(get("/api/public/notifications/unread-count").with(user("bob@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("count").value(1));
    }
    @Test void privateInboxCannotBeReadAnonymously() throws Exception {
        http.perform(get("/api/public/notifications")).andExpect(status().is4xxClientError());
    }
    @Test void cannotMarkAnotherUsersNotificationRead() throws Exception {
        Notification n=new Notification();n.setRecipient("bob");n.setActor("alice");n.setType("CONNECTION_REQUEST");n.setCreatedAt(Instant.now());
        n=notifications.saveAndFlush(n);
        http.perform(post("/api/public/notifications/"+n.getId()+"/read").with(user("alice@gmail.com")))
            .andExpect(status().isNotFound());
    }
    @Test void invalidPageAndBlankRecipientReturnBadRequest() throws Exception {
        http.perform(get("/api/public/notifications?page=-1").with(user("alice@gmail.com"))).andExpect(status().isBadRequest());
        http.perform(post("/api/public/connections").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"\"}")).andExpect(status().isBadRequest());
    }
    @Test void disabledSearchHasExplicitUnavailableResponse() throws Exception {
        http.perform(get("/api/public/search/users?q=music").with(user("alice@gmail.com")))
            .andExpect(status().isServiceUnavailable());
    }
    @Test void malformedJwtStopsAtUnauthorized() throws Exception {
        http.perform(get("/api/public/notifications").header("Authorization","Bearer malformed"))
            .andExpect(status().isUnauthorized());
    }
    @Test void metricsRequireAdminAuthority() throws Exception {
        http.perform(get("/actuator/metrics").with(user("alice@gmail.com").authorities(()->"USER")))
            .andExpect(status().isForbidden());
        http.perform(get("/actuator/metrics").with(user("admin@gmail.com").authorities(()->"ADMIN")))
            .andExpect(status().isOk());
    }
    @Test void acceptedConnectionAppearsInExistingChatList() throws Exception {
        var response=http.perform(post("/api/public/connections").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\"}"))
            .andExpect(status().isOk()).andReturn();
        long id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.getResponse().getContentAsString()).path("id").asLong();
        http.perform(post("/api/public/connections/"+id+"/accept").with(user("bob@gmail.com"))).andExpect(status().isOk());
        http.perform(get("/api/public/chat/conversations/alice").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].partnerUsername").value("bob"));
    }
}
