package com.project.codinviec_payment_service.exception.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.codinviec_payment_service.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

        // Tạo response lỗi
        BaseResponse errorResponse = BaseResponse.error(
                "Bạn không có quyền!",
                HttpStatus.FORBIDDEN);

        // Set response headers
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Ghi response body
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}