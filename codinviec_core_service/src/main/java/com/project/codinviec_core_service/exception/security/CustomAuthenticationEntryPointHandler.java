package com.project.codinviec_core_service.exception.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.codinviec_core_service.enums.CommonErrorCode;
import com.project.codinviec_core_service.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        log.warn("AuthenticationException: {} | URI: {} | Method: {} | Message: {}",
                authException.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                authException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(CommonErrorCode.UNAUTHORIZED.getCode())
                .message(CommonErrorCode.UNAUTHORIZED.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        response.setStatus(CommonErrorCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
