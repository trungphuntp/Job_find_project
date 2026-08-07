package com.project.codinviec_auth_service.exception;

import com.project.codinviec_auth_service.enums.CommonErrorCode;
import com.project.codinviec_auth_service.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException, ServletException {
        log.warn("AccessDeniedException: {} | URI: {} | Method: {} | Message: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(CommonErrorCode.FORBIDDEN.getCode())
                .message(CommonErrorCode.FORBIDDEN.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
