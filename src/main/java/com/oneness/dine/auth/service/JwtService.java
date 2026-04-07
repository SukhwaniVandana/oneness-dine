package com.oneness.dine.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${oneness.jwt.secret}")
    private String secretKey;

    @Value("${oneness.jwt.expiration}")
    private Long jwtExpiration;

    public String generateToken(String username, Long userId,
                                String role, Long branchId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",   userId);
        claims.put("role",     role);
        claims.put("branchId", branchId);
        // [NEW] Added unique sessionId to guarantee unique token every time
        claims.put("sessionId", java.util.UUID.randomUUID().toString());
        return buildToken(claims, username, jwtExpiration);
    }

    private String buildToken(Map<String, Object> claims,
                              String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean notExpired = !isTokenExpired(token);
            boolean usernameMatch = extractedUsername.equals(username);

            // [NEW] debug logs
            log.info("Token validation — username match: {}, not expired: {}",
                    usernameMatch, notExpired);
            log.info("Extracted username: {}, Expected: {}",
                    extractedUsername, username);
            log.info("Token expiry: {}", extractExpiration(token));

            return usernameMatch && notExpired;
        } catch (Exception e) {
            // [NEW] log the actual error
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long extractBranchId(String token) {
        return extractClaim(token, claims -> claims.get("branchId", Long.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getExpirationTime() {
        return jwtExpiration;
    }
}