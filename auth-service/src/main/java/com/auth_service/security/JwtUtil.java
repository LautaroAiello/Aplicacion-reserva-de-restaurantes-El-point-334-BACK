package com.auth_service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            // Fallback: use raw UTF-8 bytes of the provided secret
            keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject, Long usuarioId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(subject)
                .claim("usuarioId", usuarioId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera token incluyendo roles y roles por restaurante (contextuales).
     * restauranteRoles: lista de mapas con keys "restauranteId" (Number) y "rol" (String)
     */
    public String generateToken(String subject, Long usuarioId, java.util.List<String> roles, java.util.List<java.util.Map<String, Object>> restauranteRoles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .claim("usuarioId", usuarioId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256);

        if (roles != null) {
            builder.claim("roles", roles);
        }
        if (restauranteRoles != null) {
            builder.claim("restauranteRoles", restauranteRoles);
        }

        return builder.compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
