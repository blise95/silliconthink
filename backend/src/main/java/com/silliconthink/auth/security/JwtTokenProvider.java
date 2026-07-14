package com.silliconthink.auth.security;

import com.silliconthink.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expireMillis;

    public JwtTokenProvider(AppProperties appProperties) {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(padKey(keyBytes));
        this.expireMillis = appProperties.getJwt().getExpireSeconds() * 1000L;
    }

    public String createToken(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireMillis);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    private static byte[] padKey(byte[] keyBytes) {
        if (keyBytes.length >= 32) {
            return keyBytes;
        }
        byte[] padded = new byte[32];
        System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
        return padded;
    }
}
