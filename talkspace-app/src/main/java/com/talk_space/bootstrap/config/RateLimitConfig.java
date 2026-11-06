package com.talk_space.bootstrap.config;
import com.talk_space.bootstrap.web.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
@Configuration
public class RateLimitConfig {
    @Bean public FilterRegistrationBean<RateLimitFilter> rateLimitRegistration(RateLimitFilter filter) {
        var registration = new FilterRegistrationBean<>(filter); registration.setEnabled(false); return registration;
    }
}
