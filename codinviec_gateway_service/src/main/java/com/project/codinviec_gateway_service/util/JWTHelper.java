package com.project.codinviec_gateway_service.util;


import com.project.codinviec_gateway_service.dto.JwtUserDTO;
import com.project.codinviec_gateway_service.enums.GatewayErrorCode;
import com.project.codinviec_gateway_service.exception.AppException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTHelper {

    @Autowired
    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, String> redisTemplateDb;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long expirationTime;

    @Value("${jwt.expiration.refresh}")
    private long expirationRefresh;


    private SecretKey keyParse;


    /***
     * Key redis note
     */
    //    user_devices:{userId} = [các giá trị devices]
    private final String keyDevicesId = "user_devices:";
    private final CookieHelper cookieHelper;

    @PostConstruct
    public void init() {
        this.keyParse = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }



    public void remoteAllTokens(String userId, String deviceId, String keyRefreshTokenRedis, ServerHttpResponse response) {
        redisTemplateDb.delete(keyRefreshTokenRedis+userId+":"+deviceId);
        cookieHelper.clearRefreshTokenCookie(response);
        cookieHelper.clearAccessTokenCookie(response);

    }

    public JwtUserDTO verifyAccessToken(String accessToken, String keyVersionRedis, ServerHttpResponse response) {
        String userId = "";
        String deviceId = "";
        try{
            Jws<Claims> tokenValidate = Jwts.parser()
                    .verifyWith(keyParse)
                    .build()
                    .parseClaimsJws(accessToken);

            Claims claims = tokenValidate.getBody();
            userId = claims.getIssuer();
            String role = claims.getSubject();

            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                throw new JwtException("Type is not valid!");
            }

            deviceId = claims.get("device", String.class);
            if (!checkDevicesIDToken(userId, deviceId)) {
                throw new JwtException("Devices is not valid");
            }

            Integer tokenVersionInToken = claims.get("tokenVersion", Integer.class);
            Integer tokenVersionInDb = getTokenVersion(userId, keyVersionRedis);
            if (!Objects.equals(tokenVersionInToken, tokenVersionInDb)) {
                throw new JwtException("Token revoked");
            }

            return JwtUserDTO.builder()
                    .userId(userId)
                    .role(role)
                    .tokenVersion(tokenVersionInDb)
                    .deviceId(deviceId)
                    .build();
        } catch (ExpiredJwtException e) {
            remoteAllTokens(userId, deviceId, keyVersionRedis, response);
            throw new AppException(GatewayErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            remoteAllTokens(userId, deviceId, keyVersionRedis, response);
            throw new AppException(GatewayErrorCode.TOKEN_INVALID);
        }
    }

    public Integer getTokenVersion(String userId,String keyVersionRedis) {
        try{
            if(redisTemplateDb.hasKey(keyVersionRedis+userId)){
                return Integer.parseInt(Objects.requireNonNull(redisTemplateDb.opsForValue().get(keyVersionRedis+userId)));
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }

    public boolean checkDevicesIDToken(String userId, String devicesId) {
        try {
            Double score = redisTemplateDb.opsForZSet().score(keyDevicesId + userId, devicesId);
            return score != null;
        } catch (Exception e) {
            log.error("Error checking device ID for userId: {}, devicesId: {}", userId, devicesId, e);
            return false;
        }
    }
}
