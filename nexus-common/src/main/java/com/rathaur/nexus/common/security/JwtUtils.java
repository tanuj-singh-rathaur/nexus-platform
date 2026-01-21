package com.rathaur.nexus.common.security;

import com.rathaur.nexus.common.utils.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Utility class for JSON Web Token operations including generation, parsing, and validation.
 * This class is part of the common library and used across all microservices.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Component
public class JwtUtils {

    /* * In a production environment, this secret should be moved to a secure
     * configuration server or environment variable.
     */
    public static final String SECRET_BASE64 =
            "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    private static final String ISSUER = "nexus-identity";

    /**
     * Decode the Base64 secret and generate the HMAC-SHA signing key.
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for a specific subject with custom claims and expiration.
     * * @param claims Custom claims to include in the payload (e.g., roles, tenantId).
     * @param subject The unique identifier of the user (username).
     * @param expiryMillis Token validity duration in milliseconds.
     * @return A serialized JWT string.
     */
    public String generateToken(Map<String, Object> claims, String subject, long expiryMillis) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)                           // 1. Load the dynamic Map FIRST
                .setIssuer(ISSUER)                           // 2. Set specific fields AFTER to ensure they persist
                .setSubject(subject)                         // 3. Subject (sub)
                .setId(UUID.randomUUID().toString())         // 4. Unique ID (jti)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiryMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses the JWT, validates the signature, and ensures the issuer is correct.
     * * @param token The serialized JWT string.
     * @return The claims contained within the token.
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or tampered with.
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(ISSUER)       // This will now PASS because ISSUER is stamped correctly
                .setSigningKey(getSignKey())
                .setAllowedClockSkewSeconds(30)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Helper method to extract the username (subject) from a token.
     */
    public String extractUsername(String token) {
        return parseAndValidate(token).getSubject();
    }

    /**
     * Helper method to extract roles from the custom claims.
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> extractRoles(String token) {
        Object roles = parseAndValidate(token).get(SecurityConstants.CLAIM_ROLES);
        if (roles instanceof java.util.List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return java.util.List.of();
    }

    /**
     * Helper method to extract the token type (Access vs Refresh).
     */
    public String extractTokenType(String token) {
        Object typ = parseAndValidate(token).get(SecurityConstants.CLAIM_TOKEN_TYPE);
        return typ != null ? typ.toString() : "";
    }

    /**
     * Functional interface method to extract any specific claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseAndValidate(token);
        return claimsResolver.apply(claims);
    }
}