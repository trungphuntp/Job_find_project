package com.project.codinviec_auth_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements ErrorCode {

    // User
    USER_NOT_FOUND("EMAIL-001", HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    EMAIL_ALREADY_EXISTS("EMAIL-002", HttpStatus.CONFLICT, "Email đã tồn tại"),
    INVALID_PASSWORD("EMAIL-003", HttpStatus.BAD_REQUEST, "Mật khẩu không hợp lệ"),

    // Email / Event gửi thất bại
    SEND_OTP_FAIL("EMAIL-004", HttpStatus.INTERNAL_SERVER_ERROR, "Gửi mã OTP thất bại!"),
    RESEND_OTP_FAIL("EMAIL-005", HttpStatus.INTERNAL_SERVER_ERROR, "Gửi lại mã OTP thất bại!"),
    SEND_REGISTER_EMAIL_FAIL("EMAIL-006", HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email đăng ký thất bại!"),
    SEND_VERIFY_EMAIL_FAIL("EMAIL-007", HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email xác thực thất bại!"),
    USER_REGISTER_EVENT_FAIL("EMAIL-008", HttpStatus.INTERNAL_SERVER_ERROR, "Đăng ký tài khoản gặp sự cố!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
