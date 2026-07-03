package com.project.codinviec_payment_service.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {
    private int code;
    private String message;
    private Object data;

    public static BaseResponse success(Object data, String message) {
        return new BaseResponse(HttpStatus.OK.value(), message, data);
    }

    public static BaseResponse error(String message, HttpStatus status) {
        return new BaseResponse(status.value(), message, null);
    }

    public static BaseResponse validationError(Map<String, String> errors) {
        return new BaseResponse(HttpStatus.BAD_REQUEST.value(), "Dữ liệu không hợp lệ!", errors);
    }


}
