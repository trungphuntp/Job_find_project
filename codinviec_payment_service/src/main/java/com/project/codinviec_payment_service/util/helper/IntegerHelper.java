package com.project.codinviec_payment_service.util.helper;

import com.project.codinviec_payment_service.exception.common.ParamExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class IntegerHelper {
    public int parseIntOrThrow(Object value, String messageName) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new ParamExceptionHandler("Truyền " + messageName + " không hợp lệ!");
        }
    }
}
