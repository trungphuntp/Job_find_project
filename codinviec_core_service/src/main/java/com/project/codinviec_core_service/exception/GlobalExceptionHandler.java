package com.project.codinviec_core_service.exception;

import com.project.codinviec_core_service.enums.CommonErrorCode;
import com.project.codinviec_core_service.response.ErrorResponse;
import com.project.codinviec_core_service.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex, HttpServletRequest request) {
        log.warn("AppException: code={} | URI: {} | Method: {} | Message: {}",
                ex.getErrorCode().getCode(),
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> ValidationErrorResponse.FieldError.builder()
                        .field(e.getField())
                        .rejectedValue(e.getRejectedValue() != null ? e.getRejectedValue().toString() : null)
                        .reason(e.getDefaultMessage())
                        .build())
                .toList();

        log.warn("Validation error at {} {}: {}", request.getMethod(), request.getRequestURI(), fieldErrors);

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .code(CommonErrorCode.INVALID_INPUT.getCode())
                .message(CommonErrorCode.INVALID_INPUT.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<ValidationErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> ValidationErrorResponse.FieldError.builder()
                        .field(v.getPropertyPath().toString())
                        .rejectedValue(v.getInvalidValue() != null ? v.getInvalidValue().toString() : null)
                        .reason(v.getMessage())
                        .build())
                .toList();

        log.warn("Constraint violation at {} {}: {}", request.getMethod(), request.getRequestURI(), fieldErrors);

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .code(CommonErrorCode.INVALID_INPUT.getCode())
                .message(CommonErrorCode.INVALID_INPUT.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("AccessDeniedException: URI: {} | Method: {} | Message: {}",
                request.getRequestURI(), request.getMethod(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .code(CommonErrorCode.FORBIDDEN.getCode())
                .message(CommonErrorCode.FORBIDDEN.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(CommonErrorCode.FORBIDDEN.getHttpStatus()).body(response);
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
