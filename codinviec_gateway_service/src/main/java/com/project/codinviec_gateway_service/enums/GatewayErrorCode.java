package com.project.codinviec_gateway_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GatewayErrorCode implements ErrorCode {

    // Lỗi hệ thống
    INTERNAL_SERVER_ERROR("GTW-000", HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi hệ thống xảy ra"),

    // Token
    TOKEN_INVALID("GTW-001", HttpStatus.UNAUTHORIZED, "Token không hợp lệ. Vui lòng đăng nhập lại!"),
    TOKEN_EXPIRED("GTW-002", HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
