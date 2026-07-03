package com.project.codinviec_payment_service.specification.payment;

import com.project.codinviec_payment_service.entity.ServiceProduct;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ServiceProductSpecification {

    public Specification<ServiceProduct> searchByName(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%");
        };
    }
}
