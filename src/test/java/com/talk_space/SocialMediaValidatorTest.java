package com.talk_space;

import com.talk_space.validation.SocialMediaValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SocialMediaValidatorTest {

    @Test
    void testValidFacebookUrls() {
        assertTrue(SocialMediaValidator.mediaValidation("FACEBOOK", "https://www.facebook.com/username"));
        assertTrue(SocialMediaValidator.mediaValidation("facebook", "http://facebook.com/username"));
        assertTrue(SocialMediaValidator.mediaValidation("FACEBOOK", "https://facebook.com/username/"));
        assertTrue(SocialMediaValidator.mediaValidation("FACEBOOK", "https://facebook.com/username?ref=profile"));
    }

    @Test
    void testInvalidFacebookUrls() {
        assertFalse(SocialMediaValidator.mediaValidation("FACEBOOK", "https://www.facebok.com/username")); // typo
        assertFalse(SocialMediaValidator.mediaValidation("FACEBOOK", "https://twitter.com/username"));
        assertFalse(SocialMediaValidator.mediaValidation("FACEBOOK", "facebook.com/"));
    }

    @Test
    void testValidInstagramUrls() {
        assertTrue(SocialMediaValidator.mediaValidation("INSTAGRAM", "https://instagram.com/username"));
        assertTrue(SocialMediaValidator.mediaValidation("instagram", "http://www.instagram.com/user.name"));
        assertTrue(SocialMediaValidator.mediaValidation("INSTAGRAM", "https://instagram.com/username/"));
        assertTrue(SocialMediaValidator.mediaValidation("INSTAGRAM", "https://instagram.com/username?utm_source=profile"));
    }

    @Test
    void testInvalidInstagramUrls() {
        assertFalse(SocialMediaValidator.mediaValidation("INSTAGRAM", "https://instagrm.com/username")); // typo
        assertFalse(SocialMediaValidator.mediaValidation("INSTAGRAM", "instagram.com/"));
        assertFalse(SocialMediaValidator.mediaValidation("INSTAGRAM", "https://facebook.com/username"));
    }

    @Test
    void testValidTwitterUrls() {
        assertTrue(SocialMediaValidator.mediaValidation("TWITTER", "https://twitter.com/username"));
        assertTrue(SocialMediaValidator.mediaValidation("twitter", "http://www.twitter.com/username"));
        assertTrue(SocialMediaValidator.mediaValidation("TWITTER", "https://mobile.twitter.com/username/"));
        assertTrue(SocialMediaValidator.mediaValidation("TWITTER", "https://twitter.com/username?lang=en"));
    }

    @Test
    void testInvalidTwitterUrls() {
        assertFalse(SocialMediaValidator.mediaValidation("TWITTER", "https://twiter.com/username")); // typo
        assertFalse(SocialMediaValidator.mediaValidation("TWITTER", "twitter.com/"));
        assertFalse(SocialMediaValidator.mediaValidation("TWITTER", "https://linkedin.com/in/username"));
    }

    @Test
    void testValidLinkedInUrls() {
        assertTrue(SocialMediaValidator.mediaValidation("LINKEDIN", "https://linkedin.com/in/username"));
        assertTrue(SocialMediaValidator.mediaValidation("linkedin", "http://www.linkedin.com/company/companyname"));
        assertTrue(SocialMediaValidator.mediaValidation("LINKEDIN", "https://linkedin.com/school/schoolname/"));
        assertTrue(SocialMediaValidator.mediaValidation("LINKEDIN", "https://linkedin.com/in/username?trk=profile"));
    }

    @Test
    void testInvalidLinkedInUrls() {
        assertFalse(SocialMediaValidator.mediaValidation("LINKEDIN", "https://linkdin.com/in/username")); // typo
        assertFalse(SocialMediaValidator.mediaValidation("LINKEDIN", "linkedin.com/"));
        assertFalse(SocialMediaValidator.mediaValidation("LINKEDIN", "https://twitter.com/username"));
    }

    @Test
    void testValidYoutubeUrls() {
        assertTrue(SocialMediaValidator.mediaValidation("YOUTUBE", "https://youtube.com/channel/UC123456789"));
        assertTrue(SocialMediaValidator.mediaValidation("youtube", "http://www.youtube.com/c/SomeChannel"));
        assertTrue(SocialMediaValidator.mediaValidation("YOUTUBE", "https://youtu.be/abc123XYZ"));
        assertTrue(SocialMediaValidator.mediaValidation("YOUTUBE", "https://youtube.com/user/username/"));
    }

    @Test
    void testInvalidYoutubeUrls() {
        assertFalse(SocialMediaValidator.mediaValidation("YOUTUBE", "https://youtubee.com/channel/UC123456789")); // typo
        assertFalse(SocialMediaValidator.mediaValidation("YOUTUBE", "youtube.com/"));
        assertFalse(SocialMediaValidator.mediaValidation("YOUTUBE", "https://facebook.com/username"));
    }

}
