package com.project.codinviec_auth_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // Lỗi không mong muốn (uncaught exception, NPE, DB timeout, ...)
    INTERNAL_SERVER_ERROR("SYS-000", HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi hệ thống xảy ra"),

    // Lỗi validate dữ liệu đầu vào (@Valid / @Validated)
    INVALID_INPUT("SYS-001", HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),

    // Một số lỗi hệ thống phổ biến khác nên có sẵn luôn
    UNAUTHORIZED("SYS-002", HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập hoặc phiên đã hết hạn"),
    FORBIDDEN("SYS-003", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này"),
    METHOD_NOT_ALLOWED("SYS-004", HttpStatus.METHOD_NOT_ALLOWED, "Phương thức không được hỗ trợ"),
    RESOURCE_NOT_FOUND("SYS-005", HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}