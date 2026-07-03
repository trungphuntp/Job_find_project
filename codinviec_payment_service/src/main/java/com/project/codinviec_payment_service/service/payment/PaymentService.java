package com.project.codinviec_payment_service.service.payment;

import com.project.codinviec_payment_service.dto.payment.PaymentDTO;
import com.project.codinviec_payment_service.request.PageRequestCustom;
import com.project.codinviec_payment_service.request.payment.PaymentRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentService {
    List<PaymentDTO> getAll();
    Page<PaymentDTO> getAllWithPage(PageRequestCustom req);
    PaymentDTO getById(Integer id);
    PaymentDTO create(PaymentRequest req);
    PaymentDTO update(Integer id, PaymentRequest req);
    PaymentDTO deleteById(Integer id);
}
