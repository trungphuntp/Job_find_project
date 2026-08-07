package com.project.codinviec_gateway_service.exception;

import com.project.codinviec_gateway_service.enums.GatewayErrorCode;
import com.project.codinviec_gateway_service.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        log.warn("AppException: code={} | URI: {} | Method: {} | Message: {}",
                ex.getErrorCode().getCode(),
                request.getURI().getPath(),
                request.getMethod(),
                ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .path(request.getURI().getPath())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(
            Exception ex, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        log.error("Unexpected error at {} {}: ", request.getMethod(), request.getURI().getPath(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .code(GatewayErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(GatewayErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .path(request.getURI().getPath())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.internalServerError().body(response);
    }
}
