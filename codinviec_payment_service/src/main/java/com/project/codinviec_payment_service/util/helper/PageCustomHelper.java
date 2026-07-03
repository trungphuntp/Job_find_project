package com.project.codinviec_payment_service.util.helper;

import com.project.codinviec_payment_service.exception.common.ParamExceptionHandler;
import com.project.codinviec_payment_service.request.PageRequestCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageCustomHelper {
    private final IntegerHelper integerHelper;

    public PageRequestCustom validatePageCustom(PageRequestCustom pageRequestCustom) throws ParamExceptionHandler {
        int pageSize = integerHelper.parseIntOrThrow(pageRequestCustom.getPageSize(), "pageSize");
        int pageNumber = integerHelper.parseIntOrThrow(pageRequestCustom.getPageNumber(), "pageNumber");

        if (pageRequestCustom.getKeyword() == null) {
            pageRequestCustom.setKeyword("");
        } else {
            if (pageSize == 0) {
                pageRequestCustom.setPageSize(10);
            }
        }

        if (pageRequestCustom.getSortBy() == null || pageRequestCustom.getSortBy().isBlank()) {
            pageRequestCustom.setSortBy("createdAtDesc");
        } else {
            if (pageSize == 0) {
                pageRequestCustom.setPageSize(10);
            }
        }

        if (pageNumber == 0) {
            pageRequestCustom.setPageNumber(1);
        }

        if (pageSize < 0)
            throw new ParamExceptionHandler("Truyền pageSize không hợp lệ!");

        return pageRequestCustom;
    }
}
