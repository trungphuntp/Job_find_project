package com.project.codinviec_auth_service.util;

import com.project.codinviec_auth_service.dto.VerifyUserDTO;
import com.project.codinviec_auth_service.enums.EmailErrorCode;
import com.project.codinviec_auth_service.event.payload.VerifyRegisterPayload;
import com.project.codinviec_auth_service.event.publish.AuthEventPublisher;
import com.project.codinviec_auth_service.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class VerifyUserHelper {

    private final SecureRandom random = new SecureRandom();

    @Autowired
    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, String> redisTemplateDb;
    private final ObjectMapper objectMapper;
    private final AuthEventPublisher authEventPublisher;

    public String generateOtp4Digits() {
        int otp = random.nextInt(9000) + 1000;
        return String.valueOf(otp);
    }

    public void sendOtpUserEmail(String email, int counterResend, String keyOtpUser) {
        try {
            String otp = generateOtp4Digits();
            VerifyUserDTO verifyUserDTO = VerifyUserDTO.builder()
                    .email(email)
                    .otp(otp)
                    .counterResend(counterResend)
                    .build();
            String json = objectMapper.writeValueAsString(verifyUserDTO);
            redisTemplateDb.opsForValue().set(keyOtpUser + email, json, Duration.ofMinutes(10));
            authEventPublisher.publishVerifyRegister(VerifyRegisterPayload.builder()
                    .email(email)
                    .otp(otp)
                    .build());
        } catch (RuntimeException e) {
            throw new AppException(EmailErrorCode.SEND_OTP_FAIL);
        }
    }
}
