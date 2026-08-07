package com.project.codinviec_core_service.util.helper;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import org.springframework.stereotype.Component;

@Component
public class IntegerHelper {
    public int parseIntOrThrow(Object value, String messageName) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Truyền " + messageName + " không hợp lệ!");
        }
    }
}
