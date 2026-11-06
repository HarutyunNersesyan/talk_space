package com.talk_space.modules.auth.internal.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
public class JwtTokenUtils {

    @org.springframework.beans.factory.annotation.Value("${JWT_SECRET:5b4241a8cd06b1a75a8eedaacff58d535a842462b8c9df2476f745fbcc42951a}")
    private String secret = "5b4241a8cd06b1a75a8eedaacff58d535a842462b8c9df2476f745fbcc42951a";


    @org.springframework.beans.factory.annotation.Value("${app.jwt.access-ttl-ms:900000}")
    private long accessTtlMs = 900000;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        List<String> rolesList = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("roles", rolesList);

        Date issuedDate = new Date();


        Date expiredDate = new Date(issuedDate.getTime() + accessTtlMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String getName(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public List<String> getRoles(String token) {
        return getAllClaimsFromToken(token).get("roles", List.class);
    }


    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

}
