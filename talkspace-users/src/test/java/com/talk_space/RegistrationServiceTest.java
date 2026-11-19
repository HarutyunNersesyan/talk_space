package com.talk_space;

import com.talk_space.modules.users.api.*;
import com.talk_space.modules.users.api.dto.SignUp;
import com.talk_space.modules.users.api.model.*;
import com.talk_space.modules.users.internal.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class RegistrationServiceTest {
    PendingRegistrationRepository pending;
    UserRepository users;
    MailSenderService mail;
    CaptchaService captcha;
    BCryptPasswordEncoder encoder;
    RegistrationService service;
    @BeforeEach void setup() {
        pending=mock(PendingRegistrationRepository.class); users=mock(UserRepository.class);
        mail=mock(MailSenderService.class); captcha=mock(CaptchaService.class);
        encoder=new BCryptPasswordEncoder(4);
        service=new RegistrationService(pending,users,encoder,mail,captcha);
    }
    SignUp dto() {
        SignUp d=new SignUp(); d.setFirstName("John"); d.setLastName("Smith"); d.setUserName("johnsmith");
        d.setEmail("john@gmail.com"); d.setBirthDate(LocalDate.now().minusYears(18)); d.setGender(Gender.MALE);
        d.setPassword("Strong12!"); d.setConfirmPassword("Strong12!"); d.setCaptchaId("captcha"); return d;
    }
    PendingRegistration row() {
        PendingRegistration p=new PendingRegistration(); p.setToken("token"); p.setEmail("john@gmail.com");
        p.setUserName("johnsmith"); p.setFirstName("John"); p.setLastName("Smith"); p.setGender(Gender.MALE);
        p.setBirthDate(LocalDate.now().minusYears(18)); p.setPasswordHash(encoder.encode("Strong12!"));
        p.setCodeHash(encoder.encode("012345")); p.setCreatedAt(Instant.now()); p.setExpiresAt(Instant.now().plusSeconds(600));
        p.setResendAt(Instant.now().minusSeconds(1)); p.setSends(1);
        when(pending.lockByToken("token")).thenReturn(Optional.of(p)); return p;
    }
    @Test void exactlyEighteenCreatesPendingOnlyWithHashedSecrets() {
        var state=service.start(dto());
        ArgumentCaptor<PendingRegistration> saved=ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pending).saveAndFlush(saved.capture());
        assertTrue(encoder.matches("Strong12!",saved.getValue().getPasswordHash()));
        assertEquals(2,state.sendsRemaining()); assertNotEquals("Strong12!",saved.getValue().getPasswordHash());
        verify(users,never()).saveAndFlush(any());
        verify(mail).sendEmail(eq("john@gmail.com"),anyString(),contains("10 minutes"));
        verify(captcha).consume("captcha");
    }
    @Test void youngerThanEighteenRejected() {
        SignUp d=dto(); d.setBirthDate(LocalDate.now().minusYears(18).plusDays(1));
        assertThrows(ResponseStatusException.class,()->service.start(d)); verifyNoInteractions(mail);
    }
    @Test void mismatchedPasswordRejected() {
        SignUp d=dto(); d.setConfirmPassword("Other12!");
        assertThrows(ResponseStatusException.class,()->service.start(d)); verifyNoInteractions(mail);
    }
    @Test void incorrectCodePersistsAttemptAndNeverCreatesUser() {
        PendingRegistration p=row(); assertFalse(service.verify("token","999999").verified());
        assertEquals(1,p.getAttempts()); verify(users,never()).saveAndFlush(any());
    }
    @Test void expiredCodeRejectedEvenWhenCorrect() {
        PendingRegistration p=row(); p.setExpiresAt(Instant.now().minusSeconds(1));
        assertFalse(service.verify("token","012345").verified()); verify(users,never()).saveAndFlush(any());
    }
    @Test void correctCodeCreatesVerifiedUserAndDeletesPending() {
        PendingRegistration p=row(); assertTrue(service.verify("token","012345").verified());
        ArgumentCaptor<User> saved=ArgumentCaptor.forClass(User.class); verify(users).saveAndFlush(saved.capture());
        assertTrue(saved.getValue().getVerifyMail()); assertEquals(p.getPasswordHash(),saved.getValue().getPassword());
        verify(pending).delete(p);
    }
    @Test void totalThreeSendsAndCooldownAreEnforced() {
        PendingRegistration p=row(); service.resend("token"); assertEquals(2,p.getSends());
        assertThrows(ResponseStatusException.class,()->service.resend("token"));
        p.setResendAt(Instant.now().minusSeconds(1)); service.resend("token"); assertEquals(3,p.getSends());
        p.setResendAt(Instant.now().minusSeconds(1)); assertThrows(ResponseStatusException.class,()->service.resend("token"));
        verify(mail,times(2)).sendEmail(anyString(),anyString(),anyString());
    }
    @Test void fifthWrongCodeBlocksFurtherAttemptsAndResends() {
        PendingRegistration p=row(); p.setAttempts(4); service.verify("token","999999");
        assertFalse(service.verify("token","012345").verified());
        assertThrows(ResponseStatusException.class,()->service.resend("token"));
        verify(users,never()).saveAndFlush(any());
    }
    @Test void resendReplacesCodeWithoutResettingAttemptBudget() {
        PendingRegistration p=row(); p.setAttempts(2); String old=p.getCodeHash(); service.resend("token");
        assertNotEquals(old,p.getCodeHash()); assertEquals(2,p.getAttempts());
    }
}
