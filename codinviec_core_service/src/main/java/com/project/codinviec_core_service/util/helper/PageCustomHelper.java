package com.project.codinviec_core_service.util.helper;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.request.PageRequestCompany;
import com.project.codinviec_core_service.request.PageRequestCustom;
import com.project.codinviec_core_service.request.PageRequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageCustomHelper {
    private final IntegerHelper integerHelper;

    public PageRequestCustom validatePageCustom(PageRequestCustom pageRequestCustom) {
        // Check xem pageSize và pageNumber có phải int không
        int pageSize = integerHelper.parseIntOrThrow(pageRequestCustom.getPageSize(), "pageSize");
        int pageNumber = integerHelper.parseIntOrThrow(pageRequestCustom.getPageNumber(), "pageNumber");

        // Trường hợp keyword = null
        if (pageRequestCustom.getKeyword() == null) {
            pageRequestCustom.setKeyword("");
        } else {
            // set pageSize mặc định khi có keyword và pageSize không được truyền
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

        // truyền pageSize không hợp lệ ( > 0 mới tính)
        if (pageSize < 0)
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Truyền pageSize không hợp lệ!");

        return pageRequestCustom;
    }

    public PageRequestCompany validatePageCompany(PageRequestCompany pageRequestCompany) {
        // Check xem pageSize và pageNumber có phải int không
        int pageSize = integerHelper.parseIntOrThrow(pageRequestCompany.getPageSize(), "pageSize");
        int pageNumber = integerHelper.parseIntOrThrow(pageRequestCompany.getPageNumber(), "pageNumber");

        int minEmployees = 0;
        if (pageRequestCompany.getMinEmployees() != null ){
             minEmployees = integerHelper.parseIntOrThrow(pageRequestCompany.getMinEmployees(), "minEmployees");
        }
        int maxEmployees = 100;
        if (pageRequestCompany.getMaxEmployees() != null ){
            maxEmployees = integerHelper.parseIntOrThrow(pageRequestCompany.getMaxEmployees(), "maxEmployees");
        }

        if (maxEmployees == 0) {
            pageRequestCompany.setMaxEmployees(Integer.MAX_VALUE);
        }

        // Trường hợp keyword = null
        if (pageRequestCompany.getKeyword() == null || pageRequestCompany.getKeyword().isBlank()) {
            pageRequestCompany.setKeyword("");
        } else {
            // set pageSize mặc định khi có keyword và pageSize không được truyền
            if (pageSize == 0) {
                pageRequestCompany.setPageSize(10);
            }
        }
        if (pageRequestCompany.getLocation() == null || pageRequestCompany.getLocation().isBlank()) {
            pageRequestCompany.setLocation("");
        } else {
            if (pageSize == 0) {
                pageRequestCompany.setPageSize(10);
            }
        }

        if (pageNumber == 0) {
            pageRequestCompany.setPageNumber(1);
        }

        // truyền pageSize không hợp lệ ( > 0 mới tính)
        if (pageSize < 0)
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Truyền pageSize không hợp lệ!");
        if (minEmployees < 0)
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Truyền minEmployees không hợp lệ!");

        if (maxEmployees < 0)
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Truyền maxEmployees không hợp lệ!");
        return pageRequestCompany;
    }

    public PageRequestUser validatePageUser(PageRequestUser pageRequestUser) {
        // Check xem pageSize và pageNumber có phải int không

        int pageSize = integerHelper.parseIntOrThrow(pageRequestUser.getPageSize(), "pageSize");
        int pageNumber = integerHelper.parseIntOrThrow(pageRequestUser.getPageNumber(), "pageNumber");


        // Trường hợp keyword = null
        if (pageRequestUser.getKeyword() == null || pageRequestUser.getKeyword().isBlank()) {

            pageRequestUser.setKeyword("");
        } else {
            // set pageSize mặc định khi có keyword và pageSize không được truyền
            if (pageSize == 0) {
                pageRequestUser.setPageSize(10);
            }
        }
        if (pageRequestUser.getRoleId() == null || pageRequestUser.getRoleId().isBlank()) {
            pageRequestUser.setRoleId("");
        } else {
            if (pageSize == 0) {
                pageRequestUser.setPageSize(10);
            }
        }

        if (pageNumber == 0) {
            pageRequestUser.setPageNumber(1);
        }

        // truyền pageSize không hợp lệ ( > 0 mới tính)
        if (pageSize < 0)
            throw new AppException(ResourceErrorCode.INVALID_PARAM, "Truyền pageSize không hợp lệ!");

        return pageRequestUser;
    }
}
