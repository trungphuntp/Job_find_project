package com.project.codinviec_core_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    TOKEN_INVALID("AUTH-001", HttpStatus.UNAUTHORIZED, "Token không hợp lệ!"),
    TOKEN_EXPIRED("AUTH-002", HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!"),
    REFRESH_TOKEN_INVALID("AUTH-003", HttpStatus.UNAUTHORIZED, "RefreshToken không hợp lệ!"),
    INVALID_CREDENTIALS("AUTH-004", HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không hợp lệ!"),
    ACCOUNT_BLOCKED("AUTH-005", HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa tạm thời. Vui lòng thử lại sau!"),
    ACCOUNT_PERMANENTLY_BLOCKED("AUTH-006", HttpStatus.valueOf(410), "Tài khoản bị khóa vĩnh viễn. Vui lòng liên hệ admin!"),
    ALREADY_LOGGED_IN("AUTH-007", HttpStatus.CONFLICT, "Tài khoản này đã được đăng nhập từ thiết bị khác!"),
    LOGIN_FAIL("AUTH-008", HttpStatus.BAD_REQUEST, "Đăng nhập thất bại!"),
    GOOGLE_LOGIN_FAIL("AUTH-009", HttpStatus.UNAUTHORIZED, "Đăng nhập Google thất bại. Vui lòng thử lại!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
