package com.project.codinviec_auth_service.service.Imp;

import com.project.codinviec_auth_service.dto.GoogleInfoDTO;
import com.project.codinviec_auth_service.dto.JwtUserDTO;
import com.project.codinviec_auth_service.dto.TokenDTO;
import com.project.codinviec_auth_service.dto.VerifyUserDTO;
import com.project.codinviec_auth_service.entity.RoleEntity;
import com.project.codinviec_auth_service.entity.UserEntity;
import com.project.codinviec_auth_service.enums.AuthenticationErrorCode;
import com.project.codinviec_auth_service.enums.EmailErrorCode;
import com.project.codinviec_auth_service.event.payload.CreateUserCorePayload;
import com.project.codinviec_auth_service.event.publish.AuthEventPublisher;
import com.project.codinviec_auth_service.exception.AppException;
import com.project.codinviec_auth_service.mapper.RegisterMapper;
import com.project.codinviec_auth_service.repository.RoleRepository;
import com.project.codinviec_auth_service.repository.UserRepository;
import com.project.codinviec_auth_service.request.*;
import com.project.codinviec_auth_service.service.AuthService;
import com.project.codinviec_auth_service.service.DeviceSessionService;
import com.project.codinviec_auth_service.util.JWTHepler;
import com.project.codinviec_auth_service.util.TimeHelper;
import com.project.codinviec_auth_service.util.VerifyUserHelper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, String> redisTemplateDb;

    private final ObjectMapper objectMapper;
    private final TimeHelper timeHelper;
    private final JWTHepler jwtHepler;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterMapper registerMapper;
    private final RoleRepository roleRepository;
    private final VerifyUserHelper verifyUserHelper;
    private final AuthEventPublisher authEventPublisher;
    private final DeviceSessionService deviceSessionService;

    private final String keyRefreshTokenRedis = "token:refresh:";
    private final String keyVersionRedis = "token:version:";
    private final String keyOtpUser = "register:otp:";

    @Override
    public TokenDTO login(LoginRequest loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(AuthenticationErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(AuthenticationErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getIsBlock()) {
            throw new AppException(AuthenticationErrorCode.ACCOUNT_BLOCKED);
        }

        int tokenVersion = 1;
        if (redisTemplateDb.hasKey(keyVersionRedis + user.getId())) {
            tokenVersion = Integer.parseInt(Objects.requireNonNull(redisTemplateDb.opsForValue().get(keyVersionRedis + user.getId())));
        } else {
            redisTemplateDb.opsForValue().set(keyVersionRedis + user.getId(), String.valueOf(tokenVersion));
        }
        String userDevices = UUID.randomUUID().toString();

        String accessToken = jwtHepler.createAccessToken(user.getRole().getRoleName(), tokenVersion, user.getId(), userDevices);
        String refreshToken = jwtHepler.createRefreshToken(user.getRole().getRoleName(), user.getId(), keyRefreshTokenRedis, tokenVersion, userDevices);
        deviceSessionService.registerDevice(user.getId(), userDevices, keyRefreshTokenRedis);

        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .devicesId(userDevices)
                .build();
    }

    @Override
    @Transactional
    public String register(RegisterRequest registerRequest) {
        userRepository.findByEmail(registerRequest.getEmail())
                .ifPresent(user -> {
                    throw new AppException(EmailErrorCode.EMAIL_ALREADY_EXISTS);
                });

        RoleEntity defaultRole = roleRepository.findByRoleNameIgnoreCase("USER")
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .roleName("USER")
                        .createdDate(LocalDateTime.now())
                        .updatedDate(LocalDateTime.now())
                        .build()));

        UserEntity user = registerMapper.saveRegister(registerRequest, defaultRole);
        UserEntity savedUser = userRepository.save(user);

        if (savedUser.getId() != null && !savedUser.getId().isBlank()) {
            try {
                verifyUserHelper.sendOtpUserEmail(savedUser.getEmail(), 0, keyOtpUser);
            } catch (Exception e) {
                throw new AppException(EmailErrorCode.SEND_OTP_FAIL);
            }
        }
        return "Vui lòng xác thực tài khoản!";
    }

    @Override
    @Transactional
    public TokenDTO refreshToken(String refreshtoken, HttpServletResponse response) {
        JwtUserDTO userJwt = jwtHepler.verifyRefreshToken(refreshtoken, keyRefreshTokenRedis, keyVersionRedis, response);
        String accessToken = jwtHepler.createAccessToken(userJwt.getRole(), userJwt.getTokenVersion(), userJwt.getUserId(), userJwt.getDeviceId());
        String refreshToken = jwtHepler.createRefreshToken(userJwt.getRole(), userJwt.getUserId(), keyRefreshTokenRedis, userJwt.getTokenVersion(), userJwt.getDeviceId());
        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    @Override
    public void logout(String refreshToken, HttpServletResponse response) {
        JwtUserDTO userJwt = jwtHepler.verifyRefreshToken(refreshToken, keyRefreshTokenRedis, keyVersionRedis, response);
        deviceSessionService.logoutDevice(userJwt.getUserId(), userJwt.getDeviceId());
        jwtHepler.revokeAllTokens(userJwt.getUserId(), userJwt.getDeviceId(), keyRefreshTokenRedis, response);
    }

    @Override
    public void resendOtp(ResendOtpRequest resendOtpRequest) {
        try {
            UserEntity user = userRepository.findByEmail(resendOtpRequest.getEmail())
                    .orElseThrow(() -> new AppException(AuthenticationErrorCode.USER_NOT_REGISTERED));

            if (redisTemplateDb.hasKey(keyVersionRedis + user.getId())) {
                String json = redisTemplateDb.opsForValue().get(user.getEmail());
                VerifyUserDTO verifyUserDTO = objectMapper.readValue(json, VerifyUserDTO.class);
                if (verifyUserDTO.getCounterResend() <= 5) {
                    verifyUserHelper.sendOtpUserEmail(user.getEmail(), 0, keyOtpUser);
                } else {
                    throw new AppException(AuthenticationErrorCode.OTP_RESEND_EXCEEDED);
                }
            } else {
                verifyUserHelper.sendOtpUserEmail(user.getEmail(), 0, keyOtpUser);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(EmailErrorCode.RESEND_OTP_FAIL);
        }
    }

    @Override
    @Transactional
    public void verifyUserOtp(VerifyUserRequest verifyUserRequest) {
        try {
            UserEntity user = userRepository.findByEmail(verifyUserRequest.getEmail())
                    .orElseThrow(() -> new AppException(AuthenticationErrorCode.USER_NOT_REGISTERED));

            if (redisTemplateDb.hasKey(keyOtpUser + user.getEmail())) {
                String json = redisTemplateDb.opsForValue().get(keyOtpUser + user.getEmail());
                VerifyUserDTO verifyUserDTO = objectMapper.readValue(json, VerifyUserDTO.class);
                if (verifyUserDTO.getOtp().equalsIgnoreCase(verifyUserRequest.getOtp())) {
                    authEventPublisher.publishUserRegisteredSuccess(CreateUserCorePayload.builder()
                            .id(user.getId())
                            .avatar(user.getAvatar())
                            .email(user.getEmail())
                            .password(user.getPassword())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .isBlock(user.getIsBlock())
                            .status(user.getStatus())
                            .createdDate(user.getCreatedDate())
                            .updatedDate(user.getUpdatedDate())
                            .roleName(user.getRole().getRoleName())
                            .build());
                } else {
                    throw new AppException(AuthenticationErrorCode.OTP_INVALID);
                }
            } else {
                throw new AppException(AuthenticationErrorCode.OTP_INVALID);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AuthenticationErrorCode.OTP_INVALID);
        }
    }

    @Override
    public TokenDTO loginGoogleHandler(String code) {
        try {
            GoogleTokenResponse tokenResponse =
                    new GoogleAuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            JacksonFactory.getDefaultInstance(),
                            clientId,
                            clientSecret,
                            code,
                            redirectUri
                    ).execute();

            HttpRequestFactory factory = new NetHttpTransport().createRequestFactory();
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");
            HttpRequest request = factory.buildGetRequest(url);
            request.getHeaders().setAuthorization("Bearer " + tokenResponse.getAccessToken());

            String responseBody = request.execute().parseAsString();
            JsonNode node = objectMapper.readTree(responseBody);

            GoogleInfoDTO googleInfoDTO = GoogleInfoDTO.builder()
                    .googleId(node.path("id").asText(null))
                    .email(node.get("email").asText(null))
                    .lastName(node.get("given_name").asText(null))
                    .firstName(node.get("family_name").asText(null))
                    .picture(node.get("picture").asText(null))
                    .build();

            UserEntity user = userRepository.findByEmail(googleInfoDTO.getEmail()).orElse(null);
            if (user == null) {
                RoleEntity defaultRole = roleRepository.findByRoleNameIgnoreCase("USER")
                        .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                                .roleName("USER")
                                .createdDate(LocalDateTime.now())
                                .updatedDate(LocalDateTime.now())
                                .build()));

                user = UserEntity.builder()
                        .email(googleInfoDTO.getEmail())
                        .firstName(googleInfoDTO.getFirstName())
                        .lastName(googleInfoDTO.getLastName())
                        .avatar(googleInfoDTO.getPicture())
                        .password("")
                        .role(defaultRole)
                        .isBlock(false)
                        .status("ACTIVE")
                        .createdDate(LocalDateTime.now())
                        .updatedDate(LocalDateTime.now())
                        .build();
                user = userRepository.save(user);
                if (user == null) {
                    throw new AppException(AuthenticationErrorCode.GOOGLE_LOGIN_FAIL);
                }
                authEventPublisher.publishUserRegisteredSuccess(CreateUserCorePayload.builder()
                        .id(user.getId())
                        .avatar(user.getAvatar())
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .isBlock(user.getIsBlock())
                        .status(user.getStatus())
                        .createdDate(user.getCreatedDate())
                        .updatedDate(user.getUpdatedDate())
                        .roleName(user.getRole().getRoleName())
                        .build());
            } else {
                if (googleInfoDTO.getFirstName() == null || googleInfoDTO.getFirstName().isEmpty()) {
                    user.setFirstName(googleInfoDTO.getFirstName());
                }
                if (googleInfoDTO.getLastName() == null || googleInfoDTO.getLastName().isEmpty()) {
                    user.setLastName(googleInfoDTO.getLastName());
                }
                if (googleInfoDTO.getPicture() == null || googleInfoDTO.getPicture().isEmpty()) {
                    user.setAvatar(googleInfoDTO.getPicture());
                }
                user.setUpdatedDate(LocalDateTime.now());
                user = userRepository.save(user);
            }

            int tokenVersion = 1;
            if (redisTemplateDb.hasKey(keyVersionRedis + user.getId())) {
                tokenVersion = Integer.parseInt(Objects.requireNonNull(redisTemplateDb.opsForValue().get(keyVersionRedis + user.getId())));
            } else {
                redisTemplateDb.opsForValue().set(keyVersionRedis + user.getId(), String.valueOf(tokenVersion));
            }
            String deviceId = UUID.randomUUID().toString();

            String accessToken = jwtHepler.createAccessToken(user.getRole().getRoleName(), tokenVersion, user.getId(), deviceId);
            String refreshToken = jwtHepler.createRefreshToken(user.getRole().getRoleName(), user.getId(), keyRefreshTokenRedis, tokenVersion, deviceId);
            deviceSessionService.registerDevice(user.getId(), deviceId, keyRefreshTokenRedis);

            return TokenDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .devicesId(deviceId)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AuthenticationErrorCode.GOOGLE_LOGIN_FAIL);
        }
    }

    @Override
    public String buildUrlLoginGoogle() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&prompt=consent";
    }
}
