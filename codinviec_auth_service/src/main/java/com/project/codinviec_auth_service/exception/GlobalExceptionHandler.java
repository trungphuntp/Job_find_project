package com.project.codinviec_auth_service.exception;

import com.project.codinviec_auth_service.enums.CommonErrorCode;
import com.project.codinviec_auth_service.response.ErrorResponse;
import com.project.codinviec_auth_service.response.ValidationErrorResponse;
import com.project.codinviec_auth_service.util.CookieHelper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CookieHelper cookieHelper;

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
                .message(ex.getErrorCode().getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    // Lỗi @Valid trên @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ValidationErrorResponse.FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ValidationErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null)
                        .reason(fe.getDefaultMessage())
                        .build())
                .toList();

        log.warn("Validation Error: {} | URI: {} | Method: {} | Errors: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                errors);

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .code(CommonErrorCode.INVALID_INPUT.getCode())
                .message(CommonErrorCode.INVALID_INPUT.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .errors(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Lỗi @Validated trên @RequestParam / @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ValidationErrorResponse.FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(cv -> ValidationErrorResponse.FieldError.builder()
                        .field(cv.getPropertyPath().toString())
                        .rejectedValue(String.valueOf(cv.getInvalidValue()))
                        .reason(cv.getMessage())
                        .build())
                .toList();

        log.warn("Constraint Violation: {} | URI: {} | Method: {} | Errors: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                errors);

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .code(CommonErrorCode.INVALID_INPUT.getCode())
                .message(CommonErrorCode.INVALID_INPUT.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .errors(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Lỗi JWT không hợp lệ (token bị tamper, sai chữ ký, ...)
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex, HttpServletRequest request, HttpServletResponse response) {
        cookieHelper.clearRefreshTokenCookie(response);
        log.warn("JwtException: {} | URI: {} | Method: {} | Message: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(CommonErrorCode.UNAUTHORIZED.getCode())
                .message(CommonErrorCode.UNAUTHORIZED.getMessage())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(CommonErrorCode.UNAUTHORIZED.getHttpStatus()).body(errorResponse);
    }

    // Fallback: lỗi không xác định
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
