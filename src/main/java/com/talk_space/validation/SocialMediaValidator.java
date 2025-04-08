package com.talk_space.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;


public class SocialMediaValidator {

    private static final String FACEBOOK_REGEX = "^(https?://)?(www\\.)?(facebook|fb)\\.com/([A-Za-z0-9\\.]+)(/)?(\\?.*)?$";
    private static final String INSTAGRAM_REGEX = "^(https?://)?(www\\.)?instagram\\.com/([A-Za-z0-9_\\.]+)(/)?(\\?.*)?$";
    private static final String TWITTER_REGEX = "^(https?://)?(www\\.|mobile\\.)?twitter\\.com/([A-Za-z0-9_]+)(/)?(\\?.*)?$";
    private static final String LINKEDIN_REGEX = "^(https?://)?(www\\.)?linkedin\\.com/(in|company|school)/([A-Za-z0-9_-]+)(/)?(\\?.*)?$";

    private static final String YOUTUBE_REGEX = "^(https?://)?(www\\.)?(youtube\\.com/(channel|c|user)/([A-Za-z0-9_-]+)|youtu\\.be/([A-Za-z0-9_-]+))(/)?$";


    private static final Map<String, Pattern> PATTERN_MAP = Map.of(
            "FACEBOOK", Pattern.compile(FACEBOOK_REGEX),
            "INSTAGRAM", Pattern.compile(INSTAGRAM_REGEX),
            "TWITTER", Pattern.compile(TWITTER_REGEX),
            "LINKEDIN", Pattern.compile(LINKEDIN_REGEX),
            "YOUTUBE", Pattern.compile(YOUTUBE_REGEX)
    );


    /**
     * Validates the provided URL against the regex pattern for the specified platform.
     *
     * @param platform the name of the social media platform (e.g., FACEBOOK, INSTAGRAM)
     * @param url      the URL to validate
     * @return true if the URL is valid for the platform, false otherwise
     */
    public static boolean mediaValidation(String platform, String url) {
        Pattern pattern = PATTERN_MAP.get(platform.toUpperCase());
        return pattern.matcher(url).matches();
    }
}
