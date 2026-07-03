package com.project.codinviec_payment_service.exception;

import com.project.codinviec_payment_service.exception.common.NotFoundIdExceptionHandler;
import com.project.codinviec_payment_service.exception.common.ParamExceptionHandler;
import com.project.codinviec_payment_service.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundIdExceptionHandler.class)
    public ResponseEntity<BaseResponse> handleNotFoundIdException(
            NotFoundIdExceptionHandler ex, HttpServletRequest request) {
        log.error("NotFoundIdExceptionHandler: {} | URI: {} | Method: {} | Message: {}",
                ex.getClass().getSimpleName(), request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);

        String userMessage = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Không tìm thấy dữ liệu yêu cầu";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(userMessage, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ParamExceptionHandler.class)
    public ResponseEntity<BaseResponse> handleParamException(
            ParamExceptionHandler ex, HttpServletRequest request) {
        log.error("ParamExceptionHandler: {} | URI: {} | Method: {} | Message: {}",
                ex.getClass().getSimpleName(), request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);

        String userMessage = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage() : "Tham số không hợp lệ. Vui lòng kiểm tra lại!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(userMessage, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((e) -> errors.put(e.getField(), e.getDefaultMessage()));

        log.warn("Validation Error: {} | URI: {} | Method: {} | Errors: {}",
                ex.getClass().getSimpleName(), request.getRequestURI(), request.getMethod(), errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.validationError(errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("AccessDeniedException: {} | URI: {} | Method: {} | Message: {}",
                ex.getClass().getSimpleName(), request.getRequestURI(), request.getMethod(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.error("Bạn không có quyền!", HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected Exception: {} | URI: {} | Method: {} | Message: {}",
                ex.getClass().getName(), request.getRequestURI(), request.getMethod(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error("Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau!", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
