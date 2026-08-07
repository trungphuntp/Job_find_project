package com.project.codinviec_notification_service.exception;

import com.project.codinviec_notification_service.enums.CommonErrorCode;
import com.project.codinviec_notification_service.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex, HttpServletRequest request) {
        log.error("AppException: code={} | URI: {} | Message: {}",
                ex.getErrorCode().getCode(),
                request.getRequestURI(),
                ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {} {}: ", request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse response = ErrorResponse.builder()
                .code(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.internalServerError().body(response);
    }
}
