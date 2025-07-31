package com.josegomez.spring_mongo_api.service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import javax.crypto.SecretKey;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.josegomez.spring_mongo_api.domain.model.User;
import com.josegomez.spring_mongo_api.exceptions.ApiException;
import com.josegomez.spring_mongo_api.security.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "access")
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList())
                .issuedAt(Date.from(now))
                .issuer(jwtProperties.getIssuer())
                .expiration(Date.from(expiration))
                .signWith(getSignKey())
                .compact();
    }

    public String generateActivationToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getActivateExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "activation")
                .issuedAt(Date.from(now))
                .issuer(jwtProperties.getIssuer())
                .expiration(Date.from(expiration))
                .signWith(getSignKey())
                .compact();
    }

    public Long getIdFromActivationToken(String token) {
        String tokenType = getTokenType(token);
        if (!"activation".equals(tokenType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid token");
        }

        Claims claims = extractAllClaims(token);

        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Invalid token subject");
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token) && getTokenType(token).equals("access");
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration()
                    .before(Date.from(Instant.now()));
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtProperties.getSecret()
                .getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("type", String.class);
    }

}
