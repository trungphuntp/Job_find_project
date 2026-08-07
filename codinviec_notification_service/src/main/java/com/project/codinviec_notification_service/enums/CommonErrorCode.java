package com.project.codinviec_notification_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR("SYS-000", HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi hệ thống xảy ra");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
