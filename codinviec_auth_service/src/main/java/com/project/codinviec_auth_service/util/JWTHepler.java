package com.project.codinviec_auth_service.util;

import com.project.codinviec_auth_service.dto.JwtUserDTO;
import com.project.codinviec_auth_service.enums.AuthenticationErrorCode;
import com.project.codinviec_auth_service.exception.AppException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JWTHepler {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long expirationAccess;

    @Value("${jwt.expiration.refresh}")
    private long expirationRefresh;

    private final CookieHelper cookieHelper;

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, String> redisTemplateDb;

    private SecretKey keyParse;

    private final String keyDevicesId = "user_devices:";

    @PostConstruct
    public void init() {
        this.keyParse = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public void revokeAllTokens(String userId, String deviceId, String keyRefreshTokenRedis, HttpServletResponse response) {
        redisTemplateDb.delete(keyRefreshTokenRedis + userId + ":" + deviceId);
        cookieHelper.clearRefreshTokenCookie(response);
        cookieHelper.clearAccessTokenCookie(response);
    }

    public String createAccessToken(String roles, int tokenVersion, String userId, String devicesId) {
        try {
            long now = System.currentTimeMillis();
            Date expirationDate = new Date(now + expirationAccess);
            return Jwts.builder()
                    .setSubject(roles)
                    .setIssuer(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(expirationDate)
                    .claim("tokenVersion", tokenVersion)
                    .claim("type", "access")
                    .claim("device", devicesId)
                    .signWith(keyParse)
                    .compact();
        } catch (Exception e) {
            throw new AppException(AuthenticationErrorCode.TOKEN_CREATE_FAIL);
        }
    }

    public String createRefreshToken(String roles, String userId, String keyRefreshTokenRedis, int tokenVersion, String devicesId) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + expirationRefresh;
            Date expirationDate = new Date(exp);

            String refreshToken = Jwts.builder()
                    .setSubject(roles)
                    .setIssuer(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(expirationDate)
                    .claim("tokenVersion", tokenVersion)
                    .claim("type", "refresh")
                    .claim("device", devicesId)
                    .signWith(keyParse)
                    .compact();

            redisTemplateDb.opsForValue().set(
                    keyRefreshTokenRedis + userId + ":" + devicesId,
                    refreshToken,
                    Duration.ofMillis(exp - now));
            return refreshToken;
        } catch (Exception e) {
            throw new AppException(AuthenticationErrorCode.REFRESH_TOKEN_CREATE_FAIL);
        }
    }

    public JwtUserDTO verifyRefreshToken(String refreshToken, String keyRefreshTokenRedis, String keyVersionRedis, HttpServletResponse response) {
        try {
            Jws<Claims> tokenValidate = Jwts.parser()
                    .verifyWith(keyParse)
                    .build()
                    .parseSignedClaims(refreshToken);

            Claims claims = tokenValidate.getBody();
            String userId = claims.getIssuer();
            String role = claims.getSubject();

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                throw new JwtException("Type is not valid!");
            }

            String deviceId = claims.get("device", String.class);
            if (!checkDevicesIDToken(userId, deviceId)) {
                throw new JwtException("Devices is not valid");
            }

            Integer tokenVersionInToken = claims.get("tokenVersion", Integer.class);
            Integer tokenVersionInDb = getTokenVersion(userId, keyVersionRedis);
            if (!tokenVersionInToken.equals(tokenVersionInDb)) {
                throw new JwtException("Token revoked");
            }

            String refreshTokenRedis = getRefreshToken(userId, keyRefreshTokenRedis, deviceId);
            if (refreshTokenRedis == null) {
                throw new AppException(AuthenticationErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            return JwtUserDTO.builder()
                    .userId(userId)
                    .role(role)
                    .tokenVersion(tokenVersionInToken)
                    .deviceId(deviceId)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (ExpiredJwtException e) {
            String userId = e.getClaims().get("userId").toString();
            String deviceId = e.getClaims().get("deviceId").toString();
            revokeAllTokens(userId, deviceId, keyRefreshTokenRedis, response);
            throw new AppException(AuthenticationErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new AppException(AuthenticationErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    public String getRefreshToken(String userId, String keyRefreshTokenRedis, String devicesId) {
        try {
            if (redisTemplateDb.hasKey(keyRefreshTokenRedis + userId + ":" + devicesId)) {
                return redisTemplateDb.opsForValue().get(keyRefreshTokenRedis + userId + ":" + devicesId);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getTokenVersion(String userId, String keyVersionRedis) {
        try {
            if (redisTemplateDb.hasKey(keyVersionRedis + userId)) {
                return Integer.parseInt(Objects.requireNonNull(redisTemplateDb.opsForValue().get(keyVersionRedis + userId)));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean checkDevicesIDToken(String userId, String devicesId) {
        try {
            Double score = redisTemplateDb.opsForZSet().score(keyDevicesId + userId, devicesId);
            return score != null;
        } catch (Exception e) {
            return false;
        }
    }
}
