package com.project.codinviec_payment_service.service.payment;

import com.project.codinviec_payment_service.dto.payment.PaymentMethodDTO;
import com.project.codinviec_payment_service.request.PageRequestCustom;
import com.project.codinviec_payment_service.request.payment.PaymentMethodRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethodDTO> getAll();
    Page<PaymentMethodDTO> getAllWithPage(PageRequestCustom req);
    PaymentMethodDTO getById(Integer id);
    PaymentMethodDTO create(PaymentMethodRequest req);
    PaymentMethodDTO update(Integer id, PaymentMethodRequest req);
    PaymentMethodDTO deleteById(Integer id);
}
