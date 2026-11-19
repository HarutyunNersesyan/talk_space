package com.talk_space.modules.users.api;
import java.time.*;
import org.springframework.web.server.ResponseStatusException;
public class CaptchaServiceTest {
 static class Time extends Clock {
  Instant now=Instant.parse("2026-11-18T00:00:00Z");
  public ZoneId getZone(){return ZoneOffset.UTC;} public Clock withZone(ZoneId z){return this;} public Instant instant(){return now;}
 }
 static void rejects(Runnable r){try{r.run();throw new AssertionError("Accepted invalid verification");}catch(ResponseStatusException expected){}}
 static String answer(CaptchaService.Challenge c){String[] s=c.prompt().split(" ");return ""+(Integer.parseInt(s[0])+Integer.parseInt(s[2]));}
 @org.junit.jupiter.api.Test void verifiesSingleUseExpiryAndInvalidAnswers(){
  Time time=new Time();CaptchaService service=new CaptchaService(time);
  var c=service.create();service.verify(c.id(),answer(c));service.consume(c.id());rejects(()->service.consume(c.id()));
  var unverified=service.create();rejects(()->service.consume(unverified.id()));
  var wrong=service.create();rejects(()->service.verify(wrong.id(),"99"));rejects(()->service.verify(wrong.id(),answer(wrong)));
  var expired=service.create();time.now=time.now.plusSeconds(301);rejects(()->service.verify(expired.id(),answer(expired)));
  var verified=service.create();service.verify(verified.id(),answer(verified));time.now=time.now.plusSeconds(301);rejects(()->service.consume(verified.id()));
  rejects(()->service.verify(null,"7"));rejects(()->service.consume(null));

 }
}
