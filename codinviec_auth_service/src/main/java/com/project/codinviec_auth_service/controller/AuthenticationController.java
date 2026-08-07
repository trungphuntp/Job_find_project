package com.project.codinviec_auth_service.controller;

import com.project.codinviec_auth_service.dto.TokenDTO;
import com.project.codinviec_auth_service.enums.AuthenticationErrorCode;
import com.project.codinviec_auth_service.exception.AppException;
import com.project.codinviec_auth_service.request.*;
import com.project.codinviec_auth_service.response.BaseResponse;
import com.project.codinviec_auth_service.service.AuthService;
import com.project.codinviec_auth_service.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    @Value("${client.url}")
    private String clientUrl;

    private final AuthService authService;
    private final CookieHelper cookieHelper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenDTO tokenDTO = authService.login(request);
        cookieHelper.addRefreshTokenCookie(response, tokenDTO.getRefreshToken());
        cookieHelper.addAccessTokenCookies(response, tokenDTO.getAccessToken());
        return ResponseEntity.ok(BaseResponse.success(tokenDTO, "OK"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(BaseResponse.success(authService.register(registerRequest), "OK"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenRequest = cookieHelper.getRefreshToken(request).orElse(null);
        if (refreshTokenRequest == null || refreshTokenRequest.isEmpty()) {
            throw new AppException(AuthenticationErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        try {
            TokenDTO tokenDTO = authService.refreshToken(refreshTokenRequest, response);
            cookieHelper.addRefreshTokenCookie(response, tokenDTO.getRefreshToken());
            cookieHelper.addAccessTokenCookies(response, tokenDTO.getAccessToken());
            return ResponseEntity.ok(BaseResponse.success(tokenDTO, "OK"));
        } catch (AppException e) {
            cookieHelper.clearRefreshTokenCookie(response);
            cookieHelper.clearAccessTokenCookie(response);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieHelper.getRefreshToken(request).orElse(null);
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(AuthenticationErrorCode.REFRESH_TOKEN_INVALID);
        }
        authService.logout(refreshToken, response);
        return ResponseEntity.ok(BaseResponse.success("Đăng xuất thành công!", "OK"));
    }

    @GetMapping("/google")
    public void loginGoogle(HttpServletResponse response) throws IOException {
        String url = authService.buildUrlLoginGoogle();
        if (url == null) {
            throw new AppException(AuthenticationErrorCode.GOOGLE_LOGIN_FAIL);
        }
        response.sendRedirect(url);
    }

    @GetMapping("/google/callback")
    public void googleCallback(
            @RequestParam("code") String code,
            HttpServletResponse response, HttpServletRequest request
    ) throws IOException {
        TokenDTO tokenDTO = authService.loginGoogleHandler(code);
        cookieHelper.addRefreshTokenCookie(response, tokenDTO.getRefreshToken());
        cookieHelper.addAccessTokenCookies(response, tokenDTO.getAccessToken());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(clientUrl + "/login")
                .queryParam("token", tokenDTO.getAccessToken())
                .queryParam("refresh", tokenDTO.getRefreshToken())
                .queryParam("devicesId", tokenDTO.getDevicesId())
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest resendOtpRequest) {
        authService.resendOtp(resendOtpRequest);
        return ResponseEntity.ok(BaseResponse.success("Resend otp user successfully!", "OK"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyUserRequest verifyUserRequest) {
        authService.verifyUserOtp(verifyUserRequest);
        return ResponseEntity.ok(BaseResponse.success("Verify otp user successfully!", "OK"));
    }
}
