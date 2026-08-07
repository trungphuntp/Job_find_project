package com.project.codinviec_core_service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResourceErrorCode implements ErrorCode {

    NOT_FOUND("RES-001", HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu yêu cầu"),
    CONFLICT("RES-002", HttpStatus.CONFLICT, "Dữ liệu đã tồn tại hoặc xung đột"),
    INVALID_PARAM("RES-003", HttpStatus.BAD_REQUEST, "Tham số không hợp lệ"),
    UNAUTHORIZED_ACTION("RES-004", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này!"),
    SEND_EMAIL_FAIL("RES-005", HttpStatus.INTERNAL_SERVER_ERROR, "Không thể gửi email. Vui lòng thử lại sau!"),
    FILE_ERROR("RES-006", HttpStatus.BAD_REQUEST, "Lỗi xử lý file. Vui lòng thử lại!");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
