package com.talk_space.api.filter;
import com.talk_space.infrastructure.RateLimitService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
@Component @RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService limits;
    private final MeterRegistry metrics;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("OPTIONS".equals(request.getMethod())) { chain.doFilter(request, response); return; }
        String group = null; int limit = 60;
        if (path.equals("/account/auth") || path.endsWith("/signUp") || path.endsWith("/forgotPassword")) { group="auth"; limit=10; }
        else if (path.contains("/searchBy")) group="matching";
        else if (path.startsWith("/api/public/connections") || path.startsWith("/api/public/blocks")) group="connections";
        else if (path.startsWith("/api/public/search")) group="search";
        long retry = 0;
        if (group != null) {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String identity = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())
                ? "user:" + auth.getName() : "ip:" + request.getRemoteAddr();
            retry = limits.retryAfter(identity, group, limit, 60);
        }
        if (retry > 0) {
            metrics.counter("talkspace.rate_limit.rejected", "group", group).increment();
            response.setStatus(429); response.setHeader("Retry-After", String.valueOf(retry));
            response.setContentType("application/json"); response.getWriter().write("{\"message\":\"Too many requests\"}"); return;
        }
        chain.doFilter(request, response);
    }
}
