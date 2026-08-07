package com.project.codinviec_notification_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    SEND_REGISTER_EMAIL_FAIL("NOTIF-001", HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email đăng ký thất bại!"),
    SEND_VERIFY_EMAIL_FAIL("NOTIF-002", HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email xác thực thất bại!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
