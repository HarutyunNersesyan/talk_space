package com.talk_space;

import com.talk_space.chat.repository.ChatRepository;

import com.talk_space.blocks.*;
import com.talk_space.connections.*;
import com.talk_space.matching.*;
import com.talk_space.model.domain.*;
import com.talk_space.model.dto.ChatMessageDto;
import com.talk_space.model.enums.*;
import com.talk_space.repository.*;
import com.talk_space.chat.service.ChatService;
import com.talk_space.api.controller.publics.ChatController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ConnectOnlyHttpTest {
    @Autowired MockMvc http;
    @Autowired UserRepository users;
    @Autowired HobbyRepository hobbies;
    @Autowired SpecialityRepository specialities;
    @Autowired ConnectionService connections;
    @Autowired BlockService blocks;
    @Autowired UserBlockRepository blockRepository;
    @Autowired ChatService chat;
    @Autowired ChatController socket;
    @Autowired ChatRepository messages;
    @Autowired MatchingRepository matching;
    @Autowired ConnectionRepository connectionRecords;
    @Autowired ImageRepository images;
    @Autowired com.talk_space.service.ImageService imageService;
    @BeforeEach void setup() {
        Hobby hobby=new Hobby("Music",null); hobbies.saveAndFlush(hobby);
        Speciality speciality=new Speciality("Java",null); specialities.saveAndFlush(speciality);
        for (String name:List.of("alice","bob","eve")) {
            User u=new User();u.setFirstName("Alice");u.setLastName("Smith");u.setUserName(name);
            u.setBirthDate(LocalDate.of(2000,1,1));u.setEmail(name+"@gmail.com");u.setGender(Gender.FEMALE);
            u.setAboutMe("Hello from "+name);u.setHobbies(new ArrayList<>(List.of(hobby)));u.setSpecialities(new ArrayList<>(List.of(speciality)));
            u.setPassword("hash");u.setRole(Role.USER);u.setStatus(Status.ACTIVE);u.setVerifyMail(true);users.save(u);
        }
        users.flush();
    }
    Connection accepted() { var c=connections.request("alice","bob"); return connections.transition("bob",c.getId(),"accept"); }
    @Test void absentImageIsNormalAndUnknownProfileIsStillNotFound() throws Exception {
        http.perform(get("/api/public/user/image/bob").with(user("alice@gmail.com")))
            .andExpect(status().isNoContent()).andExpect(content().string(""));
        http.perform(get("/api/public/user/image/missing").with(user("alice@gmail.com")))
            .andExpect(status().isNotFound());
    }
    @Test void existingImageIsStillServed(@org.junit.jupiter.api.io.TempDir java.nio.file.Path directory) throws Exception {
        Object previous = org.springframework.test.util.ReflectionTestUtils.getField(imageService, "imageDirectory");
        org.springframework.test.util.ReflectionTestUtils.setField(imageService, "imageDirectory", directory.toString());
        try {
            byte[] photo = new byte[] {1, 2, 3};
            java.nio.file.Files.write(directory.resolve("photo.png"), photo);
            Image image = new Image(); image.setFileName("photo.png"); image.setFileType("image/png");
            image.setFilePath(directory.resolve("photo.png").toString()); image.setUser(users.findUserByUserName("bob").orElseThrow());
            images.saveAndFlush(image);
            http.perform(get("/api/public/user/image/bob").with(user("alice@gmail.com")))
                .andExpect(status().isOk()).andExpect(content().contentType("image/png")).andExpect(content().bytes(photo));
            java.nio.file.Files.delete(directory.resolve("photo.png"));
            http.perform(get("/api/public/user/image/bob").with(user("alice@gmail.com"))).andExpect(status().isNoContent());
        } finally {
            org.springframework.test.util.ReflectionTestUtils.setField(imageService, "imageDirectory", previous);
        }
    }
    @Test void requestContainsFullPublicProfileAndRecipientCanReadIt() throws Exception {
        connections.request("alice","bob");
        http.perform(get("/api/public/connections").with(user("bob@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("content[0].profile.userName").value("alice"))
            .andExpect(jsonPath("content[0].profile.hobbies[0]").value("Music"))
            .andExpect(jsonPath("content[0].profile.specialities[0]").value("Java"))
            .andExpect(jsonPath("content[0].profile.about").value("Hello from alice"))
            .andExpect(jsonPath("content[0].profile.email").doesNotExist())
            .andExpect(jsonPath("content[0].profile.password").doesNotExist());
    }
    @Test void recentRequestsCoolDownAndBecomeDiscoverableAgain() {
        var c=connections.request("alice","bob");
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        assertFalse(matching.candidates("bob",MatchMode.HOBBIES,Instant.now(),100).contains("alice"));
        c.setUpdatedAt(Instant.now().minus(Duration.ofHours(25))); connectionRecords.saveAndFlush(c);
        assertTrue(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        connections.transition("bob",c.getId(),"reject"); connectionRecords.flush();
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        c.setUpdatedAt(Instant.now().minus(Duration.ofDays(8))); connectionRecords.saveAndFlush(c);
        assertTrue(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
    }
    @Test void skipIsOwnedByActorSurvivesCycleResetAndExpires() throws Exception {
        matching.record("alice","bob",MatchMode.HOBBIES,Instant.now());
        http.perform(post("/api/public/matching/skip").with(user("eve@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"mode\":\"HOBBIES\"}"))
            .andExpect(status().isNotFound());
        http.perform(post("/api/public/matching/skip").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"mode\":\"HOBBIES\"}"))
            .andExpect(status().isNoContent());
        matching.restartCycle("alice",MatchMode.HOBBIES);
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now().plusSeconds(1),100).contains("bob"));
        assertTrue(matching.candidates("alice",MatchMode.SPECIALITIES,Instant.now().plusSeconds(1),100).contains("bob"));
    }
    @Test void agedSkipIsEligibleAndInvalidSkipPayloadIsRejected() throws Exception {
        matching.record("alice","bob",MatchMode.HOBBIES,Instant.now().minus(Duration.ofDays(2)));
        matching.skip("alice","bob",MatchMode.HOBBIES,Instant.now().minus(Duration.ofDays(2)));
        assertTrue(matching.candidates("alice",MatchMode.HOBBIES,Instant.now().plusSeconds(1),100).contains("bob"));
        http.perform(post("/api/public/matching/skip").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"mode\":\"INVALID\"}"))
            .andExpect(status().isBadRequest());
        http.perform(post("/api/public/matching/skip").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"\",\"mode\":\"HOBBIES\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void matchingEndpointReturnsServerCalculatedScoreAndExplanation() throws Exception {
        http.perform(get("/api/public/user/searchByHobbies/alice").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("matching.score").value(100.0))
            .andExpect(jsonPath("matching.commonHobbies[0]").value("Music"))
            .andExpect(jsonPath("matching.commonSpecialities[0]").value("Java"))
            .andExpect(jsonPath("matching.hobbyWeight").value(80.0))
            .andExpect(jsonPath("matching.algorithmVersion").value("2.0"));
    }
    @Test void httpSendConfirmsSaveAndDuplicateRetryDoesNotInsertTwice() throws Exception {
        accepted();
        String body="{\"receiver\":\"bob\",\"content\":\"hello\",\"clientMessageId\":\"550e8400-e29b-41d4-a716-446655440000\"}";
        for (int i=0; i<2; i++) http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body)).andExpect(status().isOk())
            .andExpect(jsonPath("sender").value("alice")).andExpect(jsonPath("content").value("hello"));
        assertEquals(1,messages.count());
        String changed=body.replace("hello","different");
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(changed)).andExpect(status().isConflict());
        assertEquals(1,messages.count());
    }
    @Test void httpSendRejectsUnacceptedBlockedAndMalformedRequests() throws Exception {
        String body="{\"receiver\":\"bob\",\"content\":\"hello\",\"clientMessageId\":\"550e8400-e29b-41d4-a716-446655440000\"}";
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body)).andExpect(status().isForbidden());
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body.replace("550e8400-e29b-41d4-a716-446655440000","bad")))
            .andExpect(status().isBadRequest());
        accepted(); blocks.block("bob","alice");
        http.perform(post("/api/public/chat/messages").with(user("alice@gmail.com"))
            .contentType("application/json").content(body)).andExpect(status().isNotFound());
        assertEquals(0,messages.count());
    }
    @Test void pendingDoesNotGrantChatAccessAndCannotSpoofSender() throws Exception {
        connections.request("alice","bob");
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com"))).andExpect(status().isForbidden());
        ChatMessageDto dto=new ChatMessageDto();dto.setSender("eve");dto.setReceiver("bob");dto.setContent("hello");
        assertThrows(ResponseStatusException.class,()->socket.sendMessage(dto,()->"alice"));
        assertEquals(0,messages.count());
    }
    @Test void acceptedConnectionGrantsChatAndSocketSenderComesFromPrincipal() throws Exception {
        accepted();
        ChatMessageDto dto=new ChatMessageDto();dto.setSender("eve");dto.setReceiver("bob");dto.setContent("hello");
        socket.sendMessage(dto,()->"alice");
        assertEquals("alice",messages.findAll().get(0).getSender().getUserName());
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].content").value("hello"));
        http.perform(get("/api/public/chat/history/alice/bob").with(user("eve@gmail.com"))).andExpect(status().isForbidden());
        http.perform(post("/api/public/chat/read/alice/bob").with(user("eve@gmail.com"))).andExpect(status().isForbidden());
        http.perform(get("/api/public/chat/conversations/alice").with(user("eve@gmail.com"))).andExpect(status().isForbidden());
    }
    @Test void blockingHidesBothProfilesAndDiscoveryAndClosesChatImmediately() throws Exception {
        accepted();chat.send("alice","bob","before blocking");blocks.block("alice","bob");
        assertFalse(matching.candidates("alice",MatchMode.HOBBIES,Instant.now(),100).contains("bob"));
        assertFalse(matching.candidates("bob",MatchMode.HOBBIES,Instant.now(),100).contains("alice"));
        for (String[] pair:List.of(new String[]{"alice","bob"},new String[]{"bob","alice"})) {
            http.perform(get("/api/public/profiles/"+pair[1]).with(user(pair[0]+"@gmail.com"))).andExpect(status().isNotFound());
            http.perform(get("/api/public/chat/history/"+pair[0]+"/"+pair[1]).with(user(pair[0]+"@gmail.com"))).andExpect(status().isNotFound());
            http.perform(get("/api/public/user/image/"+pair[1]).with(user(pair[0]+"@gmail.com"))).andExpect(status().isNotFound());
            http.perform(get("/api/public/chat/conversations/"+pair[0]).with(user(pair[0]+"@gmail.com")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        }
        assertThrows(ResponseStatusException.class,()->chat.send("bob","alice","after blocking"));
        assertThrows(ResponseStatusException.class,()->connections.request("bob","alice"));
        assertEquals(1,messages.count());
    }
    @Test void unblockRestoresVisibilityButNeedsFreshConsentAndCannotUndoAnotherUsersBlock() throws Exception {
        accepted();blocks.block("alice","bob");blocks.block("bob","alice");blocks.unblock("alice","bob");
        http.perform(get("/api/public/profiles/bob").with(user("alice@gmail.com"))).andExpect(status().isNotFound());
        blocks.unblock("bob","alice");
        http.perform(get("/api/public/profiles/bob").with(user("alice@gmail.com"))).andExpect(status().isOk());
        assertThrows(ResponseStatusException.class,()->chat.send("alice","bob","still forbidden"));
        var fresh=connections.request("alice","bob");assertEquals(Connection.State.PENDING,fresh.getStatus());
        connections.transition("bob",fresh.getId(),"accept");
        assertNotNull(chat.send("alice","bob","new consent").getId());
    }
    @Test void blocksArePrivateIdempotentAndOwnedByAuthenticatedActor() throws Exception {
        http.perform(post("/api/public/blocks").with(user("alice@gmail.com")).contentType("application/json").content("{\"userName\":\"bob\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("blocker").value("alice"));
        blocks.block("alice","bob");assertEquals(1,blockRepository.count());
        http.perform(get("/api/public/blocks").with(user("bob@gmail.com"))).andExpect(jsonPath("content.length()").value(0));
        http.perform(delete("/api/public/blocks/alice").with(user("bob@gmail.com"))).andExpect(status().isOk());
        assertTrue(blockRepository.blockedEitherWay("alice","bob"));
        http.perform(delete("/api/public/blocks/bob").with(user("alice@gmail.com"))).andExpect(status().isOk());
        assertFalse(blockRepository.blockedEitherWay("alice","bob"));
    }
    @Test void removedConnectionsDoNotAllowOldHistoryOrSending() throws Exception {
        var c=accepted();chat.send("alice","bob","old message");connections.transition("alice",c.getId(),"remove");
        assertThrows(ResponseStatusException.class,()->chat.send("alice","bob","new message"));
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com"))).andExpect(status().isForbidden());
    }
    @Test void usersCannotModifyAnotherUsersProfileOrLocation() throws Exception {
        http.perform(put("/api/public/user/update/aboutMe").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"aboutMe\":\"stolen\"}"))
            .andExpect(status().isForbidden());
        http.perform(put("/api/public/user/update/hobby").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"hobbies\":[]}"))
            .andExpect(status().isForbidden());
        http.perform(put("/api/public/user/update/speciality").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"bob\",\"specialities\":[]}"))
            .andExpect(status().isForbidden());
        http.perform(delete("/api/public/user/image/delete/bob").with(user("alice@gmail.com")))
            .andExpect(status().isForbidden());
        http.perform(delete("/api/public/location/bob").with(user("alice@gmail.com")))
            .andExpect(status().isForbidden());
        http.perform(post("/api/public/location/update/bob").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"country\":\"Armenia\",\"region\":\"Yerevan\",\"latitude\":40.1,\"longitude\":44.5}"))
            .andExpect(status().isForbidden());
        assertEquals("Hello from bob",users.findUserByUserName("bob").orElseThrow().getAboutMe());
        assertEquals(1,users.findUserByUserName("bob").orElseThrow().getHobbies().size());
    }
    @Test void verifiedAccountCannotBeDeletedViaPublicCancellation() throws Exception {
        http.perform(delete("/api/public/user/delete/verify/bob@gmail.com")).andExpect(status().isBadRequest());
        assertTrue(users.findUserByUserName("bob").isPresent());
    }
    @Test void usersCanStillModifyTheirOwnProfile() throws Exception {
        http.perform(put("/api/public/user/update/aboutMe").with(user("alice@gmail.com"))
            .contentType("application/json").content("{\"userName\":\"alice\",\"aboutMe\":\"New biography\"}"))
            .andExpect(status().isOk());
        assertEquals("New biography",users.findUserByUserName("alice").orElseThrow().getAboutMe());
    }
    @Test void regularUsersCannotBypassProfileVisibilityViaAdminDirectory() throws Exception {
        http.perform(get("/api/private/admin/getAll").with(user("alice@gmail.com").authorities(()->"USER")))
            .andExpect(status().isForbidden());
    }
    @Test void likingEndpointsAreRemoved() throws Exception {
        http.perform(post("/api/public/user/like").with(user("alice@gmail.com"))).andExpect(status().is4xxClientError());
        http.perform(get("/api/public/user/like/get").with(user("alice@gmail.com"))).andExpect(status().isNotFound());
    }

    @Test void browserPreflightAllowsChatMutations() throws Exception {
        for (String method : List.of("PATCH", "DELETE", "POST")) {
            http.perform(options("/api/public/chat/messages/1")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", method)
                .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString(method)));
        }
    }
    @Test void editAndDeletePersistThroughHttpForBothParticipants() throws Exception {
        accepted();
        var sent=chat.send("alice", "bob", "original");
        String url="/api/public/chat/messages/"+sent.getId();
        http.perform(patch(url).with(user("bob@gmail.com")).contentType("application/json").content("{\"content\":\"illegal\"}"))
            .andExpect(status().isForbidden());
        http.perform(patch(url).with(user("alice@gmail.com")).header("Origin", "http://localhost:3000")
            .contentType("application/json").content("{\"content\":\"updated\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("edited").value(true)).andExpect(jsonPath("content").value("updated"));
        http.perform(get("/api/public/chat/history/bob/alice").with(user("bob@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("[0].content").value("updated"));
        http.perform(delete(url).param("everyone","true").with(user("bob@gmail.com"))).andExpect(status().isForbidden());
        http.perform(delete(url).param("everyone","false").with(user("bob@gmail.com"))).andExpect(status().isNoContent());
        http.perform(get("/api/public/chat/history/bob/alice").with(user("bob@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("[0].content").value("updated"));
        http.perform(delete(url).param("everyone","true").with(user("alice@gmail.com"))).andExpect(status().isNoContent());
        http.perform(get("/api/public/chat/history/alice/bob").with(user("alice@gmail.com")))
            .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }
}
