package com.project.codinviec_auth_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthenticationErrorCode implements ErrorCode {

    // Đăng nhập
    INVALID_CREDENTIALS("AUTH-001", HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không hợp lệ!"),
    ACCOUNT_BLOCKED("AUTH-002", HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa. Vui lòng liên hệ admin!"),
    ALREADY_LOGGED_IN("AUTH-003", HttpStatus.CONFLICT, "Tài khoản này đã được đăng nhập từ thiết bị khác!"),
    GOOGLE_LOGIN_FAIL("AUTH-004", HttpStatus.BAD_REQUEST, "Đăng nhập Google thất bại!"),

    // Token
    TOKEN_INVALID("AUTH-005", HttpStatus.UNAUTHORIZED, "Token không hợp lệ!"),
    TOKEN_EXPIRED("AUTH-006", HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!"),
    TOKEN_CREATE_FAIL("AUTH-007", HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo Token!"),

    // Refresh Token
    REFRESH_TOKEN_NOT_FOUND("AUTH-008", HttpStatus.UNAUTHORIZED, "Không tìm thấy Refresh Token!"),
    REFRESH_TOKEN_INVALID("AUTH-009", HttpStatus.UNAUTHORIZED, "RefreshToken không hợp lệ!"),
    REFRESH_TOKEN_EXPIRED("AUTH-010", HttpStatus.UNAUTHORIZED, "RefreshToken đã hết hạn. Vui lòng đăng nhập lại!"),
    REFRESH_TOKEN_CREATE_FAIL("AUTH-011", HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo RefreshToken!"),

    // OTP
    OTP_INVALID("AUTH-012", HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ!"),
    OTP_RESEND_EXCEEDED("AUTH-013", HttpStatus.TOO_MANY_REQUESTS, "Số lần gửi OTP quá giới hạn. Vui lòng thử lại sau!"),
    USER_NOT_REGISTERED("AUTH-014", HttpStatus.BAD_REQUEST, "Tài khoản chưa được đăng ký!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
