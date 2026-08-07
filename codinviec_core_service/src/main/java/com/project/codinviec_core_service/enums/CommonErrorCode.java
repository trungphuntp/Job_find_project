package com.project.codinviec_core_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR("SYS-000", HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi hệ thống xảy ra"),
    INVALID_INPUT("SYS-001", HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    UNAUTHORIZED("SYS-002", HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập hoặc phiên đã hết hạn"),
    FORBIDDEN("SYS-003", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
