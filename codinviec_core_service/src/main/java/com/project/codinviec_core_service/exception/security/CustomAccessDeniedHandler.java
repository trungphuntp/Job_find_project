package com.project.codinviec_core_service.exception.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.codinviec_core_service.enums.CommonErrorCode;
import com.project.codinviec_core_service.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        log.warn("AccessDeniedException: {} | URI: {} | Method: {} | Message: {}",
                accessDeniedException.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                accessDeniedException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(CommonErrorCode.FORBIDDEN.getCode())
                .message(CommonErrorCode.FORBIDDEN.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        response.setStatus(CommonErrorCode.FORBIDDEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
