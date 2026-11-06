package com.talk_space.modules.auth.internal.security;
import com.talk_space.modules.users.api.UserDirectory;
import com.talk_space.modules.users.api.model.Status;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component @RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDirectory users;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return java.util.Set.of("/account/auth", "/account/refresh", "/account/logout").contains(request.getRequestURI().substring(request.getContextPath().length()));
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String token=header.substring(7);
                var user=users.findUserByEmail(jwtTokenUtils.getName(token));
                if (user.isEmpty()) { response.sendError(401,"Invalid user");return; }
                if (!Boolean.TRUE.equals(user.get().getVerifyMail()) || user.get().getStatus() != Status.ACTIVE) {
                    response.sendError(403,"Account is unavailable");return;
                }
                var authorities=java.util.List.of(new SimpleGrantedAuthority(user.get().getRole().name()));
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.get().getEmail(),null,authorities));
            } catch (JwtException | IllegalArgumentException ex) { response.sendError(401,"Invalid or expired token");return; }
        }
        chain.doFilter(request,response);
    }
}
