package com.project.codinviec_payment_service.service.payment;

import com.project.codinviec_payment_service.dto.payment.ServiceProductDTO;
import com.project.codinviec_payment_service.request.PageRequestCustom;
import com.project.codinviec_payment_service.request.payment.ServiceProductRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ServiceProductService {
    List<ServiceProductDTO> getAll();
    Page<ServiceProductDTO> getAllWithPage(PageRequestCustom req);
    ServiceProductDTO getById(Integer id);
    ServiceProductDTO create(ServiceProductRequest req);
    ServiceProductDTO update(Integer id, ServiceProductRequest req);
    ServiceProductDTO deleteById(Integer id);
}
