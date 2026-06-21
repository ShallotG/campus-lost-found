package com.campus.lostfound.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            StringRedisTemplate stringRedisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成Access Token
     */
    public String generateAccessToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * 获取Token的JTI
     */
    public String getJtiFromToken(String token) {
        return getClaims(token).getId();
    }

    /**
     * 获取Token剩余有效期(毫秒)
     */
    public long getRemainingTime(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            // 检查是否在黑名单中
            String jti = claims.getId();
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey("jwt:blacklist:" + jti))) {
                log.warn("Token已被加入黑名单: jti={}", jti);
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将Token加入黑名单
     */
    public void addToBlacklist(String token) {
        try {
            String jti = getJtiFromToken(token);
            long remainingTime = getRemainingTime(token);
            if (remainingTime > 0) {
                stringRedisTemplate.opsForValue().set(
                        "jwt:blacklist:" + jti,
                        "1",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
                log.info("Token已加入黑名单: jti={}, ttl={}ms", jti, remainingTime);
            }
        } catch (Exception e) {
            log.warn("Token加入黑名单失败(可能已过期): {}", e.getMessage());
        }
    }

    /**
     * 解析Token的Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
