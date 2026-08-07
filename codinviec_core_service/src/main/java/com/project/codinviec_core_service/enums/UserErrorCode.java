package com.project.codinviec_core_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    EMAIL_ALREADY_EXISTS("USER-001", HttpStatus.CONFLICT, "Email đã tồn tại trong hệ thống!"),
    EMAIL_NOT_CHANGE("USER-002", HttpStatus.BAD_REQUEST, "Email không được phép thay đổi!"),
    EMAIL_NOT_FOUND("USER-003", HttpStatus.NOT_FOUND, "Email không tồn tại trong hệ thống!"),
    USER_CREATE_FAIL("USER-004", HttpStatus.INTERNAL_SERVER_ERROR, "Tạo tài khoản thất bại!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
